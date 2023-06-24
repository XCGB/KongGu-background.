package com.hongjie.konggu.aop;

import com.hongjie.konggu.annotation.AuthCheck;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.domain.Users;
import com.hongjie.konggu.service.UsersService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @author: WHJ
 * @createTime: 2023-06-23 17:14
 * @description: AOP切面类对拦截的方法进行权限验证
 */
@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UsersService userService;

    /**
     * 切面拦截权限验证
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 1. 获取权限
        int[] anyRole = authCheck.anyRole();
        int mustRole = authCheck.mustRole();

        // 2. 获取当前线程中的请求的属性对象。
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        Users user = userService.getLoginUser(request);

        // 3. 拥有任意权限即通过
        if (anyRole.length > 0) {
            int userRole = user.getUserRole();
            if (Arrays.stream(anyRole).noneMatch(role -> role == userRole)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
        }

        // 4. 必须有所有权限才通过
        if (mustRole != user.getUserRole()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // 通过权限校验，放行
        return joinPoint.proceed();
    }


}
