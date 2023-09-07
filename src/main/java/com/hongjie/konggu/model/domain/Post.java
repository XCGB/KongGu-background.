package com.hongjie.konggu.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "主键ID")
    private Long id;

    /**
     * 发帖用户ID
     */
    @ApiModelProperty(value = "用户ID")
    private Long userId;

    /**
     * 发帖内容
     */
    @ApiModelProperty(value = "发帖内容")
    private String content;

    /**
     * 审核状态 0-待审核 1-通过 2-拒绝
     */
    @ApiModelProperty(value = "审核状态")
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    @ApiModelProperty(value = "审核信息")
    private String reviewMessage;

    /**
     * 点赞数
     */
    @ApiModelProperty(value = "点赞数")
    private Integer thumbNum;

    /**
     * 浏览数
     */
    @ApiModelProperty(value = "评论数")
    private Integer commentsNum;

    /**
     * 逻辑删除 
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
    @ApiModelProperty(value = "创建用户信息")
    private Users user;

    @TableField(exist = false)
    @ApiModelProperty(value = "标签列表")
    private List<Tag> tagList;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}