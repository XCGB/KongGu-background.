package com.hongjie.konggu.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: WHJ
 * @createTime: 2023-06-18 16:07
 * @description:
 */
@Data
public class UserDeleteRequest  implements Serializable {
    private static final long serialVersionUID = 8184842984693162160L;

    /**
     * 用户ID
     */
    private Long id;

}
