package com.taskengine.backend.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents required task permission for a service operation. Enforcement is done via
 * {@link TaskPermissionEvaluator}.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskPermission {

  TaskPermissionAction value();
}
