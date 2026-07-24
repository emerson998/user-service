package com.solutis.dev.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserCsvServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserCsvService userCsvService;

    @Test
    void exportToCsv_shouldWriteHeaderAndOneRowPerUser() throws IOException, CsvValidationException {
        User user = new User(1L, "Alice", "alice@example.com", "12345678909", "hashed",
                "+55 11 99999-0000", "bio", true, null);
        when(userRepository.findAll()).thenReturn(List.of(user));

        byte[] csv = userCsvService.exportToCsv();

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(new ByteArrayInputStream(csv), StandardCharsets.UTF_8))) {
            assertThat(reader.readNext())
                    .containsExactly("id", "name", "email", "cpf", "phone", "bio", "enabled");
            assertThat(reader.readNext())
                    .containsExactly("1", "Alice", "alice@example.com", "12345678909",
                            "+55 11 99999-0000", "bio", "true");
            assertThat(reader.readNext()).isNull();
        }
    }

    @Test
    void exportToCsv_shouldReturnHeaderOnly_whenNoUsersExist() throws IOException, CsvValidationException {
        when(userRepository.findAll()).thenReturn(List.of());

        byte[] csv = userCsvService.exportToCsv();

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(new ByteArrayInputStream(csv), StandardCharsets.UTF_8))) {
            assertThat(reader.readNext())
                    .containsExactly("id", "name", "email", "cpf", "phone", "bio", "enabled");
            assertThat(reader.readNext()).isNull();
        }
    }

    @Test
    void exportToCsv_shouldNeverIncludePasswordHash() {
        User user = new User(1L, "Alice", "alice@example.com", "12345678909", "super-secret-hash",
                null, null, true, null);
        when(userRepository.findAll()).thenReturn(List.of(user));

        String csv = new String(userCsvService.exportToCsv(), StandardCharsets.UTF_8);

        assertThat(csv).doesNotContain("super-secret-hash");
    }
}
