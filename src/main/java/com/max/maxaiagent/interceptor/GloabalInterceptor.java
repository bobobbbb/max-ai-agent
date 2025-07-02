package com.max.maxaiagent.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.max.maxaiagent.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
@Slf4j
public class GloabalInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 检查是否登录，如果登录了再设置userId
            if (StpUtil.isLogin()) {
                Long userId = StpUtil.getLoginIdAsLong();
                UserContext.set(userId);
                log.debug("设置用户ID到上下文: {}", userId);
            } else {
                log.debug("用户未登录，跳过设置用户ID");
            }
        } catch (Exception e) {
            log.warn("设置用户上下文失败: {}", e.getMessage());
            // 即使设置失败也继续执行请求
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }

}
