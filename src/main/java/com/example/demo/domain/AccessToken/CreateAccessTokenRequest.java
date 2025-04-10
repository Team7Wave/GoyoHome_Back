package com.example.demo.domain.AccessToken;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//토큰 생성 요청
public class CreateAccessTokenRequest
{
    private String refreshToken;
}
