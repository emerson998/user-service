package com.solutis.dev.application.port.in;

import com.solutis.dev.application.dto.user.BulkActionResult;
import com.solutis.dev.application.dto.user.BulkNotificationActivationRequest;

public interface UserBulkNotificationUseCase {

    BulkActionResult activate(BulkNotificationActivationRequest request);
}
