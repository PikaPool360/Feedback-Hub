package com.feedbackhub.dto;

import com.feedbackhub.model.Category;
import com.feedbackhub.model.Feedback;
import java.time.LocalDateTime;

public record FeedbackResponse(
        Long id,
        String name,
        String email,
        Category category,
        int rating,
        String message,
        LocalDateTime createdAt) {

    public static FeedbackResponse from(Feedback f) {
        return new FeedbackResponse(f.getId(), f.getName(), f.getEmail(),
                f.getCategory(), f.getRating(), f.getMessage(), f.getCreatedAt());
    }
}
