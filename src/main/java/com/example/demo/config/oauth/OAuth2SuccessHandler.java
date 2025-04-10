package com.example.demo.config.oauth;

import com.example.demo.domain.RefreshToken.RefreshToken;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.domain.user.User;
import com.example.demo.domain.user.UserService;
import com.example.demo.config.jwt.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler
{
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException
    {
        // OAuth 인증이 완료된 사용자 객체 가져오기
        User user = (User) authentication.getPrincipal();

        // AccessToken: 1시간, RefreshToken: 7일 유효
        String accessToken = tokenProvider.generateToken(user, Duration.ofHours(1));
        String refreshToken = tokenProvider.generateToken(user, Duration.ofDays(7));

        // RefreshToken을 DB에 저장하거나 갱신 (기존 있으면 update)
        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        existingToken -> existingToken.update(refreshToken, LocalDateTime.now().plusDays(7)),
                        () -> refreshTokenRepository.save(
                                new RefreshToken(user.getId(), refreshToken, LocalDateTime.now().plusDays(7)))
                );

        // 프론트에서 로그인 유지 여부를 판단할 수 있도록 keepLogin도 함께 응답에 포함
        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("accessToken", accessToken);
        tokenResponse.put("refreshToken", refreshToken);
        tokenResponse.put("keepLogin", user.getKeepLogin()); // A/B 시나리오 분기를 위해 필요

        // JSON 응답 설정 및 전송
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        new ObjectMapper().writeValue(response.getOutputStream(), tokenResponse);
    }
}
