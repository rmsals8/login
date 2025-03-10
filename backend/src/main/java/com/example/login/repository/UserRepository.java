package com.example.login.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.example.login.entity.User;

@Component
public class UserRepository {

    private JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT user_id,username,password FROM users where username =? ";

        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), username);

        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return User.builder()
                    .userId(rs.getLong("user_id"))
                    .username(rs.getString("username"))
                    .password(rs.getString("password"))
                    .build();
        }
    }

    public User save(User user) {
        if (user.getUserId() == null) {
            String sql = "INSERT INTO users (username,password) VALUES (?,?)";
            jdbcTemplate.update(
                    sql,
                    user.getUsername(),
                    user.getPassword());

            String idSql = "SELECT userId from users where username = ?";
            Long id = jdbcTemplate.queryForObject(idSql, Long.class, user.getUsername());
            user.setUserId(id);
        } else {
            String sql = "UPDATE users SET username = ? , password = ? where user_id = ?";
            jdbcTemplate.update(
                    sql,
                    user.getUsername(),
                    user.getPassword(),
                    user.getUserId());
        }
        return user;
    }

}
