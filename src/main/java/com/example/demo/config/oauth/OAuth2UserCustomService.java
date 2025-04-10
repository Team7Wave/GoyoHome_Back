package com.example.demo.config.oauth;

import com.example.demo.domain.user.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService
{
    private final UserRepository userRepository;

    // 리소스 서버에서 보내주는 사용자 정보를 불러오는 메서드인 loadUser()를 통해 사용자를 조회함.
    // 부모 클래스인 DefaultOAuth2UserService 의 OAuth 서비스에서 제공하는 정보를 기반으로
    // 유저 객체를 만들어주는 loadUser() 메서드를 사용해 사용자 객체를 불러옴.
    // users 테이블에 사용자 정보가 있다면 이름을 업데이트 함.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException
    {
        OAuth2User user = super.loadUser(userRequest);
        saveOrUpdate(user, userRequest);
        return user;
    }

    // users 테이블 필드 매핑 정보(불러오는 사용자 객체)
// ┌────────────────────────────┬──────────────────────────────┬────────────────────────────┐
// │ DB 필드명                   │ 설명                         │ 처리 방식                   │
// ├────────────────────────────┼──────────────────────────────┼────────────────────────────┤
// │ id                         │ 기본키 (auto_increment)      │ JPA @Id + @GeneratedValue  │
// │ email                      │ 사용자 이메일                │ attributes.get("email")    │
// │ name                       │ 사용자 이름                  │ attributes.get("name")     │
// │ provider                   │ OAuth 제공자 (google 등)     │ userRequest.getClientRegistration().getRegistrationId() |
// │ provider_id                │ OAuth 고유 ID (sub)          │ attributes.get("sub")      │
// │ role                       │ 권한 (기본값: USER)          │ @Builder.Default 로 자동   │
// │ location_permission        │ 위치 권한 여부 (false)       │ @Builder.Default 로 자동   │
// │ keep_login                 │ 로그인 유지 여부 (false)     │ @Builder.Default 로 자동   │
// │ created_at / updated_at    │ 생성/수정 시간               │ @PrePersist / @PreUpdate   │
// └────────────────────────────┴──────────────────────────────┴────────────────────────────┘


    // users 테이블에 사용자 정보가 없다면 saveOrUpdate() 메서드를 실행해 users 테이블에 회원 데이터를 추가함.
    // 사용자가 users 테이블에 있으면 업데이트하고 없으면 사용자를 새로 생성해서 데이터베이스에 저장함.
    private User saveOrUpdate(OAuth2User oAuth2User, OAuth2UserRequest userRequest)
    {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String provider = userRequest.getClientRegistration().getRegistrationId(); // ex: "google"
        String providerId = (String) attributes.get("sub"); // Google의 고유 식별자

        // 유저가 존재하면 이름 업데이트
        User user = userRepository.findByEmail(email)
                .map(entity -> {
                    entity.setName(name); // 따로 update() 메서드 대신 setName 사용
                    return entity;
                })
                .orElse(
                        User.builder()
                                .email(email)
                                .name(name)
                                .provider(provider)
                                .providerId(providerId)
                                .build()
                );

        return userRepository.save(user);
    }
}

