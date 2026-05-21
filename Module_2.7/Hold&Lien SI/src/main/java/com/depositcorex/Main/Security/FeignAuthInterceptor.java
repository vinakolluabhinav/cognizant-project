package com.depositcorex.Main.Security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignAuthInterceptor implements RequestInterceptor {

	// System identity used for scheduler / background jobs
	// Must match a role that downstream services accept
	private static final String SYSTEM_USER_ID = "0";
	private static final String SYSTEM_USER_ROLE = "CORE_ADMIN";

	@Override
	public void apply(RequestTemplate template) {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

		if (attrs != null) {
			// Normal HTTP request — forward the authenticated user's headers
			HttpServletRequest request = attrs.getRequest();
			String role = request.getHeader("X-User-Role");
			String userId = request.getHeader("X-User-Id");
			if (role != null)
				template.header("X-User-Role", role);
			if (userId != null)
				template.header("X-User-Id", userId);
		} else {
			// No request context — this is a scheduler / background thread
			// Inject system-level identity so downstream services accept the call
			template.header("X-User-Role", SYSTEM_USER_ROLE);
			template.header("X-User-Id", SYSTEM_USER_ID);
		}
	}
}
