package com.apistudy.restapi.common;

import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;


//restdocs를 좀더 깔끔하게 해주기 위해 만든 config
// test에서만 사용하는 config라고 알려주는 애노테이션
@TestConfiguration
public class RestDocsConfiguration {

    // docs의 json을 포맷팅해서 이쁘게 정렬하도록 하는 것
    @Bean
    public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer(){
        return configurer -> configurer.operationPreprocessors()
                .withRequestDefaults(prettyPrint())
                .withResponseDefaults(prettyPrint());
    }
}
