package com.hongjie.konggu.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: WHJ
 * @createTime: 2023-06-24 15:14
 * @description: 删帖请求
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 7850730750098916183L;

    /**
     * id
     */
    private Long id;

}
