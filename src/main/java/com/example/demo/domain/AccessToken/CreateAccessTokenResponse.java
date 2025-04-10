package com.example.demo.domain.AccessToken;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
// 토큰 생성 응답
public class CreateAccessTokenResponse
{
    private String accessToken;
}
