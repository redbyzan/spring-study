package com.apistudy.restapi.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;

// 외부설정으로 기본 유저와 클라이언트 정보 빼내기
// 이걸 사용하는 이유는
// 지금 테스트마다 다 계정을 만들어서 테스트하고 있기 때문에
// 하나로 뽑아서 빈 등록 해주고 이걸로 사용하도록 하는 것임
// properties에 정보를 다 넣어줬고
// appconfig에서 스프링이 뜨면서 이 정보를 가지고 계정을 만들도록 해놨음
@Component
@ConfigurationProperties(prefix = "my-app")
@Getter @Setter
public class AppProperties {

    @NotEmpty
    private String adminUsername;

    @NotEmpty
    private String adminPassword;

    @NotEmpty
    private String userUsername;

    @NotEmpty
    private String userPassword;

    @NotEmpty
    private String clientId;

    @NotEmpty
    private String clientSecret;

}

