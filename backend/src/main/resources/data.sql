-- 테스트 사용자 데이터
INSERT INTO users (username, password, email, nickname) 
VALUES ('testuser', '$2a$10$hPmDSuEEFP.u9PiRpgThVOriNh/iRvKwJjmxj7.tPFcvXNI7Hg9lu', 'test@example.com', '테스트유저')
ON DUPLICATE KEY UPDATE username = VALUES(username);
-- 비밀번호: test123