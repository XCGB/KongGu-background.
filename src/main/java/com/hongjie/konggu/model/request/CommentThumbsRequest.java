package com.hongjie.konggu.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: WHJ
 * @createTime: 2023-09-06 08:30
 * @description: 评论点赞请求
 */
@Data
public class CommentThumbsRequest implements Serializable {
    /**
     * 串行版本uid
     */
    private static final long serialVersionUID = 935627460548801727L;

    /**
     * 评论Id
     */
    private Long commentId;
}
