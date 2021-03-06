package com.springsecurity.support.restdocs;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.springsecurity.common.exception.GlobalExceptionHandler;
import com.springsecurity.config.RestDocsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
public class DocsControllerTest {


    MockMvc mockMvc;
    RestDocumentationResultHandler restDocumentationResultHandler;

    @BeforeEach
    void setUp(final RestDocumentationContextProvider provider) {
        this.restDocumentationResultHandler = MockMvcRestDocumentation.document(
                "{class-name}/{method-name}",
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()));

        this.mockMvc = MockMvcBuilders.standaloneSetup(DocsController.class)
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))
                .setControllerAdvice(GlobalExceptionHandler.class)
                .alwaysDo(MockMvcResultHandlers.print())
                .alwaysDo(restDocumentationResultHandler)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }


    /**
     * ?????? ???????????? ?????? ??????????????? ???????????? ????????? ?????? api ?????? ??? ????????? ?????????????????? rest docs ?????? ?????????
     * standalone?????? ????????? ?????? ????????? rest docs ?????? ??????????????? ????????? ????????? ?????? ??????
     */

    @Test
    void ??????_?????????() throws Exception{
        // when then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/oauth2/authorization/{provider}","kakao")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocumentationResultHandler.document(
                        pathParameters(
                                parameterWithName("provider").description("?????? ????????? ?????????, [kakao, naver, google]")
                        ),
                        responseFields(
                                fieldWithPath("tokenType").type(STRING).description("?????? ??????"),
                                fieldWithPath("accessToken").type(STRING).description("?????? ????????? ??? ???????????? access ??????"),
                                fieldWithPath("refreshToken").type(STRING).description("access ?????? ????????? ???????????? ???????????? refresh ??????")
                        )
                ))

        ;
    }

    @Test
    void ??????_??????() throws Exception{
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        SampleRequest sampleRequest = new SampleRequest("backtony");
        String content = objectMapper.writeValueAsString(sampleRequest);
        // when then docs
        mockMvc.perform(RestDocumentationRequestBuilders.get("/docs/error")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(restDocumentationResultHandler.document(
                        responseFields(RestDocsTestSupport.errorDescriptorIncludeErrorFields())
                ));
    }
}
