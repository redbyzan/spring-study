package com.example.csvload.disease.presentation;

import com.example.csvload.disease.application.DiseaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {
        DiseaseRestController.class
})
public class DiseaseRestControllerTest {

    @MockBean DiseaseService diseaseService;


    private final String URL = "/api/v1/csv";

    @Autowired MockMvc mockMvc;

    @Test
    void CSV_파일_업로드_성공() throws Exception{
        // given
        MockMultipartFile file = createMockMultipartFile();

        // when then
        mockMvc.perform(MockMvcRequestBuilders.multipart(URL)
                .file(file))
                .andExpect(status().isOk())
        ;
    }

    private MockMultipartFile createMockMultipartFile() {
        return new MockMultipartFile("file",
                "disease.csv",
                "text/csv",
                "부정맥,정의,내과,증상,원인,치료".getBytes());
    }

    @Test
    void CSV_파일이_아닌_경우_업로드_실패() throws Exception{
        // given
        MockMultipartFile file = createMockMultipartFile();

        // when then
        mockMvc.perform(MockMvcRequestBuilders.multipart(URL)
                .file(file))
                .andExpect(status().isOk())
        ;
    }

    private MockMultipartFile createMockMultipartFileNotCsv() {
        return new MockMultipartFile("file",
                "disease.jpg",
                "text/csv",
                "부정맥,정의,내과,증상,원인,치료".getBytes());
    }

    @Test
    void 지정_형식이_아닌_경우_CSV_파일_업로드_실패() throws Exception{
        // given
        MockMultipartFile file = createMockMultipartFile();

        // when then
        mockMvc.perform(MockMvcRequestBuilders.multipart(URL)
                .file(file))
                .andExpect(status().isOk())
        ;
    }

    private MockMultipartFile createMockMultipartFileNotOrdered() {
        return new MockMultipartFile("file",
                "disease.jpg",
                "text/csv",
                "부정맥,정의,내과,증상,원인".getBytes());
    }
}
