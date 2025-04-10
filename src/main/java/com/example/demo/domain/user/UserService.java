package com.example.demo.domain.user;

import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
// OAuth 로그인 후 유저를 저장/조회/업데이트 하기 위해 유지
public class UserService
{
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    //토큰 API 구현을 위해 토큰 서비스 추가하기
    //UserService.java 파일을 열어 전달받은 유저 ID로 유저를 검색해서 전달하는 
    //findById()메서드 구현
    public User findById(Long userId)
    {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }

    //OAuth 로그인 시, 이메일로 사용자 정보 검색 (이미 존재하는 유저인지 확인)
    public User findByEmail(String email)
    {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }
}
