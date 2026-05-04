package com.ktcloud.daangn.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Token;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (!StringUtils.hasText(token)) {
            log.error("토큰 정보 알 수 없음");
            filterChain.doFilter(request,response);
            return;
        }

        if (!jwtTokenProvider.validateToken(token)) {
            log.error("유효하지 않은 토큰");
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = jwtTokenProvider.authentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request,response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        String result = null;

        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer")) {
            result = authorization.substring(7);
        }
        return result;
    }
}
