package com.ktcloud.daangn.auth.jwt;

import com.ktcloud.daangn.auth.dto.AuthLoginRequestDto;
import com.ktcloud.daangn.auth.dto.TokenResponseDto;
import com.ktcloud.daangn.config.dto.BaseResponse;
import com.ktcloud.daangn.config.exception.InvalidInputException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginFilter(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider){
        this.authenticationManager = authenticationManager;
        this.objectMapper = new ObjectMapper();
        this.jwtTokenProvider = jwtTokenProvider;
        super.setFilterProcessesUrl("/api/v1/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        AuthLoginRequestDto dto;
        try {
            dto = objectMapper.readValue(request.getInputStream(), AuthLoginRequestDto.class);
            validateLoginRequest(dto);
        } catch (IOException e) {
            throw new AuthenticationServiceException("요청을 읽을 수 없습니다.");
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.password(), null);

        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException{
        TokenResponseDto tokenDto = jwtTokenProvider.createToken(authResult);
        BaseResponse<TokenResponseDto> baseResponse = BaseResponse.success(tokenDto);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8);

        objectMapper.writeValue(response.getWriter(), baseResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException{
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8);

        BaseResponse<String> failedResponse = BaseResponse.fail(HttpStatus.UNAUTHORIZED.value(), "로그인 실패", "이메일 혹은 비밀번호 오류입니다.");

        objectMapper.writeValue(response.getWriter(), failedResponse);
    }

    private void validateLoginRequest(AuthLoginRequestDto dto) {
        if (dto.email() == null || dto.email().isBlank()){
            throw new AuthenticationServiceException("이메일은 필수입니다.");
        }
        if (dto.password() == null || dto.password().isBlank()) {
            throw new AuthenticationServiceException("비밀번호는 필수입니다.");
        }
    }
}
