package com.likelion.mindiary.global.Security.JWT;

import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {

    private SecretKey secretKey;

    // @Value : application.yml에서의 특정한 변수 데이터를 가져올 수 있음
    // string key는 jwt에서 사용 안하므로 객체 키 생성!
    // "${spring.jwt.secret}" : application.yml에 저장된 spring: jwt: secret 에 저장된 암호화 키 사용
    public JWTUtil(@Value("${jwt.secret}") String secret) {

        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    // loginId 반환 메서드
    public String getLoginId(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("loginId", String.class);
    }

    // role 반환 메서드
    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    // 토큰이 소멸 (유효기간 만료) 하였는지 검증 메서드
    public boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }


    // refresh토큰 생성
    public String createRefreshToken(Long expiredMs) {
        return Jwts.builder()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    // access토큰 생성
    public String createAccessToken(String loginId, String role, Long expiredMs) {
        return Jwts.builder()
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                .claim("loginId", loginId)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

}
