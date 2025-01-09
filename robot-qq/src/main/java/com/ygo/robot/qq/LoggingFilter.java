package com.ygo.robot.qq;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

    try {
      filterChain.doFilter(wrappedRequest, wrappedResponse);
    } catch (Exception e) {
      logRequestInfo(wrappedRequest, wrappedResponse);
      throw e;
    }

    logRequestInfo(wrappedRequest, wrappedResponse);

    wrappedResponse.copyBodyToResponse();
  }

  private void logRequestInfo(
      ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
    // 请求 URL
    log.info(
        "Request Method: {}, URL: {}", request.getMethod(), request.getRequestURL().toString());

    // 请求参数
    Enumeration<String> parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String paramName = parameterNames.nextElement();
      String[] paramValues = request.getParameterValues(paramName);
      log.info("Request Parameter - {} : {}", paramName, String.join(", ", paramValues));
    }

    // 请求头
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      String headerValue = request.getHeader(headerName);
      log.info("Request Header - {} : {}", headerName, headerValue);
    }

    // 请求 body
    String requestBody = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
    log.info("Request Body: \n{}", requestBody);

    String responseBody = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
    log.info("Response Body: {}", responseBody);
  }
}
