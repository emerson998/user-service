package com.solutis.dev.application.dto.csv;

import java.util.List;

public record CsvImportResult(int successCount, int errorCount, List<CsvRowError> errors) {
}
