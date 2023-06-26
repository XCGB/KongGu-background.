package com.hongjie.konggu.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hongjie.konggu.model.dto.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.hongjie.konggu.constant.RedisConstants.CACHE_NULL_TTL;
import static com.hongjie.konggu.constant.RedisConstants.LOCK_SHOP_KEY;

/**
 * @author: WHJ
 * @createTime: 2023-06-26 13:54
 * @description:
 */
@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        // 1. 设置逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        // 2. 写入Redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R, ID> R getWithPenetration(String keyPrefix, ID id, Class<R> type,
                                        Function<ID, R> dbFallBack, Long time, TimeUnit unit) {
        // 1. 从redis中读取缓存
        // 1.1 获取key
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2. 判断是否存在(只有存在数据，才返回true)
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }

        // 判断是否为空字符串
        if (json != null) {
            return null;
        }

        // 3. 不存在，根据id查询数据库
        R r = dbFallBack.apply(id);
        if (r == null) {
            // 4. 商户不存在返回空字符串
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }

        // 4. 将信息写入redis
        set(key, r, time, unit);
        return r;
    }

    public <R,ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type,
                                           Function<ID, R> dbFallBack, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1. 从redis中读取缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2. 判断是否存在(只有存在数据，才返回true)
        if (StrUtil.isBlank(json)) {
            // 3. 未命中，返回空
            return null;
        }

        // 4. 命中，将数据反序列化
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(),type);
        LocalDateTime expireTime = redisData.getExpireTime();

        // 5. 判断缓存是否过期
        if (expireTime.isAfter(LocalDateTime.now())){
            // 6. 未过期返回商铺信息
            return r;
        }

        // 7. 过期 缓存重建
        // 7.1 获取互斥锁
        String lock = LOCK_SHOP_KEY + id;
        boolean isLock = getLock(lock);
        // 7.2 判断是否获取成功
        if (isLock){
            // 7.3 成功 开辟独立线程
            CACHE_REBUILD_EXECUTOR.submit(() ->{
                try {
                    // 重建缓存
                    // 查询数据库
                    dbFallBack.apply(id);
                    // 写入缓存
                    setWithLogicalExpire(key,r,time,unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 释放锁
                    releaseLock(lock);
                }
            });
        }
        // 7.4 失败 返回过期的商铺信息
        return r;
    }

    //    public Shop queryWithMutex(Long id) {
//        // 1. 从redis中读取缓存
//        // 1.1 获取key
//        String key = CACHE_SHOP_KEY + id;
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 2. 判断是否存在(只有存在数据，才返回true)
//        if (StrUtil.isNotBlank(shopJson)) {
//            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
//            return shop;
//        }
//
//        // 3. 判断是否为空字符串
//        if (shopJson != null) {
//            return null;
//        }
//
//        // 4. 实现缓存重建
//        // 4.1 获取互斥锁
//        String lock = LOCK_SHOP_KEY + id;
//        Shop shop = null;
//        try {
//            boolean isLock = getLock(lock);
//            if (!isLock) {
//                // 4.2 失败休眠并重试
//                Thread.sleep(50);
//                return queryWithMutex(id);
//            }
//
//            // 4.3. 成功，根据id查询数据库
//            shop = getById(id);
//            Thread.sleep(200);
//            // 5. 不存在则返回空串
//            if (shop == null) {
//                // 5.1 将空值写入redis
//                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
//                return null;
//            }
//
//            // 6. 将商品信息写入redis
//            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            // 7. 释放锁
//            releaseLock(lock);
//        }
//
//        // 8. 返回
//        return shop;
//    }
//
    private boolean getLock(String key) {
        // 获取互斥锁 set key values nx 和 10 秒的有效期，一般为业务逻辑的10倍
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        // 直接返回lock 可能出现拆箱，空指针异常的问题
        return BooleanUtil.isTrue(lock);
    }

    private void releaseLock(String key) {
        stringRedisTemplate.delete(key);
    }

}
