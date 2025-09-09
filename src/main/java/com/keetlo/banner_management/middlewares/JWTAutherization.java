package com.keetlo.banner_management.middlewares;

import com.keetlo.banner_management.utils.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;


@Component
public class JWTAutherization {
    private final Authentication authentication;

    public JWTAutherization(Authentication authentication) {
        this.authentication = authentication;
    }

    public String JWTHeaderVerification(HttpServletRequest request){
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            try {
                String userCode = authentication.verifyToken(token);
                if (userCode != null) {
                    return userCode;
                }
            } catch (Exception e) {
                return null;
            }
        } else {
            return  null;
        }
        return null;
    }

}
