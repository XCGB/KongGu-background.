package com.hongjie.konggu.constant;

/**
 * @author: WHJ
 * @createTime: 2023-06-26 13:57
 * @description:
 */
public interface RedisConstants {
    Long CACHE_NULL_TTL = 2L;

    String  LOGIN_USER_KEY = "login:token:";

    Long LOGIN_USER_TTL = 180L;

    String LOCK_SHOP_KEY = "lock:shop:";

}
