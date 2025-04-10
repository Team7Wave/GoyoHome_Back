package me.ahnsujin.springbootdeveloper.config.oauth;

import com.example.demo.config.oauth.OAuth2SuccessHandler;
import com.example.demo.repository.RefreshTokenRepository;
import jakarta.servlet.ServletException;
import com.example.demo.domain.user.User;
import com.example.demo.domain.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.config.jwt.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OAuth2SuccessHandlerTest {

    private TokenProvider tokenProvider;
    private RefreshTokenRepository refreshTokenRepository;
    private UserService userService;
    private OAuth2SuccessHandler successHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tokenProvider = mock(TokenProvider.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        userService = mock(UserService.class);
        successHandler = new OAuth2SuccessHandler(tokenProvider, refreshTokenRepository, userService);
        objectMapper = new ObjectMapper();
    }

    @DisplayName("OAuth2SuccessHandler: 로그인 성공 시 accessToken, refreshToken, keepLogin 포함된 JSON을 반환한다")
    @Test
    void onAuthenticationSuccess_returnsTokensAsJson() throws IOException, ServletException
    {
        // given
        User mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .keepLogin(true)
                .build();

        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, authorities);

        when(tokenProvider.generateToken(mockUser, Duration.ofHours(1))).thenReturn("mockedAccessToken");
        when(tokenProvider.generateToken(mockUser, Duration.ofDays(7))).thenReturn("mockedRefreshToken");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        String jsonOutput = response.getContentAsString();
        //Map<String, Object> result = objectMapper.readValue(jsonOutput, Map.class);
        // 컴파일러가 제네릭 타입을 정확히 추론하지 못해서 unchecked or unsafe operations 경고 발생.
        // 그래서 타입 정보를 명확히 지정하는 TypeReference 방식을 사용함.
        Map<String, Object> result = objectMapper.readValue(
                jsonOutput,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
        );


        assertThat(result).containsKeys("accessToken", "refreshToken", "keepLogin");
        assertThat(result.get("accessToken")).isEqualTo("mockedAccessToken");
        assertThat(result.get("refreshToken")).isEqualTo("mockedRefreshToken");
        assertThat(result.get("keepLogin")).isEqualTo(true);
    }
}
