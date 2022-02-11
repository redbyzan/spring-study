package com.example.userservice.vo;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RequestLogin {

    @NotBlank(message = "email cannot be null")
    @Size(min = 2)
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;
}
