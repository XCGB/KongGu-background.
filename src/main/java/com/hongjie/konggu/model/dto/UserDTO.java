package com.hongjie.konggu.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author: WHJ
 * @createTime: 2023-06-26 14:33
 * @description: 脱敏用户实体类
 */
@Data
public class UserDTO {
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "用户学号")
    private String userAccount;

    @ApiModelProperty(value = "用户姓名")
    private String username;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "用户头像")
    private String avatar;

    @ApiModelProperty(value = "性别")
    private Integer gender;

    @ApiModelProperty(value = "年级")
    private String grade;

    @ApiModelProperty(value = "学院")
    private String college;

    @ApiModelProperty(value = "专业")
    private String profession;

    @ApiModelProperty(value = "兴趣")
    private String hobby;

    @ApiModelProperty(value = "状态")
    private Integer userStatus;

    @ApiModelProperty(value = "用户角色")
    private Integer userRole;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 是否关注
     */
    @ApiModelProperty(value = "是否关注")
    private Boolean isFollow;

}
