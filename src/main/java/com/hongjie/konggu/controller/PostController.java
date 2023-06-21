package com.hongjie.konggu.controller;

import com.hongjie.konggu.common.BaseResponse;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.common.ResultUtil;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.domain.Post;
import com.hongjie.konggu.model.domain.Users;
import com.hongjie.konggu.service.PostService;
import com.hongjie.konggu.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.hongjie.konggu.constant.UserConstant.ADMIN_ROLE;
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

    /**
     * 新增帖子
     * @param post 帖子对象
     * @param request 请求对象
     * @return 帖子ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> addPost(@RequestBody Post post, HttpServletRequest request){
        if(post == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long result = postService.addPost(post,request);
        return ResultUtil.success(result);
    }

    /**
     * 查询全部列表信息（仅管理员可查看）
     * @param request 请求对象
     * @return 帖子列表
     */
    @GetMapping("/list")
    private BaseResponse<List<Post>> listPost(HttpServletRequest request){
        // 1. 检查权限-仅管理员
        if(!isAdmin(request)){
            throw  new BusinessException(ErrorCode.NO_AUTH);
        }
        List<Post> list = postService.list();
        return ResultUtil.success(list);
    }

    /**
     * 获取通过审核的帖子
     * @param request 请求对象
     * @return 通过审核的帖子列表
     */
    @GetMapping("/listByUser")
    private BaseResponse<List<Post>> listPostWithUser(HttpServletRequest request){
        // 1. 检查登录态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        Users currentObj = (Users) userObj;
        Users user = usersService.getById(currentObj.getId());
        if (user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 2. 获取通过审核的帖子
        List<Post> postList = postService.listByUser();
        return ResultUtil.success(postList);
    }

    /**
     * 删除帖子
     * @param id 帖子ID
     * @param request 请求对象
     * @return 是否删除成功
     */
    @DeleteMapping("/delete/{id}")
    private BaseResponse<Boolean> deletePost(@PathVariable Long id, HttpServletRequest request){
        // 1. 检查权限-仅管理员可删除
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // 2. 获取帖子ID
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 3. 返回删除结果
        boolean result = postService.removeById(id);
        return ResultUtil.success(result);
    }

    /**
     * 更新审核帖子状态
     * @param post 修改的审核状态
     * @param request 请求对象
     * @return 是否更新成功
     */
    @PutMapping("/update")
    private BaseResponse<Boolean> updatePost(@RequestBody Post post,HttpServletRequest request){
        // 1. 检查权限-仅管理员可修改状态
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 2. 校验合法性
        if(post == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(post.getId() <= 0){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = postService.updateById(post);
        return ResultUtil.success(result);
    }

    /**
     * 获取用户鉴权
     * @param request HTTP请求
     * @return 是否有权限
     */
    private boolean isAdmin(HttpServletRequest request) {
        Object userRole = request.getSession().getAttribute(USER_LOGIN_STATE);
        Users user = (Users) userRole;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}
