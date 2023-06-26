package com.hongjie.konggu.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: WHJ
 * @createTime: 2023-06-26 13:55
 * @description: Redis数据格式实体类
 */
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
