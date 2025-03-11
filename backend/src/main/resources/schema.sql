-- 필요시 기존 테이블 삭제
-- DROP TABLE IF EXISTS refresh_tokens;
-- DROP TABLE IF EXISTS logs;
-- DROP TABLE IF EXISTS users;

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    nickname VARCHAR(50)
);

-- 리프레시 토큰 테이블
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    refresh_token VARCHAR(500) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 로그 테이블
CREATE TABLE IF NOT EXISTS logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action_type VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    status VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);