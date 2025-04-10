package com.example.demo.controller;

import com.example.demo.domain.AccessToken.CreateAccessTokenRequest;
import com.example.demo.domain.AccessToken.CreateAccessTokenResponse;
import com.example.demo.domain.Token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
// 실제로 요청을 받고 처리할 컨트롤러
public class TokenApiController
{
    private final TokenService tokenService;

    @PostMapping("/api/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken
            (@RequestBody CreateAccessTokenRequest request)
    {
        String newAccessToken = tokenService.createNewAccessToken(request.
                getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateAccessTokenResponse(newAccessToken));
    }
}
