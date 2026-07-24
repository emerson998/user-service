package com.solutis.dev.web.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.solutis.dev.application.dto.csv.CsvImportResult;
import com.solutis.dev.application.port.in.UserCsvUseCase;
import com.solutis.dev.web.ApiRoutes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiRoutes.V1 + "/users/csv")
@Tag(name = "Users CSV")
public class UserCsvController {

    private final UserCsvUseCase userCsvUseCase;

    public UserCsvController(UserCsvUseCase userCsvUseCase) {
        this.userCsvUseCase = userCsvUseCase;
    }

    @GetMapping("/export")
    @Operation(summary = "Exporta os usuários ativos para CSV")
    public ResponseEntity<byte[]> export() {
        byte[] csv = userCsvUseCase.exportToCsv();
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename("users.csv")
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(csv);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importa usuários a partir de um CSV")
    public ResponseEntity<CsvImportResult> importCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userCsvUseCase.importFromCsv(file));
    }
}
