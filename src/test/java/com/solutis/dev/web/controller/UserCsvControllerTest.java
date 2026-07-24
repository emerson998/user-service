package com.solutis.dev.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.solutis.dev.application.port.in.UserCsvUseCase;

@WebMvcTest(UserCsvController.class)
class UserCsvControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCsvUseCase userCsvUseCase;

    @Test
    void export_shouldReturn200WithCsvContentTypeAndAttachmentHeader() throws Exception {
        byte[] csvBytes = "id,name,email,cpf,phone,bio,enabled\n1,Alice,alice@example.com,12345678909,,,true\n"
                .getBytes(StandardCharsets.UTF_8);
        when(userCsvUseCase.exportToCsv()).thenReturn(csvBytes);

        mockMvc.perform(get("/api/v1/users/csv/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("text/csv")))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"users.csv\""))
                .andExpect(content().bytes(csvBytes));
    }
}
