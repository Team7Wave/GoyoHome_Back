package com.example.demo.domain.Token;

import com.example.demo.domain.RefreshToken.RefreshTokenService;
import com.example.demo.domain.user.User;
import com.example.demo.domain.user.UserService;
import com.example.demo.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
// <<흐름 정리>>
// 리프레시 토큰 -> 유저 ID -> 유저 조회 -> 새 액세스 토큰 발급
public class TokenService
{
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    // 전달받은 리프레시 토큰으로 토큰 유효성 검사를 진행하고,
    // 유효한 토큰일 떄 리프레시 토큰으로 사용자ID를 찾는 메서드.
    public String createNewAccessToken(String refreshToken)
    {
        //토큰 유효성 검사에 실패하면 예외 발생
        if(!tokenProvider.validToken(refreshToken))
        {
            throw new IllegalArgumentException("Unexpected token");
        }

        Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUserId();
        User user = userService.findById(userId);

        // 사용자 ID로 사용자를 찾은 후에 토큰 제공자의 generateToken() 메서드를 호출하여
        // 새로운 액세스 토큰을 생성함.
        return tokenProvider.generateToken(user, Duration.ofHours(2));
    }
}
