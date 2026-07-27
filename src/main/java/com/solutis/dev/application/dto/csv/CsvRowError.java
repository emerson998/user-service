package com.solutis.dev.application.dto.csv;

public record CsvRowError(int rowNumber, String rawLine, String message) {
}
