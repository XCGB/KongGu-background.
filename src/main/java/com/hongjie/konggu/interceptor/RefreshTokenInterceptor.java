package com.hongjie.konggu.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hongjie.konggu.constant.RedisConstants;
import com.hongjie.konggu.model.dto.UserDTO;
import com.hongjie.konggu.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: WHJ
 * @createTime: 2023-06-26 16:19
 * @description: token刷新拦截器
 */
public class RefreshTokenInterceptor implements HandlerInterceptor {
    private final StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从header中获取token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)){
            return true;
        }

        // 2. 从redis中获取用户
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object, Object> cacheUser = stringRedisTemplate.opsForHash().entries(tokenKey);

        // 3. 判断用户是否存在
        if (cacheUser.isEmpty()){
            return true;
        }

        // 4. 将HashMap类型的User转换成UserDTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(cacheUser, new UserDTO(), false);

        // 5. 保存用户到ThreadLocal中
        UserHolder.saveUser(userDTO);

        // 6. 刷新token有效期
        stringRedisTemplate.expire(tokenKey, 30L, TimeUnit.MINUTES);

        // 7. 放行
        return true;
    }

}

