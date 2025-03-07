package com.example.login.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.login.dto.LoginRequestDto;
import com.example.login.dto.LoginResponseDto;
import com.example.login.entity.Log;
import com.example.login.entity.User;
import com.example.login.repository.LogRepository;
import com.example.login.repository.UserRepository;
import com.example.login.util.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

        private final UserRepository userRepository;
        private final LogRepository logRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;

        @Transactional
        public LoginResponseDto login(LoginRequestDto loginRequest, HttpServletRequest request) {
                // 1. 사용자명으로 사용자 조회
                Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

                if (userOptional.isEmpty()) {
                        // 사용자가 존재하지 않는 경우 로그 기록
                        saveLog(null, "LOGIN_FAIL", "사용자가 존재하지 않음: " + loginRequest.getUsername(),
                                        loginRequest.getIpAddress(), request.getHeader("User-Agent"));

                        throw new BadCredentialsException("Invalid username or password");
                }

                User user = userOptional.get();

                // 2. 비밀번호 검증
                if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                        // 비밀번호 불일치 로그 기록
                        saveLog(user.getUserId(), "LOGIN_FAIL", "비밀번호 불일치: " + loginRequest.getUsername(),
                                        loginRequest.getIpAddress(), request.getHeader("User-Agent"));

                        throw new BadCredentialsException("Invalid username or password");
                }

                // 3. JWT 토큰 생성
                String token = jwtTokenProvider.createToken(user.getUsername());

                // 4. 로그인 성공 로그 기록
                saveLog(user.getUserId(), "LOGIN_SUCCESS", "로그인 성공: " + loginRequest.getUsername(),
                                loginRequest.getIpAddress(), request.getHeader("User-Agent"));

                // 5. 응답 생성 및 반환
                return LoginResponseDto.builder()
                                .userId(user.getUserId())
                                .username(user.getUsername())
                                .token(token)
                                .build();
        }

        private void saveLog(Long userId, String actionType, String description,
                        String ipAddress, String userAgent) {
                Log log = Log.builder()
                                .user(userId != null ? User.builder().userId(userId).build() : null)
                                .actionType(actionType)
                                .description(description)
                                .ipAddress(ipAddress)
                                .userAgent(userAgent != null ? userAgent : "Unknown")
                                .status("COMPLETED")
                                .createdAt(LocalDateTime.now())
                                .build();

                logRepository.save(log);
        }
}