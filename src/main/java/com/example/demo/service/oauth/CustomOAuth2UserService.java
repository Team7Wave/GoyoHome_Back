package com.example.demo.service.oauth;

import com.example.demo.domain.user.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>
{

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException
    {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String provider = userRequest.getClientRegistration().getRegistrationId(); // google
        String providerId = (String) attributes.get("sub"); // 구글의 고유 ID

        // 기존 사용자 조회
        // CustomOAuth2UserService가 구글에서 받아온 사용자 정보를
        // UserRepository를 통해 DB에 저장하고 갱신함.
        User user = userRepository.findByEmail(email)
                .map(existing -> {
                    existing.setName(name); // 이름 업데이트
                    existing.setProvider(provider);
                    existing.setProviderId(providerId);
                    return existing;
                })
                .orElseGet(() -> User.builder()
                        .email(email)
                        .name(name)
                        .provider(provider)
                        .providerId(providerId)
                        .role("USER")
                        .build());

        userRepository.save(user);

        return oAuth2User; // 여기선 기본 oAuth2User 반환, 커스터마이징 가능
    }
}
