package com.hongjie.konggu.model.vo;

import com.hongjie.konggu.model.domain.PostComments;
import com.hongjie.konggu.model.dto.UserDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: WHJ
 * @createTime: 2023-09-05 21:02
 * @description: 帖子评论VO
 */
@Data
public class PostCommentsVO extends PostComments implements Serializable {
    /**
     * 串行版本uid
     */
    private static final long serialVersionUID = -2072476628118289151L;

    /**
     * 用户评论
     */
    @ApiModelProperty(value = "评论用户")
    private UserDTO commentUser;


}
