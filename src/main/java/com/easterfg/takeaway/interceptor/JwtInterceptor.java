package com.easterfg.takeaway.interceptor;

import com.easterfg.takeaway.exception.AccessDeniedException;
import com.easterfg.takeaway.utils.security.Authorize;
import com.easterfg.takeaway.utils.security.JwtUtil;
import com.easterfg.takeaway.utils.security.Role;
import com.easterfg.takeaway.utils.security.UserContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author EasterFG on 2022/9/27
 */
@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    /**
     * @param request  请求
     * @param response 响应
     * @param handler  拦截处理器
     * @return 是否被拦截
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 获取注解
            // 方法注解优先级 > 类注解优先级
            Authorize authorize = handlerMethod.getMethodAnnotation(Authorize.class);
            if (authorize == null) {
                // 在类上面寻找注解
                authorize = handlerMethod.getBeanType().getAnnotation(Authorize.class);
            }

            if (authorize == null) {
                return true;
            }

            String token = request.getHeader(jwtUtil.getHeader());
            if (!StringUtils.hasLength(token)) {
                throw new MalformedJwtException("token is null or blank");
            }
            // 抛出异常通过统一异常处理
            Claims claims = jwtUtil.getClaimsByToken(token);
            // 获取角色
            Role[] roles = authorize.value();
            Object r = claims.get("role");
            log.info("access role is {}", r);
            if (!(r instanceof List)) {
                throw new AccessDeniedException("服务器繁忙, 请稍后再试");
            }
            List<String> role = (List<String>) r;
            for (Role item : roles) {
                if (item.equals(Role.ALL) || role.contains(item.toString())) {
                    UserContext.User user = new UserContext.User(
                            claims.get("id", Long.class),
                            claims.get("username", String.class),
                            claims.get("name", String.class),
                            role);
                    UserContext.setUser(user);
                    return true;
                }
            }
            //
            throw new AccessDeniedException("无访问权限");
        }
        return true;
    }
}
