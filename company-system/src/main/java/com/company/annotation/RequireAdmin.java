package com.company.annotation;

import java.lang.annotation.*;

/**
 * 管理员权限注解 —— 标记在Controller类或方法上，
 * 配合 JwtInterceptor 的 URL 前缀检查实现双重权限控制
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdmin {
}
