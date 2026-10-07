package com.feedbackhub.exception;

import java.time.Instant;
import java.util.Map;

/** Consistent error body returned for every failed request. */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> details) {
}
