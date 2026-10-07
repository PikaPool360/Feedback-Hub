package com.feedbackhub.service;

import com.feedbackhub.dto.FeedbackRequest;
import com.feedbackhub.dto.FeedbackResponse;
import com.feedbackhub.dto.FeedbackSummary;
import com.feedbackhub.dto.PageResponse;
import com.feedbackhub.exception.NotFoundException;
import com.feedbackhub.model.Category;
import com.feedbackhub.model.Feedback;
import com.feedbackhub.repository.FeedbackRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private final FeedbackRepository repository;

    public FeedbackService(FeedbackRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FeedbackResponse create(FeedbackRequest request) {
        Feedback feedback = new Feedback();
        apply(feedback, request);
        return FeedbackResponse.from(repository.save(feedback));
    }

    @Transactional(readOnly = true)
    public PageResponse<FeedbackResponse> list(Category category, Integer minRating, Pageable pageable) {
        Page<Feedback> page;
        if (category != null && minRating != null) {
            page = repository.findByCategoryAndRatingGreaterThanEqual(category, minRating, pageable);
        } else if (category != null) {
            page = repository.findByCategory(category, pageable);
        } else if (minRating != null) {
            page = repository.findByRatingGreaterThanEqual(minRating, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return PageResponse.from(page.map(FeedbackResponse::from));
    }

    @Transactional(readOnly = true)
    public FeedbackResponse get(Long id) {
        return FeedbackResponse.from(find(id));
    }

    @Transactional
    public FeedbackResponse update(Long id, FeedbackRequest request) {
        Feedback feedback = find(id);
        apply(feedback, request);
        return FeedbackResponse.from(repository.save(feedback));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    @Transactional(readOnly = true)
    public FeedbackSummary summary() {
        Double avg = repository.findAverageRating();
        double average = avg == null ? 0.0 : Math.round(avg * 100.0) / 100.0;

        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (Category c : Category.values()) {
            byCategory.put(c.name(), 0L);
        }
        for (Object[] row : repository.countGroupedByCategory()) {
            byCategory.put(((Category) row[0]).name(), (Long) row[1]);
        }

        Map<Integer, Long> byRating = new LinkedHashMap<>();
        for (int r = 1; r <= 5; r++) {
            byRating.put(r, 0L);
        }
        for (Object[] row : repository.countGroupedByRating()) {
            byRating.put((Integer) row[0], (Long) row[1]);
        }

        return new FeedbackSummary(repository.count(), average, byCategory, byRating);
    }

    private Feedback find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Feedback with id " + id + " not found"));
    }

    private void apply(Feedback feedback, FeedbackRequest request) {
        feedback.setName(request.name().trim());
        feedback.setEmail(request.email().trim());
        feedback.setCategory(request.category());
        feedback.setRating(request.rating());
        feedback.setMessage(request.message().trim());
    }
}
