package com.example.springsecurity.config.security.redis;

import org.springframework.data.repository.CrudRepository;

public interface LogoutAccessTokenRepository extends CrudRepository<LogoutAccessToken,String> {
}
