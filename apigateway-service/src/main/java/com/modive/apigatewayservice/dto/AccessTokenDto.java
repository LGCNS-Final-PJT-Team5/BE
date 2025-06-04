package com.modive.apigatewayservice.dto;

import com.modive.apigatewayservice.entity.MemberRole;

import java.util.UUID;

public record AccessTokenDto(UUID userId, MemberRole role, String accessTokenValue) {
    public static AccessTokenDto of(UUID userId, MemberRole role, String accessTokenValue) {
        return new AccessTokenDto(userId, role, accessTokenValue);
    }
}

