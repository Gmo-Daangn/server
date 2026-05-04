package com.ktcloud.daangn.auth.jwt;

import com.ktcloud.daangn.auth.dto.CustomUserDetails;
import com.ktcloud.daangn.auth.dto.TokenResponseDto;
import com.ktcloud.daangn.config.ResultCode;
import com.ktcloud.daangn.config.exception.InvalidInputException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final Long ACCESS_EXPIRATION_MILLISECOND = 1000L * 60 * 30;

    private final SecretKey accessKey;

    public JwtTokenProvider(@Value("${jwt.access_secret}") String accessSecret) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
    }

    /**
     * 토큰 생성
     */
    public TokenResponseDto createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date accessExpiration = new Date(now.getTime() + ACCESS_EXPIRATION_MILLISECOND);

        String accessToken = Jwts
                .builder()
                .subject(authentication.getName())
                .claim("auth", authorities)
                .claim("memberId", ((CustomUserDetails) Objects.requireNonNull(authentication.getPrincipal())).getMemberId())
                .issuedAt(now)
                .expiration(accessExpiration)
                .signWith(accessKey, Jwts.SIG.HS256)
                .compact();
        return new TokenResponseDto("Bearer", accessToken);
    }

    /**
     * 토큰 정보 추출
     */
    public Authentication authentication(String token) {
        Claims claims = getClaims(token);

        String auth = Optional.ofNullable(claims.get("auth", String.class)).orElseThrow(() -> new AuthenticationServiceException("잘못된 토큰 정보입니다."));
        Long memberId = Optional.ofNullable(claims.get("memberId", Long.class)).map(id -> Long.parseLong(id.toString()))
                .orElseThrow(() -> new AuthenticationServiceException("잘못된 토큰 정보입니다."));

        Collection<GrantedAuthority> authorities = Arrays.stream(auth.split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        UserDetails principal = new CustomUserDetails(memberId, claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal,"", authorities);
    }

    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 토큰: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 토큰: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("토큰이 비어있음: {}", e.getMessage());
        }
        return false;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
