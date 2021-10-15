package com.example.restdocs.document;

import com.example.restdocs.document.utils.CustomResponseFieldsSnippet;
import com.example.restdocs.document.utils.RestDocsTestSupport;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class EnumDocControllerTest extends RestDocsTestSupport {

    @Test
    public void sampleRequest() throws Exception {
        EnumDocController.SampleRequest sampleRequest = new EnumDocController.SampleRequest("name","hhh.naver");
        mockMvc.perform(
                post("/test/error")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest))
        )
                .andExpect(status().isBadRequest())
                .andDo(
                        restDocs.document(
                                responseFields(
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("status").description("Http Status Code"),
                                        fieldWithPath("code").description("Error Code"),
                                        fieldWithPath("timestamp").description("에러 발생 시간"),
                                        fieldWithPath("errors").description("Error 값 배열 값"),
                                        fieldWithPath("errors[0].field").description("문제 있는 필드"),
                                        fieldWithPath("errors[0].value").description("문제가 있는 값"),
                                        fieldWithPath("errors[0].reason").description("문재가 있는 이유")
                                )
                        )
                )
        ;
    }

    @Test
    public void commons() throws Exception {
        ResultActions result = this.mockMvc.perform(
                get("/test/docs")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        MvcResult mvcResult = result.andReturn();
        EnumDocs enumDocs = getData(mvcResult);

        result.andExpect(status().isOk())
                .andDo(restDocs.document(
                        customResponseFields("custom-response", beneathPath("data.memberStatus").withSubsectionId("memberStatus"), // (2)
                                attributes(key("title").value("memberStatus")),
                                enumConvertFieldDescriptor((enumDocs.getMemberStatus()))
                        ),
                        customResponseFields("custom-response", beneathPath("data.sex").withSubsectionId("sex"), // (2)
                                attributes(key("title").value("sex")),
                                enumConvertFieldDescriptor((enumDocs.getSex()))
                        )
                ));
    }

    // 커스텀 템플릿 사용을 위한 함수
    public static CustomResponseFieldsSnippet customResponseFields
                                (String type,
                                 PayloadSubsectionExtractor<?> subsectionExtractor,
                                 Map<String, Object> attributes, FieldDescriptor... descriptors) {
        return new CustomResponseFieldsSnippet(type, subsectionExtractor, Arrays.asList(descriptors), attributes
                , true);
    }

    // Map으로 넘어온 enumValue를 fieldWithPath로 변경하여 리턴
    private static FieldDescriptor[] enumConvertFieldDescriptor(Map<String, String> enumValues) {
        return enumValues.entrySet().stream()
                .map(x -> fieldWithPath(x.getKey()).description(x.getValue()))
                .toArray(FieldDescriptor[]::new);
    }

    // mvc result 데이터 파싱
    private EnumDocs getData(MvcResult result) throws IOException {
        ApiResponseDto<EnumDocs> apiResponseDto = objectMapper
                                                .readValue(result.getResponse().getContentAsByteArray(),
                                                new TypeReference<ApiResponseDto<EnumDocs>>() {}
                                                );
        return apiResponseDto.getData();
    }


}
