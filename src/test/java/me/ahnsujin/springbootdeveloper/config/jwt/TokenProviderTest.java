package me.ahnsujin.springbootdeveloper.config.jwt;

import com.example.demo.domain.user.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.config.jwt.JwtProperties;
import com.example.demo.config.jwt.TokenProvider;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.security.Keys; //Key 객체 사용으로 인해 추가함

@SpringBootTest
public class TokenProviderTest
{

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProperties jwtProperties;

    
    @DisplayName("generateToken(): 유저 정보와 만료 기간을 전달해 토큰을 만들 수 있다.")
    @Test
    public void generateToken()
    {
        // given
        User testUser = userRepository.save(User.builder()
                .email("user@gmail.com")
                //.password("test") OAuth 로그인이라 password는 사용 안함
                .build());

        // when
        String token = tokenProvider.generateToken(testUser, Duration.ofDays(14));

        // then
        /*
        Long userId = Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .get("id", Long.class);
        */

        // Jwts.parser() -> Jwts.parserBuilder() 사용으로 변경.
        Long userId = Jwts.parserBuilder()
                //.setSigningKey(jwtProperties.getSecretKey()) 이 코드는 deprecated
                .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes())) // Key 객체 방식으로 수정
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", Long.class);


        assertThat(userId).isEqualTo(testUser.getId());
    }

    @DisplayName("validToken(): 만료된 토큰인 때에 유효성 검증에 실패한다.")
    @Test
    public void validToken_invalidToken()
    {
        // given
        String token = JwtFactory.builder()
                .expiration(new Date(System.currentTimeMillis() - Duration.ofDays(7).toMillis()))
                .build()
                .createToken(jwtProperties);

        // when
        boolean result = tokenProvider.validToken(token);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("validToken(): 유효한 토큰인 때에 유효성 검증에 성공한다.")
    @Test
    public void validToken_validToken()
    {
        // given
        String token = JwtFactory.withDefaultValues()
                .createToken(jwtProperties);

        // when
        boolean result = tokenProvider.validToken(token);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("getAuthentication(): 토큰 기반으로 인증 정보를 가져올 수 있다.")
    @Test
    public void getAuthentication()
    {
        // given
        String userEmail = "user@email.com";
        String token = JwtFactory.builder()
                .subject(userEmail)
                .build()
                .createToken(jwtProperties);

        // when
        Authentication authentication = tokenProvider.getAuthentication(token);

        // then
        assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).isEqualTo(userEmail);
    }

    @DisplayName("getUserId(): 토큰으로 유저 ID를 가져올 수 있다.")
    @Test
    public void getUserId()
    {
        // given
        Long userId = 1L;
        String token = JwtFactory.builder()
                .claims(Map.of("id", userId))
                .build()
                .createToken(jwtProperties);

        // when
        Long userIdByToken = tokenProvider.getUserId(token);

        // then
        assertThat(userIdByToken).isEqualTo(userId);
    }
}
