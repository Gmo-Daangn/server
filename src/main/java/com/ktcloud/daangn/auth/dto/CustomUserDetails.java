package com.ktcloud.daangn.auth.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long memberId;
    private final String email;
    private final String password;
    private final Collection<GrantedAuthority> memberRole;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return memberRole;
    }

    @Override
    public @NonNull String getPassword() {
        return password;
    }

    @Override
    public @NonNull String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { //만료 계정 여부
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() { //계정 잠금 여부
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() { // 비밀번호 만료 여부
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() { // 계정 활성화 여부
        return UserDetails.super.isEnabled();
    }
}
