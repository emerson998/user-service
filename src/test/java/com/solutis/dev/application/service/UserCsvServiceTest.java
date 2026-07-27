package com.solutis.dev.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.solutis.dev.application.dto.csv.CsvImportResult;
import com.solutis.dev.application.dto.csv.CsvRowError;
import com.solutis.dev.application.dto.user.UserResponse;
import com.solutis.dev.application.dto.user.UserUpsertRequest;
import com.solutis.dev.application.port.in.UserUseCase;
import com.solutis.dev.domain.exception.DuplicateResourceException;
import com.solutis.dev.domain.exception.InvalidFileException;
import com.solutis.dev.domain.model.Role;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserCsvServiceTest {

    private static final long MAX_FILE_SIZE_BYTES = 5_242_880L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserUseCase userUseCase;

    private UserCsvService userCsvService;

    @BeforeEach
    void setUp() {
        userCsvService = new UserCsvService(userRepository, userUseCase, MAX_FILE_SIZE_BYTES);
    }

    @Test
    void exportToCsv_shouldWriteHeaderAndOneRowPerUser() throws IOException, CsvValidationException {
        User user = new User(1L, "Alice", "alice@example.com", "12345678909", "hashed",
                "+55 11 99999-0000", "bio", true, null, false, Role.USER);
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
                null, null, true, null, false, Role.USER);
        when(userRepository.findAll()).thenReturn(List.of(user));

        String csv = new String(userCsvService.exportToCsv(), StandardCharsets.UTF_8);

        assertThat(csv).doesNotContain("super-secret-hash");
    }

    @Test
    void importFromCsv_shouldUpsertOneUserPerCsvRow_forMixedRows() throws IOException {
        String csv = "name,email,cpf,phone,bio,password\n"
                + "Alice,alice@example.com,12345678909,+55 11 90000-0000,bio-alice,password123\n"
                + "Bob,bob@example.com,98765432100,,,\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "users.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        when(userUseCase.upsert(any(UserUpsertRequest.class)))
                .thenReturn(new UserResponse(1L, "Alice", "alice@example.com", "12345678909", null, null, true, false, Role.USER))
                .thenReturn(new UserResponse(2L, "Bob", "bob@example.com", "98765432100", null, null, true, false, Role.USER));

        CsvImportResult result = userCsvService.importFromCsv(file);

        assertThat(result.successCount()).isEqualTo(2);
        assertThat(result.errorCount()).isZero();
        assertThat(result.errors()).isEmpty();

        ArgumentCaptor<UserUpsertRequest> captor = ArgumentCaptor.forClass(UserUpsertRequest.class);
        verify(userUseCase, times(2)).upsert(captor.capture());
        List<UserUpsertRequest> requests = captor.getAllValues();
        assertThat(requests.get(0)).isEqualTo(new UserUpsertRequest(
                "Alice", "alice@example.com", "12345678909", "password123", "+55 11 90000-0000", "bio-alice"));
        assertThat(requests.get(1).name()).isEqualTo("Bob");
        assertThat(requests.get(1).password()).isEmpty();
    }

    @Test
    void importFromCsv_shouldSkipFailingRowsAndContinueProcessing_whenSomeRowsHaveDuplicateEmail() throws IOException {
        StringBuilder csv = new StringBuilder("name,email,cpf,phone,bio,password\n");
        for (int i = 1; i <= 10; i++) {
            csv.append("User").append(i).append(",user").append(i).append("@example.com,")
                    .append(String.format("%011d", i)).append(",,,password123\n");
        }
        MockMultipartFile file = new MockMultipartFile(
                "file", "users.csv", "text/csv", csv.toString().getBytes(StandardCharsets.UTF_8));

        when(userUseCase.upsert(any(UserUpsertRequest.class))).thenAnswer(invocation -> {
            UserUpsertRequest request = invocation.getArgument(0);
            if (request.email().equals("user3@example.com") || request.email().equals("user7@example.com")) {
                throw new DuplicateResourceException("Usuário com e-mail " + request.email() + " já existe");
            }
            return new UserResponse(1L, request.name(), request.email(), request.cpf(), null, null, true, false, Role.USER);
        });

        CsvImportResult result = userCsvService.importFromCsv(file);

        assertThat(result.successCount()).isEqualTo(8);
        assertThat(result.errorCount()).isEqualTo(2);
        assertThat(result.errors()).hasSize(2);
        assertThat(result.errors()).extracting(CsvRowError::rowNumber).containsExactly(4, 8);
        assertThat(result.errors()).allSatisfy(error -> assertThat(error.message()).contains("já existe"));

        verify(userUseCase, times(10)).upsert(any(UserUpsertRequest.class));
    }

    @Test
    void importFromCsv_shouldThrowInvalidFileException_whenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "users.csv", "text/csv", new byte[0]);

        assertThatThrownBy(() -> userCsvService.importFromCsv(file))
                .isInstanceOf(InvalidFileException.class);

        verify(userUseCase, never()).upsert(any());
    }

    @Test
    void importFromCsv_shouldThrowInvalidFileException_whenExtensionAndContentTypeAreNotCsv() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "users.txt", "text/plain", "name,email,cpf,phone,bio,password\n".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> userCsvService.importFromCsv(file))
                .isInstanceOf(InvalidFileException.class);

        verify(userUseCase, never()).upsert(any());
    }

    @Test
    void importFromCsv_shouldThrowInvalidFileException_whenFileExceedsMaxSize() {
        UserCsvService serviceWithTinyLimit = new UserCsvService(userRepository, userUseCase, 10L);
        MockMultipartFile file = new MockMultipartFile("file", "users.csv", "text/csv",
                "name,email,cpf,phone,bio,password\nAlice,alice@example.com,12345678909,,,password123\n"
                        .getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> serviceWithTinyLimit.importFromCsv(file))
                .isInstanceOf(InvalidFileException.class);

        verify(userUseCase, never()).upsert(any());
    }

    @Test
    void importFromCsv_shouldThrowInvalidFileException_whenHeaderDoesNotMatch() {
        MockMultipartFile file = new MockMultipartFile("file", "users.csv", "text/csv",
                "nome,email,cpf\nAlice,alice@example.com,12345678909\n".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> userCsvService.importFromCsv(file))
                .isInstanceOf(InvalidFileException.class);

        verify(userUseCase, never()).upsert(any());
    }
}
