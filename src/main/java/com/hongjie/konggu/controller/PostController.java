package com.hongjie.konggu.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.hongjie.konggu.annotation.AuthCheck;
import com.hongjie.konggu.common.BaseResponse;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.common.ResultUtil;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.domain.*;
import com.hongjie.konggu.model.dto.UserDTO;
import com.hongjie.konggu.model.request.PostAddRequest;
import com.hongjie.konggu.model.request.PostDoThumbRequest;
import com.hongjie.konggu.service.*;
import com.hongjie.konggu.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.*;

import static com.hongjie.konggu.constant.UserConstant.ADMIN_ROLE;

/**
 * @author: WHJ
 * @createTime: 2023-06-20 20:54
 * @description: 帖子控制层
 */
@RequestMapping("/post")
@Api(tags = "帖子控制器")
@RestController
@Slf4j
public class PostController {
    /**
     * 帖子服务
     */
    @Resource
    private PostService postService;

    /**
     * 用户服务
     */
    @Resource
    private UsersService usersService;

    /**
     * 标签服务
     */
    @Resource
    private TagService tagService;

    /**
     * 帖子点赞服务
     */
    @Resource
    private PostThumbService postThumbService;

    /**
     * 帖子标签关联服务
     */
    @Resource
    private PostTagService postTagService;

    /**
     * IO 型线程池
     */
    private final ExecutorService ioExecutorService = new ThreadPoolExecutor(4, 20, 10, TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(10000));

    /**
     *  单帖子获取缓存，key 为 postId，value 为 post
     */
    LoadingCache<Long, Post> postGetCache = Caffeine.newBuilder().expireAfterWrite(12, TimeUnit.HOURS)
            .maximumSize(5_000).build(postId -> postService.getById(postId));

