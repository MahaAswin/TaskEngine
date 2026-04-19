package com.taskengine.backend.config;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import com.taskengine.backend.api.ApiResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice(basePackages = "com.taskengine.backend.controller")
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    if (returnType.getMethod() == null) {
      return true;
    }
    Class<?> ret = returnType.getMethod().getReturnType();
    if (ret == void.class || ResponseEntity.class.isAssignableFrom(ret)) {
      return false;
    }
    return true;
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (body == null) {
      return ApiResponse.ok(null);
    }
    if (body instanceof ApiResponse) {
      return body;
    }
    if (body instanceof String) {
      return body;
    }
    return ApiResponse.ok(body);
  }
}
