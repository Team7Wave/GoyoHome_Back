package com.example.demo.domain.RefreshToken;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "token", nullable = false, columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "created_at", updatable = false, insertable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    public RefreshToken(Long userId, String refreshToken, LocalDateTime expiredAt)
    {
        this.userId = userId;
        this.refreshToken = refreshToken;
        this.expiredAt = expiredAt;
    }

    public RefreshToken update(String newRefreshToken, LocalDateTime newExpiredAt)
    {
        this.refreshToken = newRefreshToken;
        this.expiredAt = newExpiredAt;
        return this;
    }
}
