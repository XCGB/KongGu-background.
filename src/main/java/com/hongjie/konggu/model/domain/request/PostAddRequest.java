package com.hongjie.konggu.model.domain.request;

import com.hongjie.konggu.model.domain.Tag;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author: WHJ
 * @createTime: 2023-06-23 20:11
 * @description: 添加帖子请求类
 */
@Data
public class PostAddRequest  implements Serializable {
    private static final long serialVersionUID = -5166955030779353111L;

    /**
     * 发帖内容
     */
    private String content;

    /**
     * 审核状态 0-待审核 1-通过 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 关联标签列表
     */
    private List<Tag> tags;

}
