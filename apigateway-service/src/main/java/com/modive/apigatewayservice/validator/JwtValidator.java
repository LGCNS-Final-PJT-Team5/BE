package com.modive.apigatewayservice.validator;

import com.modive.apigatewayservice.dto.AccessTokenDto;
import com.modive.apigatewayservice.entity.MemberRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
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
            String userIdStr = claims.getBody().getSubject();
            if (userIdStr == null) {
                // subject에 없으면 커스텀 클레임에서 시도
                userIdStr = claims.getBody().get("userId", String.class);
            }

            if (userIdStr == null) {
                throw new JwtException("User ID not found in token");
            }

            // UUID 변환 (기존 Long 값도 처리)
            UUID userId = convertToUUID(userIdStr);

            // 클레임에서 role 가져오기, 없을 경우 처리
            String role = claims.getBody().get("role", String.class);
            MemberRole memberRole = (role != null) ? MemberRole.valueOf(role) : MemberRole.USER;

            log.info("JWT validation successful - userId: {}, role: {}", userId, memberRole);

            return new AccessTokenDto(userId, memberRole, accessTokenValue);
        } catch (Exception e) {
            log.error("JWT validation failed", e);
            throw new JwtException("JWT 검증 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 문자열을 UUID로 변환 (기존 Long 값도 처리)
     *
     * @param userIdStr 변환할 문자열
     * @return UUID 객체
     * @throws JwtException 변환 실패 시
     */
    private UUID convertToUUID(String userIdStr) {
        System.out.println("Converting userIdStr: " + userIdStr); // 추가
        try {
            UUID result = UUID.fromString(userIdStr);
            System.out.println("Successfully converted to UUID: " + result); // 추가
            return result;
        } catch (IllegalArgumentException e) {
            System.out.println("Not UUID format, trying Long conversion"); // 추가
            try {
                Long longValue = Long.parseLong(userIdStr);
                log.warn("Converting legacy Long userId {} to UUID format", longValue);

                String uuidString = String.format("00000000-0000-0000-0000-%012d", longValue);
                System.out.println("Generated UUID string: " + uuidString); // 추가
                return UUID.fromString(uuidString);
            } catch (NumberFormatException nfe) {
                log.error("Unable to convert userId '{}' to UUID or Long", userIdStr);
                throw new JwtException("Invalid userId format: " + userIdStr);
            }
        }
    }
}