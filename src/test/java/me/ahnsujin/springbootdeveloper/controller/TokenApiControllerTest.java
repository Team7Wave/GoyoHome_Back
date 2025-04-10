package me.ahnsujin.springbootdeveloper.controller;

// TokenApiController.java 에서
// 토큰을 생성하는 메서드인 createNewAccessToken()메서드에 대한
// 테스트 코드

import com.example.demo.domain.AccessToken.CreateAccessTokenRequest;
import com.example.demo.domain.RefreshToken.RefreshToken;
import com.example.demo.domain.user.User;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.config.jwt.JwtProperties;
import me.ahnsujin.springbootdeveloper.config.jwt.JwtFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath; // jsonPath 올바르게 작성 주의하기.
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TokenApiControllerTest
{
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    JwtProperties jwtProperties;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    public void mockMvcSetup()
    {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        userRepository.deleteAll();
    }

    @DisplayName("createNewAccessToken: 새로운 액세스 토큰을 발급한다.")
    @Test
    public void createNewAccessToken() throws Exception
    {
        //given
        final String url = "/api/token";

        User testUser = userRepository.save(User.builder()
                .email("user@gmail.com")
                //.password("test") OAuth전용 서비스에서 password 필요없음
                .build());

        String refreshToken = JwtFactory.builder()
                .claims(Map.of("id", testUser.getId()))
                .build()
                .createToken(jwtProperties);

        refreshTokenRepository.save(new RefreshToken(
                testUser.getId(),
                refreshToken,
                LocalDateTime.now().plusDays(7) // 일단 7일 후 만료된다고 설정
        ));

        CreateAccessTokenRequest request = new CreateAccessTokenRequest();
        request.setRefreshToken(refreshToken);
        final String requestBody = objectMapper.writeValueAsString(request);

        //when
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        //then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }
}
