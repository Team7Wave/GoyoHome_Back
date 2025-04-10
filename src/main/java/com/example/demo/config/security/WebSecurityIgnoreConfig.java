package com.example.demo.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class WebSecurityIgnoreConfig
{
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer()
    {
        return (web) -> web.ignoring()
                .requestMatchers(
                        // 보안 검사에서 제외되는 정적 리소스 경로들
                        new AntPathRequestMatcher("/img/**"),
                        new AntPathRequestMatcher("/css/**"),
                        new AntPathRequestMatcher("/js/**"),
                        // Swagger 및 기타 공개 API 관련 경로를 보안 필터 체인에서 제외하기
                        new AntPathRequestMatcher("/swagger-ui.html"),
                        new AntPathRequestMatcher("/v3/api-docs/**"),
                        new AntPathRequestMatcher("/swagger-ui/**")
                );
    }
}
