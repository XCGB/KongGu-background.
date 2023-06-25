package com.hongjie.konggu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: WHJ
 * @createTime: 2023-06-23 17:20
 * @description: 权限校验
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 有任何一个角色
     * @return
     */
    int[] anyRole() default {};

    /**
     * 必须有一个角色
     * @return
     */
    int mustRole() default 0;
}