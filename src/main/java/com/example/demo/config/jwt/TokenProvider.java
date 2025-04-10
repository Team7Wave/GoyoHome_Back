package com.example.demo.config.jwt;

import com.example.demo.domain.user.User;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest; // resolveToken()을 위해 추가

@RequiredArgsConstructor
@Service
// 계속해서 토큰을 생성하고 올바른 토큰인지 유효성 검사
// 토큰에서 필요한 정보를 가져오는 클래스
public class TokenProvider
{
    private final JwtProperties jwtProperties;

    public String generateToken(User user, Duration expiredAt)
    {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), user);
    }

    //JWT 토큰 생성 메서드
    private String makeToken(Date expiry, User user)
    {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam("typ", "JWT") // 헤더 type: JWT
                // 내용 iss : ahnsujin2022@gmail.com
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now) // 내용 iat: 현재 시간
                .setExpiration(expiry) // 내용 exp: expiry 멤버 변수 값
                .setSubject(user.getEmail()) // 내용 sub: 유저의 이메일
                .claim("id", user.getId()) // 클레임 id: 유저 ID
                // 서명: 비밀값과 함께 해시 값을 HS256 방식으로 암호화
                //.signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey()) 이 코드는 deprecated
                .signWith(Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes()), SignatureAlgorithm.HS256) //Key 객체 방식으로 수정
                .compact();

    }

    // JWT 토큰 유효성 검증 메서드
    public boolean validToken(String token)
    {
        try
        {
            // Jwts.parser() -> Jwts.parserBuilder() 사용으로 변경.
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes())) // Key 객체 방식으로 수정
                    .build()
                    .parseClaimsJws(token);
            return true;

        } catch (Exception e) //복호화 과정에서 에러가 나면 유효하지 않은 토큰
        {
            return false;
        }
    }

    //토큰 기반으로 인증 정보를 가져오는 메서드
    public Authentication getAuthentication(String token)
    {
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        return new UsernamePasswordAuthenticationToken(new org.springframework.security.core.userdetails.User(claims.getSubject(),
                "", authorities), token, authorities);
    }

    // 토큰 기반으로 유저 ID를 가져오는 메서드
    public Long getUserId(String token)
    {
        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }

    private Claims getClaims(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes())) //Key 객체 방식으로 수정
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 요청 헤더에서 토큰을 추출하는 메서드 ("Bearer ...") 형식
    public String resolveToken(HttpServletRequest request)
    {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer "))
        {
            return bearerToken.substring(7).trim(); // "Bearer " 이후 토큰만 추출
        }
        return null;
    }
}
