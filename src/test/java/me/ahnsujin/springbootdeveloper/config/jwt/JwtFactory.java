package me.ahnsujin.springbootdeveloper.config.jwt;
// TokenProvider 가 잘 돌아가는지 확인하기 위해 테스트 코드 작성
// 이 파일은 JWT 토큰 서비스를 테스트 하는 데 사용할 모킹(mocking)용 객체
// 테스트용 토큰 생성을 위한 유틸 클래스.

import com.example.demo.config.jwt.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static java.util.Collections.emptyMap;

@Getter
public class JwtFactory
{
    private String subject = "test@email.com";
    private Date issuedAt = new Date();
    private Date expiration = new Date(new Date().getTime() + Duration.ofDays(14).toMillis());
    private Map<String, Object> claims = emptyMap();

    //빌더 패턴을 사용해 설정이 필요한 데이터만 선택 설정
    @Builder
    public JwtFactory(String subject, Date issuedAt, Date expiration,
                      Map<String, Object> claims)
    {
        this.subject = subject != null ? subject : this.subject;
        this.issuedAt = issuedAt != null ? issuedAt : this.issuedAt;
        this.expiration = expiration != null ? expiration : this.expiration;
        this.claims = claims != null ? claims : this.claims;
    }

    public static JwtFactory withDefaultValues()
    {
        return JwtFactory.builder().build();
    }

    /*
    <<별도로 JWT 를 만들어야 하는 이유>>
    - OAuth는 '누군지 확인' 하는 단계. 구글이 인증해줬다는 사실 확인 후,
    - 인증된 사용자 정보를 바탕으로 JWT(access token 역할)을 만들어서 프론트엔드에 전달.
    - JWT에서 secret key가 필요한 이유: signWith(secretKey)를 통해 토큰의 위변조 방지가 가능함.
      (프론트는 이 토큰을 매 요청마다 보내고, 백엔드는 secret key로 복호화해 사용자 인증을 수행함)

    - 구글 OAuth와 secret key는 무관함.
    - 생성한 JWT에는 반드시 secret key가 필요함.
    - TokenProvider와 JwtFactory는 고요홈 어플만의 인증시스템을 구성하는 도구임.
     */

    //jjwt 라이브러리를 사용해 JWT 토큰 생성
    public String createToken(JwtProperties jwtProperties)
    {
        return Jwts.builder()
                .setSubject(subject)
                .setHeaderParam("typ", "JWT")
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .addClaims(claims)
                // jjwt 0.11.x 기준으로 deprecated 된 방식.
                //.signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .signWith(Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes()), SignatureAlgorithm.HS256) // 수정
                .compact();
    }
}
