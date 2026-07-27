package com.solutis.dev.domain.repository;

import java.util.List;

public record PageResult<T>(List<T> content, long totalElements, int totalPages) {
}
