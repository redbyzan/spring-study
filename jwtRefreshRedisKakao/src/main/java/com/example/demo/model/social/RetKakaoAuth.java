package com.example.demo.model.social;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RetKakaoAuth {
    private String token_type;
    private String access_token;
    private long expires_in;
    private String refresh_token;
    private long refresh_token_expires_in;
    private String scope;
}
