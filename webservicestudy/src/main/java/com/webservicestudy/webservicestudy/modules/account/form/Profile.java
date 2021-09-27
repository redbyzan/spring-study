package com.webservicestudy.webservicestudy.modules.account.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Lob;

@Data
public class Profile {

    // 자기 소개
    @Length(max = 35)
    private String bio;

    // 블로그 url
    @Length(max = 50)
    private String url;

    // 직업
    @Length(max = 50)
    private String occupation;

    // 지역
    @Length(max = 50)
    private String location;

    @Lob
    private String profileImage;


}
