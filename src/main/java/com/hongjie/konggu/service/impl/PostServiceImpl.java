package com.hongjie.konggu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.domain.Post;
import com.hongjie.konggu.model.domain.Tag;
import com.hongjie.konggu.model.domain.Users;
import com.hongjie.konggu.model.dto.UserDTO;
import com.hongjie.konggu.model.request.PostAddRequest;
import com.hongjie.konggu.service.PostService;
import com.hongjie.konggu.mapper.PostMapper;
import com.hongjie.konggu.service.PostTagService;
import com.hongjie.konggu.service.UsersService;
import com.hongjie.konggu.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;


/**
* @author WHJ
* @description 针对表【post(帖子表)】的数据库操作Service实现
* @createDate 2023-06-17 16:31:33
*/
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService{
    @Resource
    private UsersService usersService;
    @Resource
    private PostTagService postTagService;

    /**
     * 不雅词汇数组
     */
    private static final String[] PROFANITY_WORDS = {
            "操你妈",
            "滚你妈",
            "吃屎",
            "去死",
            "鸡巴",
    };

    /**
     * 发布帖子
     *
     * @param postAddRequest 帖子信息
     * @return {@link Long}
     */
    @Override
    public Long addPost(PostAddRequest postAddRequest) {
        // 1. 检查用户是否登录
        // 1. 从线程中获取当前用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 2. 获取帖子内容和发布用户ID
        Post post = new Post();
        post.setContent(postAddRequest.getContent());
        post.setUserId(user.getId());

        // 3. 检查帖子是否合法
        validPost(post);

        // 4. 保存帖子
        boolean saveResult = this.save(post);

        // 5. 返回结果
        if(!saveResult){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"帖子信息保存失败");
        }
        return post.getId();
    }

    /**
     * 查询用户帖子（仅管理员）
     *
     * @param userId 用户ID
     * @return {@link List}<{@link Post}>
     */
    @Override
    public List<Post> searchPosts(Long userId) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("userId",userId);

        List<Post> postList = list(queryWrapper);
        if (postList == null) {
            throw new BusinessException(ErrorCode.NO_FOUND_ERROR);
        }
        return postList;
    }

    /**
     * 查询通过审核的帖子
     *
     * @return {@link List}<{@link Post}>
     */
    @Override
    public List<Post> listByUser() {
        // 只显示通过审核的
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reviewStatus",1);
        queryWrapper.orderByDesc("createTime");
        List<Post> postList = list(queryWrapper);

        List<Object> objectList = postList.stream().map(post -> {
            Users user = usersService.getById(post.getUserId());
            Users safetyUser = usersService.getSafetyUser(user);
            post.setUser(safetyUser);
            Long postId = post.getId();
            List<Tag> tagList = postTagService.searchByPostId(postId);
            post.setTagList(tagList);
            return null;
        }).collect(Collectors.toList());

        log.info("STREAM LIST:{}",objectList);
        return postList;
    }

    /**
     * 检查帖子是否合法
     *
     * @param post 帖子
     */
    @Override
    public void validPost(Post post) {
        String content = post.getContent();
        if(StringUtils.isAnyBlank(post.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"帖子内容不能为空");
        }
        if(content.length() > 8192){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"帖子内容过长");
        }
        if(containsProfanity(content)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请文明发言");
        }
    }

    /**
     * 检测是否包含不文明词汇
     *
     * @param content 帖子内容
     * @return 是否包含非法内容
     */
    @Override
    public boolean containsProfanity(String content) {
        for (String word : PROFANITY_WORDS) {
            if (content.toLowerCase().contains(word)) {
                return true;
            }
        }
        return false;
    }
}




