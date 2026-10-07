package com.feedbackhub.dto;

import java.util.Map;

public record FeedbackSummary(
        long totalCount,
        double averageRating,
        Map<String, Long> countByCategory,
        Map<Integer, Long> countByRating) {
}
