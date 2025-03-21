package com.example.login.repository;

import com.example.login.entity.Log;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findByUser_UserId(Long userId);

    List<Log> findByActionType(String actionType);

    List<Log> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Log> findByUser_UserIdAndActionType(Long userId, String actionType);

    // Log save(Log log);
}
