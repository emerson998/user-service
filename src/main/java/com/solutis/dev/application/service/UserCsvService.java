package com.solutis.dev.application.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.solutis.dev.application.dto.csv.CsvImportResult;
import com.solutis.dev.application.dto.csv.CsvRowError;
import com.solutis.dev.application.dto.user.UserUpsertRequest;
import com.solutis.dev.application.port.in.UserCsvUseCase;
import com.solutis.dev.application.port.in.UserUseCase;
import com.solutis.dev.domain.exception.InvalidFileException;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;

@Service
public class UserCsvService implements UserCsvUseCase {

    private static final String[] EXPORT_HEADER = {"id", "name", "email", "cpf", "phone", "bio", "enabled"};
    private static final String[] IMPORT_HEADER = {"name", "email", "cpf", "phone", "bio", "password"};

    private final UserRepository userRepository;
    private final UserUseCase userUseCase;
    private final long maxFileSizeBytes;

    public UserCsvService(UserRepository userRepository, UserUseCase userUseCase,
            @Value("${app.csv.max-file-size-bytes}") long maxFileSizeBytes) {
        this.userRepository = userRepository;
        this.userUseCase = userUseCase;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportToCsv() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            writer.writeNext(EXPORT_HEADER);
            for (User user : userRepository.findAll()) {
                writer.writeNext(toRow(user));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar CSV de usuários", e);
        }
        return output.toByteArray();
    }

    @Override
    public CsvImportResult importFromCsv(MultipartFile file) {
        validateFile(file);

        int successCount = 0;
        List<CsvRowError> errors = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] header = reader.readNext();
            if (!Arrays.equals(header, IMPORT_HEADER)) {
                throw new InvalidFileException(
                        "Cabeçalho do CSV inválido. Esperado: " + String.join(",", IMPORT_HEADER));
            }
            String[] row;
            int rowNumber = 1;
            while ((row = reader.readNext()) != null) {
                rowNumber++;
                try {
                    userUseCase.upsert(toUpsertRequest(row));
                    successCount++;
                } catch (RuntimeException e) {
                    errors.add(new CsvRowError(rowNumber, String.join(",", row), e.getMessage()));
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Falha ao ler CSV de importação", e);
        }

        return new CsvImportResult(successCount, errors.size(), errors);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("Arquivo CSV vazio");
        }
        if (!hasCsvExtensionOrContentType(file)) {
            throw new InvalidFileException("Arquivo precisa ser um CSV (.csv ou content-type text/csv)");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new InvalidFileException(
                    "Arquivo excede o tamanho máximo permitido de " + maxFileSizeBytes + " bytes");
        }
    }

    private boolean hasCsvExtensionOrContentType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        boolean hasCsvExtension = filename != null && filename.toLowerCase().endsWith(".csv");
        boolean hasCsvContentType = "text/csv".equalsIgnoreCase(file.getContentType());
        return hasCsvExtension || hasCsvContentType;
    }

    private UserUpsertRequest toUpsertRequest(String[] row) {
        String name = row[0];
        String email = row[1];
        String cpf = row[2];
        String phone = row[3];
        String bio = row[4];
        String password = row[5];
        return new UserUpsertRequest(name, email, cpf, password, phone, bio);
    }

    private String[] toRow(User user) {
        return new String[] {
                String.valueOf(user.getId()),
                user.getName(),
                user.getEmail(),
                user.getCpf(),
                user.getPhone(),
                user.getBio(),
                String.valueOf(user.isEnabled())
        };
    }
}
