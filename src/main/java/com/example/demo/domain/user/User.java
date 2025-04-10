package com.example.demo.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // 이게 없으면 builder() 못 씀
public class User implements UserDetails
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 100)
    private String name;

    @Column(length = 20)
    private String provider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Builder.Default //테스트 후 추가
    @Column(length = 20)
    private String role = "USER";

    @Builder.Default //테스트 후 추가
    @Column(name = "location_permission")
    private Boolean locationPermission = false;

    @Builder.Default //테스트 후 추가
    @Column(name = "keep_login")
    private Boolean keepLogin = false;

    @Builder.Default //테스트 후 추가
    @Column(name = "created_at", updatable = false)
    //private LocalDateTime createdAt;
    private LocalDateTime createdAt = LocalDateTime.now(); // @Builder.Default 유지 위해 초기값 명시.

    @Builder.Default //테스트 후 추가
    @Column(name = "updated_at")
    //private LocalDateTime updatedAt;
    private LocalDateTime updatedAt = LocalDateTime.now(); // @Builder.Default 유지 위해 초기값 명시.



    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate()
    {
        this.updatedAt = LocalDateTime.now();
    }

    // Spring Security 관련 메서드 구현부

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override
    public String getUsername()
    {
        return email;
    }

    // OAuth 로그인이라 password 는 사용하지 않을 수도 있음 (null 반환 가능)
    @Override
    public String getPassword()
    {
        return null;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true; // 계정 만료 여부 (true = 만료되지 않음)
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true; // 계정 잠김 여부 (true = 잠기지 않음)
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true; // 비밀번호 만료 여부
    }

    @Override
    public boolean isEnabled()
    {
        return true; // 계정 활성화 여부
    }
}
