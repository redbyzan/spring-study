package com.example.restdocs.member;

import com.example.restdocs.document.utils.RestDocsTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static com.example.restdocs.document.utils.RestDocsConfig.field;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest
//@AutoConfigureMockMvc
////@ExtendWith(RestDocumentationExtension.class)
//@AutoConfigureRestDocs
class MemberControllerTest extends RestDocsTestSupport {

    @Test
    public void member_get() throws Exception {
        // 조회 API -> 대상의 데이터가 있어야 합니다.
        mockMvc.perform(
                get("/api/members/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(
                        restDocs.document(
                                pathParameters(
                                        parameterWithName("id").description("Member ID")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("ID"),
                                        fieldWithPath("age").description("age"),
                                        fieldWithPath("email").description("email")
                                )
                        )
                )
        ;
    }

    @Test
    public void member_page_test() throws Exception {
        mockMvc.perform(
                get("/api/members")
                        .param("size", "10")
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andDo(
                        restDocs.document(
                                requestParameters(
                                        parameterWithName("size").optional().description("size"),
                                        parameterWithName("page").optional().description("page")
                                )
                        )
                )
        ;
    }


    @Test
    public void member_create() throws Exception {
        MemberSignUpRequest dto = MemberSignUpRequest.builder()
                .age(1)
                .email("hhh@naver.com")
                .status(MemberStatus.BAN)
                .build();
        mockMvc.perform(
                post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isOk())
                .andDo(
                        restDocs.document(
                                requestFields(
                                        fieldWithPath("age").description("age").attributes(field("constraints", "길이 10 이하")),
                                        fieldWithPath("email").description("email").attributes(field("constraints", "길이 30 이하")),
                                        fieldWithPath("status").description("Code Member Status 참조")
                                )
                        )
                )
        ;
    }

    @Test
    public void member_modify() throws Exception {
        MemberModificationRequest dto = MemberModificationRequest.builder()
                .age(1)
                .build();
        mockMvc.perform(
                put("/api/members/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))

        )
                .andExpect(status().isOk())
                .andDo(
                        restDocs.document(
                                pathParameters(
                                        parameterWithName("id").description("Member ID")
                                ),
                                requestFields(
                                        fieldWithPath("age").description("age")
                                )
                        )
                )
        ;
    }


}