package com.hongjie.konggu.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 帖子表
 * @TableName post
 */
@TableName(value ="post")
@Data
public class Post implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发帖用户ID
     */
    private Long userId;

    /**
     * 发帖内容
     */
    private String content;

    /**
     * 审核状态 0-待审核 1-通过 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 评论数
     */
    private Integer commentNum;

    /**
     * 逻辑删除 
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
    private Users user;

    @TableField(exist = false)
    private List<Tag> tagList;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}