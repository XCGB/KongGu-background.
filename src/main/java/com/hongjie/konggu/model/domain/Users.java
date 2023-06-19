package com.hongjie.konggu.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
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
    private Long id;

    /**
     * 学号（全局唯一）
     */
    private String userAccount;

    /**
     * 用户姓名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 密码（加密存储）
     */
    private String userPassword;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别 0-男 1-女
     */
    private Integer gender;

    /**
     * 入学年级
     */
    private String grade;

    /**
     * 学院
     */
    private String college;

    /**
     * 专业
     */
    private String profession;

    /**
     * 爱好
     */
    private String hobby;

    /**
     * 用户状态 0-正常 1-封号
     */
    private Integer userStatus;

    /**
     * 用户鉴权 0-普通用户 1-高级用户 2-管理用户
     */
    private Integer userRole;

    /**
     * 逻辑删除 0-存在 1-删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}