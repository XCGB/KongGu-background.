package com.hongjie.konggu.controller;

import com.hongjie.konggu.annotation.AuthCheck;
import com.hongjie.konggu.common.BaseResponse;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.common.ResultUtil;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.domain.Tag;
import com.hongjie.konggu.model.domain.Users;
import com.hongjie.konggu.model.dto.UserDTO;
import com.hongjie.konggu.model.request.ChangeColorRequest;
import com.hongjie.konggu.model.request.DeleteRequest;
import com.hongjie.konggu.service.TagService;
import com.hongjie.konggu.service.UsersService;
import com.hongjie.konggu.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.hongjie.konggu.constant.UserConstant.ADMIN_ROLE;

/**
 * @author: WHJ
 * @createTime: 2023-06-23 14:21
 * @description: 标签控制层
 */
@Slf4j
@RestController
@RequestMapping("/tag")
public class TagController {
    @Resource
    private TagService tagService;
    @Resource
    private UsersService usersService;

//    // TagMap 缓存
//    private final Cache<String, Map<String, List<Tag>>> tagMapCache = Caffeine.newBuilder().build();
//
//    // 整个 TagMap 缓存 key
//    private static final String FULL_TAG_MAP_KEY = "uniqueTag";

    /**
     * 获取所有标签
     * @param tagName 标签名
     * @return 标签列表
     */
    @GetMapping("/list")
    public BaseResponse<List<Tag>> getTagList(@RequestParam(required = false) String tagName) {
        // 1. 从线程中获取当前用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 2. 判断是否根据标签名搜索
        List<Tag> list = tagService.getTagList(tagName);
        return ResultUtil.success(list);
    }


    /**
     * 创建标签
     *
     * @param tag     标签内容
     * @param request 请求对象
     * @return 标签ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Long> addTag(@RequestBody Tag tag, HttpServletRequest request) {
        // 1. 判断请求非空
        if (tag == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String tagName = tag.getTagName();
        String tagColor = tag.getTagColor();
        if (StringUtils.isAllBlank(tagName,tagColor)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 获取用户登录态
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 3. 获取用户ID
        tag.setUserId(user.getId());

        boolean result = tagService.save(tag);
        if (!result) {
            throw new BusinessException(ErrorCode.INSERT_ERROR);
        }

        return ResultUtil.success(tag.getId());
    }

    /**
     * 删除标签
     *
     * @param deleteRequest 删除标签ID
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> deleteTag(@RequestBody DeleteRequest deleteRequest) {
        // 1. 判断请求是否合法
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 获取标签ID
        Long tagId = deleteRequest.getId();
        boolean result = tagService.deleteTag(tagId);

        // 3. 清除缓存
//        tagMapCache.invalidate(FULL_TAG_MAP_KEY);
        return ResultUtil.success(result);
    }

    // 更换颜色
    @PostMapping("/changeColor")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> changeColor(@RequestBody ChangeColorRequest changeColorRequest){
        if (changeColorRequest == null || changeColorRequest.getTagId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long tagId = changeColorRequest.getTagId();
        Tag tag = tagService.getById(tagId);
        tag.setTagColor(changeColorRequest.getTagColor());
        boolean b = tagService.updateById(tag);
        if (!b){
            return ResultUtil.error(ErrorCode.INSERT_ERROR);
        }
        return ResultUtil.success(true);

    }


}
