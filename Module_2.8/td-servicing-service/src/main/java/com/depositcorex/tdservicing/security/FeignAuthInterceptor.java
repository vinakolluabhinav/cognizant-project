package com.depositcorex.tdservicing.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    private static final String SYSTEM_USER_ID   = "0";
    private static final String SYSTEM_USER_ROLE = "CORE_ADMIN";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String role   = request.getHeader("X-User-Role");
            String userId = request.getHeader("X-User-Id");
            if (role   != null) template.header("X-User-Role", role);
            if (userId != null) template.header("X-User-Id",   userId);
        } else {
            template.header("X-User-Role", SYSTEM_USER_ROLE);
            template.header("X-User-Id",   SYSTEM_USER_ID);
        }
    }
}
