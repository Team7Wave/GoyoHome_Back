package me.ahnsujin.springbootdeveloper.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest
{

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private WebApplicationContext context;
    @Autowired private JwtProperties jwtProperties;
    @Autowired private UserRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp()
    {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("로그아웃: RefreshToken이 정상적으로 삭제된다")
    @Test
    void logout_success() throws Exception
    {
        // given
        User testUser = userRepository.save(User.builder()
                .email("logout-test@example.com")
                .build());

        String token = JwtFactory.builder()
                .claims(Map.of("id", testUser.getId()))
                .build()
                .createToken(jwtProperties);

        refreshTokenRepository.save(new RefreshToken(
                testUser.getId(), token, LocalDateTime.now().plusDays(7)));

        // when
        ResultActions result = mockMvc.perform(post("/api/logout")
                .header("Authorization", "Bearer " + token));

        // then
        result.andExpect(status().isOk());
    }

    @DisplayName("회원 탈퇴: 유저가 정상적으로 삭제된다")
    @Test
    void withdraw_success() throws Exception
    {
        // given
        User testUser = userRepository.save(User.builder()
                .email("withdraw-test@example.com")
                .build());

        String token = JwtFactory.builder()
                .claims(Map.of("id", testUser.getId()))
                .build()
                .createToken(jwtProperties);

        // when
        ResultActions result = mockMvc.perform(delete("/api/user/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNoContent());
    }
}
