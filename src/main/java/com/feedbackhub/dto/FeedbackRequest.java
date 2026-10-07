package com.feedbackhub.dto;

import com.feedbackhub.model.Category;
import jakarta.validation.constraints.*;

/** Incoming payload for creating or updating feedback. Validation runs before the service is called. */
public record FeedbackRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @NotNull Category category,
        @Min(1) @Max(5) int rating,
        @NotBlank @Size(max = 2000) String message) {
}
