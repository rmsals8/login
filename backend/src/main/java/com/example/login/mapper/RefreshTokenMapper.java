package com.example.login.mapper;

import com.example.login.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenMapper {
    RefreshToken findByUserId(@Param("userId") Long userId);

    RefreshToken findByRefreshToken(@Param("refreshToken") String refreshToken);

    void save(RefreshToken refreshToken);

    void update(RefreshToken refreshToken);

    void deleteByUserId(@Param("userId") Long userId);
}