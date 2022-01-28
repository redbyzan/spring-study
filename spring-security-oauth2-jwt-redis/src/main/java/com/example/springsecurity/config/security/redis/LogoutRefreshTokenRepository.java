package com.example.springsecurity.config.security.redis;

import org.springframework.data.repository.CrudRepository;

public interface LogoutRefreshTokenRepository extends CrudRepository<LogoutRefreshToken,String> {
}
