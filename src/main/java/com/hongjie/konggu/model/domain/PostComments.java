package com.hongjie.konggu.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 帖子评论表
 * @TableName post_comments
 */
@TableName(value ="post_comments")
@Data
public class PostComments implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 评论用户ID
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;

    /**
     * 帖子ID
     */
    @ApiModelProperty(value = "帖子id")
    private Long postId;

    /**
     * 评论内容
     */
    @ApiModelProperty(value = "评论内容")
    private String content;

    /**
     * 点赞数
     */
    @ApiModelProperty(value = "点赞数")
    private Integer thumbNum;

    /**
     * 状态 0：正常，1：被举报，2：禁止查看
     */
    @ApiModelProperty(value = "状态")
    private Integer status;

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