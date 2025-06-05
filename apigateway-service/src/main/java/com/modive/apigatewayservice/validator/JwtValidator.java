package com.modive.apigatewayservice.validator;

import com.modive.apigatewayservice.dto.AccessTokenDto;
import com.modive.apigatewayservice.entity.MemberRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtValidator {

    @Value("${spring.jwt.issuer}")
    private String issuer;

    @Value("${spring.jwt.secret}")
    private String accessToken;

    public AccessTokenDto retrieveAccessToken(String accessTokenValue) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .requireIssuer(issuer)
                    .setSigningKey(Keys.hmacShaKeyFor(accessToken.getBytes()))
                    .build()
                    .parseClaimsJws(accessTokenValue);

            // memberId를 subject나 커스텀 클레임으로 사용하는지 확인
                // subject에 없으면 커스텀 클레임에서 시도
            }

            // 클레임에서 role 가져오기, 없을 경우 처리
            String role = claims.getBody().get("role", String.class);

        } catch (Exception e) {
            throw new JwtException("JWT 검증 실패: " + e.getMessage(), e);
        }
    }
}