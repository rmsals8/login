package com.example.login.service;

import com.example.login.dto.LoginRequestDto;
import com.example.login.dto.LoginResponseDto;
import com.example.login.entity.Log;
import com.example.login.entity.RefreshToken;
import com.example.login.entity.User;
import com.example.login.repository.RefreshTokenRepository;
import com.example.login.repository.UserRepository;
import com.example.login.repository.jpa.JpaUserRepository;
import com.example.login.repository.LogRepository;
import com.example.login.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {
        private final UserRepository userRepository; // MyBatis 구현체
        private final LogRepository logRepository; // JPA 리포지토리
        private final JpaUserRepository jpaUserRepository; // JPA 구현체
        private final RefreshTokenRepository refreshTokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider tokenProvider;

        public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
        public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
        public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);

        @Transactional
        public LoginResponseDto login(LoginRequestDto loginRequest, HttpServletRequest request,
                        HttpServletResponse response) {
                // 1. 사용자명으로 사용자 조회 (MyBatis)
                Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

                if (userOptional.isEmpty()) {
                        // 사용자가 존재하지 않는 경우 로그 기록 (JPA)
                        saveLog(null, "LOGIN_FAIL", "사용자가 존재하지 않음: " + loginRequest.getUsername(),
                                        loginRequest.getIpAddress(), request.getHeader("User-Agent"));

                        throw new BadCredentialsException("Invalid username or password");
                }

                User user = userOptional.get();

                // 2. 비밀번호 검증
                if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                        // 비밀번호 불일치 로그 기록 (JPA)
                        saveLog(user.getUserId(), "LOGIN_FAIL", "비밀번호 불일치: " + loginRequest.getUsername(),
                                        loginRequest.getIpAddress(), request.getHeader("User-Agent"));

                        throw new BadCredentialsException("Invalid username or password");
                }

                // 3. JWT 토큰 생성
                String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);

                // 4. 리프레시 토큰 생성 및 저장 (MyBatis)
                String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
                saveRefreshToken(user.getUserId(), refreshToken);
                addRefreshTokenToCookie(request, response, refreshToken);

                // 5. 로그인 성공 로그 기록 (JPA)
                saveLog(user.getUserId(), "LOGIN_SUCCESS", "로그인 성공: " + loginRequest.getUsername(),
                                loginRequest.getIpAddress(), request.getHeader("User-Agent"));

                // 6. 응답 생성 및 반환
                return LoginResponseDto.builder()
                                .userId(user.getUserId())
                                .username(user.getUsername())
                                .token(accessToken)
                                .build();
        }

        private void saveLog(Long userId, String actionType, String description,
                        String ipAddress, String userAgent) {
                // JPA로 User 엔티티 조회 (필요한 경우)
                User user = null;
                if (userId != null) {
                        user = jpaUserRepository.findById(userId).orElse(null);
                }

                Log log = Log.builder()
                                .user(user)
                                .actionType(actionType)
                                .description(description)
                                .ipAddress(ipAddress)
                                .userAgent(userAgent != null ? userAgent : "Unknown")
                                .status("COMPLETED")
                                .createdAt(LocalDateTime.now())
                                .build();

                logRepository.save(log);
        }

        private void saveRefreshToken(Long userId, String newRefreshToken) {
                RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                                .map(entity -> entity.update(newRefreshToken))
                                .orElse(RefreshToken.builder()
                                                .userId(userId)
                                                .refreshToken(newRefreshToken)
                                                .build());

                refreshTokenRepository.save(refreshToken);
        }

        private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response,
                        String refreshToken) {
                int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();

                Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setMaxAge(cookieMaxAge);

                response.addCookie(cookie);
        }
}