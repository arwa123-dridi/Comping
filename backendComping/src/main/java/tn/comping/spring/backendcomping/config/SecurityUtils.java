package tn.comping.spring.backendcomping.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final JwtUtils jwtUtils;

    public String getCurrentUserId() {
        String token = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes())
                .getRequest()
                .getHeader("Authorization")
                .substring(7);

        return jwtUtils.getIdFromToken(token);
    }

    public String getCurrentUserEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
}
