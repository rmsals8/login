package com.example.login.mapper;

import com.example.login.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);

    User findById(@Param("userId") Long userId);

    User findByEmail(@Param("email") String email);

    void save(User user);

    Long getLastInsertId();
}