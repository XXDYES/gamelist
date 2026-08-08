package org.example.gamelist.interceptor;
import org.example.gamelist.common.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.gamelist.common.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    throws Exception{
        String path = request.getRequestURI();
        System.out.println("🔍 拦截器检查路径：" + path);
        // 1. 从请求头中获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录，缺少Token\"}");
            return false;
        }
        // 2. 提取 Token（去掉 "Bearer " 前缀）
        String token = authHeader.substring(7);
        // 3. 校验 Token
        if (!JwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Token无效或已过期\"}");
            return false;
        }
        // 4. 解析 Token，获取用户信息
        Integer userId = JwtUtil.getUserIdFromToken(token);
        String username = JwtUtil.getUsernameFromToken(token);

        // 5. 将用户信息存入当前线程上下文
        UserContext.setCurrentUser(userId, username);

        // 6. 放行
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清除用户信息，防止内存泄漏
        UserContext.clear();
    }

}
