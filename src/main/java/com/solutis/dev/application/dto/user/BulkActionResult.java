package com.solutis.dev.application.dto.user;

import java.util.List;

public record BulkActionResult(int activatedCount, List<Long> skippedIds, boolean dryRun) {
}
