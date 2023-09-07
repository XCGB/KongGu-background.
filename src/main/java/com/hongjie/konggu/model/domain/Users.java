package com.hongjie.konggu.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户表
 * @TableName users
 */
@TableName(value ="users")
@Data
public class Users implements Serializable {
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 学号（全局唯一）
     */
    @ApiModelProperty(value = "学号")
    private String userAccount;

    /**
     * 用户姓名
     */
    @ApiModelProperty(value = "用户姓名")
    private String username;

    /**
     * 昵称
     */
    @ApiModelProperty(value = "昵称")
    private String nickname;

    /**
     * 密码（加密存储）
     */
    @ApiModelProperty(value = "密码")
    private String userPassword;

    /**
     * 头像
     */
    @ApiModelProperty(value = "头像")
    private String avatar;

    /**
     * 性别 0-男 1-女
     */
    @ApiModelProperty(value = "性别")
    private Integer gender;

    /**
     * 入学年级
     */
    @ApiModelProperty(value = "入学年级")
    private String grade;

    /**
     * 学院
     */
    @ApiModelProperty(value = "学院")
    private String college;

    /**
     * 专业
     */
    @ApiModelProperty(value = "专业")
    private String profession;

    /**
     * 爱好
     */
    @ApiModelProperty(value = "爱好")
    private String hobby;

    /**
     * 用户状态 0-正常 1-封号
     */
    @ApiModelProperty(value = "用户状态")
    private Integer userStatus;

    /**
     * 用户鉴权 0-普通用户 1-高级用户 2-管理用户
     */
    @ApiModelProperty(value = "用户鉴权")
    private Integer userRole;

    /**
     * 逻辑删除 0-存在 1-删除
     */
    @TableLogic
    @ApiModelProperty(value = "逻辑删除")
    private Integer isDelete;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}