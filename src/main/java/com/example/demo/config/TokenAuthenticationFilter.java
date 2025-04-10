package com.example.demo.config;

import com.example.demo.config.jwt.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import org.slf4j.Logger; //Logger 선언 추가
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter
{
    private final TokenProvider tokenProvider;
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer";
    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException
    {

        // Options 메서드 또는 인증 예외 경로 처리
        // - 모바일 앱에서 Preflight 요청(특히 CORS) 때문에 OPTIONS 요청이 들어올 수 있음.
        // - 이때 인증 처리를 생략하고 필터 통과시키는 조건을 추가함.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()))
        {
            filterChain.doFilter(request, response);
            return;
        }

        //요청 헤더의 Authorization 키의 값 조회
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        //가져온 값에서 접두사 제거
        String token = getAccessToken(authorizationHeader);
        //가져온 토큰이 유효한지 확인하고, 유효한 때는 인증 정보 설정

        // 토큰이 없는 경우, 추가 검증 없이 다음 필터로 넘김 (Swagger 또는 공용 엔드포인트에 유용)
        // Authorization 헤더가 없는 경우에는 JWT 검증을 건너뛰게 되어 인증이 필요 없는
        // 엔드포인트 접근에는 문제가 발생하지 않음.
        if (token == null)
        {
            filterChain.doFilter(request, response);
            return;
        }

        // 예외 상황에 대한 Logging 또는 방어 처리
        // - tokenProvider.getAuthentication() 과정에서 내부적으로 에러가 발생할 수 있음.
        // - 그래서 try-catch 와 로그를 추가함.
        try
        {
            if (tokenProvider.validToken(token))
            {
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            else
            {
                // 유효하지 않은 토큰일 경우 바로 JSON 응답 반환
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Access token has expired or is invalid\"}");
                return;
            }
        }
        catch (Exception e)
        {
            logger.warn("JWT 인증 중 오류 발생", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Authentication failed due to an internal error\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getAccessToken(String authorizationHeader)
    {
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX))
        {
            return authorizationHeader.substring(TOKEN_PREFIX.length()).trim(); //trim()으로 토큰 파싱 시 공백 제거
        }
        return null;
    }

}
