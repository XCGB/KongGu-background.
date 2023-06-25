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
import com.hongjie.konggu.model.domain.request.PostAddRequest;
import com.hongjie.konggu.model.domain.request.PostDoThumbRequest;
import com.hongjie.konggu.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.*;

import static com.hongjie.konggu.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author: WHJ
 * @createTime: 2023-06-20 20:54
 * @description: 帖子控制层
 */
@RequestMapping("/post")
@RestController
@Slf4j
public class PostController {
    @Resource
    private PostService postService;
    @Resource
    private UsersService usersService;
    @Resource
    private TagService tagService;
    @Resource
    private PostThumbService postThumbService;
    @Resource
    private PostTagService postTagService;


    // IO 型线程池
    private final ExecutorService ioExecutorService = new ThreadPoolExecutor(4, 20, 10, TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(10000));

    // 单帖子获取缓存，key 为 postId，value 为 post
    LoadingCache<Long, Post> postGetCache = Caffeine.newBuilder().expireAfterWrite(12, TimeUnit.HOURS)
            .maximumSize(5_000).build(postId -> postService.getById(postId));

    /**
     * 新增帖子
     *
     * @param post    帖子对象
     * @param request 请求对象
     * @return 帖子ID
     */
    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Long> addPost(@RequestBody PostAddRequest postAddRequest, HttpServletRequest request) {
        // 1. 校验合法性
        if (postAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 新增帖子
        Long result = null;
        result = postService.addPost(postAddRequest, request);

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
     * 查询全部列表信息（仅管理员可查看）
     *
     * @param userId 请求对象
     * @return 帖子列表
     */
    @GetMapping("/list")
    @AuthCheck(mustRole = 1)
    public BaseResponse<List<Post>> listPost(@RequestParam(required = false) String userId) {
        // 1. 判断是否根据用户ID搜索帖子
        if (userId == null) {
            // 1.1 返回所有帖子
            List<Post> list = postService.list();
            return ResultUtil.success(list);
        }
        // 2. 根据ID搜索帖子
        Long Id = Long.valueOf(userId);
        List<Post> list = postService.searchPosts(Id);
        return ResultUtil.success(list);
    }

    /**
     * 获取通过审核的帖子
     *
     * @param request 请求对象
     * @return 通过审核的帖子列表
     */
    @GetMapping("/listByUser")
    public BaseResponse<List<Post>> listPostWithUser(HttpServletRequest request) {
        // 1. 检查登录态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        Users currentObj = (Users) userObj;
        Users user = usersService.getById(currentObj.getId());
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
     * @return 是否删除成功
     */
    @DeleteMapping("/delete/{id}")
    public BaseResponse<Boolean> deletePostByAdmin(@PathVariable Long id, HttpServletRequest request) {
        // 校验参数非空
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 检查权限-仅本人和管理员可删除
        // 2. 获取帖子ID
        Users user = usersService.getLoginUser(request);
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
            //TODO 删贴的时候是否需要减少引用次数？
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
     * @param request 请求对象
     * @return 是否更新成功
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = 1)
    public BaseResponse<Boolean> updatePost(@RequestBody Post post, HttpServletRequest request) {
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
     * @param userId  发帖用户ID
     * @param request 请求对象
     * @return 帖子列表
     */
    @GetMapping("/search/{id}")
    public BaseResponse<List<Post>> searchPosts(@PathVariable String id,
                                                HttpServletRequest request) {
        // 1. 检查登录态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        Users currentObj = (Users) userObj;
        Users user = usersService.getById(currentObj.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 2. 搜索帖子
        Long userId = Long.valueOf(id);
        if (userId <= 0) {
            return ResultUtil.error(ErrorCode.PARAMS_ERROR);
        }
        List<Post> list = postService.searchPosts(userId);
        return ResultUtil.success(list);
    }

    /**
     * 点赞/取消点赞
     *
     * @param postDoThumbRequest 点赞请求对象
     * @param request            请求对象
     * @return 点赞ID
     */
    @PostMapping("/thumb")
    public BaseResponse<Long> postDoThumb(@RequestBody PostDoThumbRequest postDoThumbRequest, HttpServletRequest request) {
        if (postDoThumbRequest == null || postDoThumbRequest.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Users loginUser = (Users) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = loginUser.getId();
        Long postId = postDoThumbRequest.getPostId();
        long result = postThumbService.doThumb(userId, postId);
        if (result != 0) {
            // 移除缓存
            postGetCache.invalidate(postId);
        }
        return ResultUtil.success(result);
    }

}
