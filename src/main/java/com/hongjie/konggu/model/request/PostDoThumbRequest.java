package com.hongjie.konggu.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: WHJ
 * @createTime: 2023-06-23 11:05
 * @description: 帖子点赞请求
 */
@Data
public class PostDoThumbRequest implements Serializable {
    private static final long serialVersionUID = -1937264635564994698L;

    /**
     * 帖子id
     */
    private long postId;
}
