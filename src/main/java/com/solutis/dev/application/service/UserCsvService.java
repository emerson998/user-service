package com.solutis.dev.application.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVWriter;
import com.solutis.dev.application.port.in.UserCsvUseCase;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;

@Service
public class UserCsvService implements UserCsvUseCase {

    private static final String[] HEADER = {"id", "name", "email", "cpf", "phone", "bio", "enabled"};

    private final UserRepository userRepository;

    public UserCsvService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportToCsv() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            writer.writeNext(HEADER);
            for (User user : userRepository.findAll()) {
                writer.writeNext(toRow(user));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar CSV de usuários", e);
        }
        return output.toByteArray();
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
