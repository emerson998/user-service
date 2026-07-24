package com.solutis.dev.application.port.in;

import org.springframework.web.multipart.MultipartFile;

import com.solutis.dev.application.dto.csv.CsvImportResult;

public interface UserCsvUseCase {

    byte[] exportToCsv();

    CsvImportResult importFromCsv(MultipartFile file);
}
