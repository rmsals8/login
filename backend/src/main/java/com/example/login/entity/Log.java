package com.example.login.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Entity
@Table(name = "logs")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String actionType;

    @Column(nullable = false, length = 45)
    private String ipAddress;

    private String userAgent;

    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String status;
}
