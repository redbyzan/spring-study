package com.example.userservice.vo;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RequestUser {

    @NotBlank(message = "email can not null")
    @Size(min = 2,message = "email not be less than two characters")
    private String email;

    @NotBlank(message = "name can not null")
    @Size(min = 2)
    private String name;

    @NotBlank(message = "pwd can not null")
    @Size(min = 8)
    private String pwd;
}
