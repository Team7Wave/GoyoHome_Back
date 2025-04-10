package com.example.demo.controller;

import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.config.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class UserController
{

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    // AccessToken을 통해 유저를 식별하고 해당 유저의 RefreshToken을 삭제하는 로그아웃 기능
    @PostMapping("/api/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request)
    {
        String token = tokenProvider.resolveToken(request);
        if (token != null && tokenProvider.validToken(token))
        {
            Long userId = tokenProvider.getUserId(token);
            refreshTokenRepository.findByUserId(userId)
                    .ifPresent(refreshTokenRepository::delete);
        }
        return ResponseEntity.ok().build();
    }

    // AccessToken을 통해 유저를 식별하고 해당 유저의 계정과 모든 연관 데이터를 삭제하는 탈퇴 기능
    @DeleteMapping("/api/user/me")
    public ResponseEntity<Void> withdraw(HttpServletRequest request)
    {
        String token = tokenProvider.resolveToken(request);
        if (token != null && tokenProvider.validToken(token))
        {
            Long userId = tokenProvider.getUserId(token);
            userRepository.deleteById(userId); // ON DELETE CASCADE가 적용된 테이블들까지 함께 삭제됨
        }
        return ResponseEntity.noContent().build();
    }
}
