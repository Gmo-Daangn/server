package com.ktcloud.daangn.auth.service;

import com.ktcloud.daangn.auth.dto.AuthSignupRequestDto;

public interface AuthService {
    String signup(AuthSignupRequestDto dto);
}