    /**
     * 发布帖子
     *
     * @param postAddRequest  帖子对象
     * @return {@link BaseResponse}<{@link Long}>
     */
    @PostMapping("/add")
    @ApiOperation(value = "发布帖子")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Long> addPost(@RequestBody PostAddRequest postAddRequest) {
        // 1. 校验合法性
        if (postAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 新增帖子
        Long result = null;
        result = postService.addPost(postAddRequest);

        // 3. 获取标签
        List<Tag> tags = postAddRequest.getTags();

        // 4. 添加标签帖子关联
        for (Tag tag : tags) {
            Long tagId = tag.getId();
            // 进行标签的处理逻辑
            postTagService.addPostTag(tagId, result);
        }

        // 5. 添加标签引用次数
        for (Tag tag : tags) {
            Long tagId = tag.getId();
            tagService.addPostNum(tagId);
        }

        // 6. 移除缓存
        postGetCache.invalidate(result);
        // 7. 返回结果
        return ResultUtil.success(result);
    }

    /**
     * 查询帖子信息（仅管理员可查看）
     *
     * @param userId 用户ID
     * @return {@link BaseResponse}<{@link List}<{@link Post}>>
     */
    @GetMapping("/list")
    @ApiOperation(value = "查询全部帖子信息")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<List<Post>> listPost(@RequestParam(required = false) String userId) {
        // 1. 判断是否根据用户ID搜索帖子
        if (userId == null) {
            // 1.1 返回所有帖子
            List<Post> list = postService.list();
            return ResultUtil.success(list);
        }
        // 2. 根据用户ID搜索帖子
        Long Id = Long.valueOf(userId);
        List<Post> list = postService.searchPosts(Id);
        return ResultUtil.success(list);
    }

    /**
     * 获取通过审核的帖子
     *
     * @return {@link BaseResponse}<{@link List}<{@link Post}>>
     */
    @GetMapping("/listByUser")
    @ApiOperation(value = "获取通过审核的帖子")
    public BaseResponse<List<Post>> listPostWithUser() {
        // 1. 从线程中获取当前用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 2. 获取通过审核的帖子
        List<Post> postList = postService.listByUser();
        return ResultUtil.success(postList);
    }

    /**
     * 删除帖子(本人和管理员)
     *
     * @param id      帖子ID
     * @param request 请求对象
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除帖子(本人和管理员)")
    public BaseResponse<Boolean> deletePostByAdmin(@PathVariable Long id, HttpServletRequest request) {
        // 校验参数非空
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 检查权限-仅本人和管理员可删除
        // 2. 获取当前用户
        UserDTO user = usersService.getLoginUser(request);
        // 3. 判断删除的帖子是否存在
        Post oldPost = postService.getById(id);
        if (oldPost == null) {
            throw new BusinessException(ErrorCode.NO_FOUND_ERROR);
        }
        // 4. 仅本人或管理员可删除
        if (!oldPost.getUserId().equals(user.getId()) && !usersService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean deleteResult = postService.removeById(id);
        // 异步删除点赞信息，和标签关联信息
        CompletableFuture.runAsync(() -> {
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            QueryWrapper<PostTag> postTagQueryWrapper = new QueryWrapper<>();
            long postId = oldPost.getId();
            postThumbQueryWrapper.eq("postId", postId);
            postTagQueryWrapper.eq("postId", postId);
            boolean thumbResult = postThumbService.remove(postThumbQueryWrapper);
            boolean tagResult = postTagService.remove(postTagQueryWrapper);
            //TODO 删贴的时候是否需要减少标签引用次数？
            if (!thumbResult) {
                log.error("postThumb delete failed, postId = {}", postId);
            } else if (!tagResult) {
                log.error("postTag delete failed, postId = {}", postId);
            }
        }, ioExecutorService);

        // 移除缓存
        postGetCache.invalidate(id);

        return ResultUtil.success(deleteResult);
    }

    /**
     * 更新审核帖子状态
     *
     * @param post    修改的审核状态
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PutMapping("/update")
    @ApiOperation(value = "更新审核帖子状态")
    @AuthCheck(mustRole = ADMIN_ROLE)
    //TODO 修改参数仅发送状态（前后端）
    public BaseResponse<Boolean> updatePost(@RequestBody Post post) {
        // 1. 校验合法性
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (post.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 判断帖子是否存在
        Long id = post.getId();
        Post oldPost = postService.getById(id);
        if (oldPost == null) {
            throw new BusinessException(ErrorCode.NO_FOUND_ERROR);
        }
        // 3. 更新帖子
        boolean result = postService.updateById(post);
        // 4. 移除缓存
        postGetCache.invalidate(id);
        // 5. 返回结果
        return ResultUtil.success(result);
    }

    /**
     * 根据发帖用户ID搜索帖子
     *
     * @param id  发帖用户ID
     * @return {@link BaseResponse}<{@link List}<{@link Post}>>
     */
    @GetMapping("/search/{id}")
    @ApiOperation(value = "根据用户ID搜索帖子")
    public BaseResponse<List<Post>> searchPostsByUser(@PathVariable String id) {
        // 1. 从线程中获取当前用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 2. 搜索帖子
        Long userId = Long.valueOf(id);
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<Post> list = postService.searchPosts(userId);
        return ResultUtil.success(list);
    }

    /**
     * 获取帖子详细信息
     *
     * @param id 帖子ID
     * @return {@link BaseResponse}<{@link Post}>
     */
    @GetMapping("/detail/{id}")
    @ApiOperation(value = "获取帖子详细信息")
    public BaseResponse<Post> getPostDetail(@PathVariable Long id) {
        // 1. 从线程中获取当前用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 2. 从缓存中获取帖子
        Post post = postGetCache.get(id);

        // 3. 返回用户和标签列表
        assert post != null;
        Users usersServiceById = usersService.getById(post.getUserId());
        Users safetyUser = usersService.getSafetyUser(usersServiceById);
        post.setUser(safetyUser);
        Long postId = post.getId();
        List<Tag> tagList = postTagService.searchByPostId(postId);
        post.setTagList(tagList);

        return ResultUtil.success(post);
    }

    /**
     * 点赞/取消点赞
     *
     * @param postDoThumbRequest 点赞请求对象
     * @return {@link BaseResponse}<{@link Long}>
     */
    @PostMapping("/thumb")
    @ApiOperation(value = "点赞/取消点赞")
    public BaseResponse<Long> postDoThumb(@RequestBody PostDoThumbRequest postDoThumbRequest) {
        // 1. 判断请求非空
        if (postDoThumbRequest == null || postDoThumbRequest.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 从线程中获取当前用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        Long userId = user.getId();
        Long postId = postDoThumbRequest.getPostId();
        long result = postThumbService.doThumb(userId, postId);
        if (result != 0) {
            // 移除缓存
            postGetCache.invalidate(postId);
        }
        return ResultUtil.success(result);
    }

}
