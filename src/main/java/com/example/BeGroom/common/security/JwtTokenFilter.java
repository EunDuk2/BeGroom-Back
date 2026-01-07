package com.example.BeGroom.common.security;

import com.example.BeGroom.auth.domain.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class JwtTokenFilter extends GenericFilter {
    @Value("${jwt.secretKeyAt}")
    private String secretKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            String bearerToken = req.getHeader("Authorization");

            log.info("[DEBUG] Raw Authorization Header: '{}'", bearerToken);

            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                log.info("[JWT] No Token found in header");
                chain.doFilter(request, response);
                return;
            }

            String token = bearerToken.substring(7);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Object memberIdObj = claims.get("memberId");
            if (memberIdObj == null) memberIdObj = claims.get("id"); // "id"로 들어있을 경우 대비

            Long memberId = (memberIdObj instanceof Number) ? ((Number) memberIdObj).longValue() : null;
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            if (memberId != null) {
                UserPrincipal principal = new UserPrincipal(memberId, email);
                List<GrantedAuthority> authorityList = new ArrayList<>();
                if (role != null) {
                    authorityList.add(new SimpleGrantedAuthority("ROLE_" + role));
                }

                Authentication authentication = new UsernamePasswordAuthenticationToken(principal, "", authorityList);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("[JWT] Authentication success: memberId {}", memberId);
            }

        } catch (Exception e) {
            log.error("[JWT ERROR] Token validation failed: {}", e.getMessage());
        }
        chain.doFilter(request, response);
    }
}
