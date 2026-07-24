package com.solutis.dev.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.solutis.dev.application.dto.csv.CsvImportResult;
import com.solutis.dev.application.port.in.UserCsvUseCase;
import com.solutis.dev.domain.exception.InvalidFileException;

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

    @Test
    void importCsv_shouldReturn200WithImportResult() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "users.csv", "text/csv",
                "name,email,cpf,phone,bio,password\nAlice,alice@example.com,12345678909,,,password123\n"
                        .getBytes(StandardCharsets.UTF_8));
        when(userCsvUseCase.importFromCsv(any())).thenReturn(new CsvImportResult(1, 0, List.of()));

        mockMvc.perform(multipart("/api/v1/users/csv/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.errorCount").value(0));
    }

    @Test
    void importCsv_shouldReturn422_whenFileIsInvalid() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "users.txt", "text/plain", new byte[0]);
        when(userCsvUseCase.importFromCsv(any()))
                .thenThrow(new InvalidFileException("Arquivo CSV vazio"));

        mockMvc.perform(multipart("/api/v1/users/csv/import").file(file))
                .andExpect(status().is(422));
    }
}
