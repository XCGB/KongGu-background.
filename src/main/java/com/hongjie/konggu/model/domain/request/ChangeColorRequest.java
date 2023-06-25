package com.hongjie.konggu.model.domain.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: WHJ
 * @createTime: 2023-06-25 20:29
 * @description:
 */
@Data
public class ChangeColorRequest implements Serializable {

    private static final long serialVersionUID = 5233931486090832607L;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 标签颜色
     */
    private String tagColor;
}
