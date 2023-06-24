package com.hongjie.konggu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.domain.Post;
import com.hongjie.konggu.model.domain.Users;
import com.hongjie.konggu.model.domain.request.PostAddRequest;
import com.hongjie.konggu.service.PostService;
import com.hongjie.konggu.mapper.PostMapper;
import com.hongjie.konggu.service.UsersService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

import static com.hongjie.konggu.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author WHJ
* @description 针对表【post(帖子表)】的数据库操作Service实现
* @createDate 2023-06-17 16:31:33
*/
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
    implements PostService{
    @Resource
    private PostMapper postMapper;
    @Resource
    private UsersService usersService;

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

    @Override
    public Long addPost(PostAddRequest postAddRequest, HttpServletRequest request) {
        // 1. 检查用户是否登录
        Users loginUser = (Users)request.getSession().getAttribute(USER_LOGIN_STATE);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN,"请先登录");
        }
        // 2. 获取帖子内容和发布用户ID
        Post post = new Post();
        post.setContent(postAddRequest.getContent());
        post.setUserId(loginUser.getId());

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

    @Override
    public List<Post> listByUser() {
        // 只显示通过审核的
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reviewStatus",1);
        queryWrapper.orderByDesc("createTime");
        List<Post> postList = list(queryWrapper);
        postList.stream().map(post -> {
            Users user = usersService.getById(post.getUserId());
            post.setUser(user);
            return null;
        }).collect(Collectors.toList());
        return postList;
    }

    @Override
    public List<Post> searchPosts(Long userId) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("userId",userId);

        List<Post> postList = list(queryWrapper);
        return postList;
    }

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
     * @param content 帖子内容
     * @return 是否包含非法内容
     */
    public static boolean containsProfanity(String content) {
        for (String word : PROFANITY_WORDS) {
            if (content.toLowerCase().contains(word)) {
                return true;
            }
        }
        return false;
    }
}




