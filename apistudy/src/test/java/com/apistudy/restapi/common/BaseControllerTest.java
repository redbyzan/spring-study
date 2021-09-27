package com.apistudy.restapi.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// 테스트의 중복 코드를 간소화 -> 상속을 이용


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class) // docs 포맷팅 하기 위해서 만든 config import
@ActiveProfiles("test") // resource를 application-test 를 사용하게 된다 // todo 이거 아직 테스트 안해봄
@Disabled // 테스트를 가지고 있는 클래스로 간주되지 않도록 무시하는 애노테이션
public class BaseControllerTest {

    // protected로 외부 사용 막기
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;
}
