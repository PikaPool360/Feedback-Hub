package com.feedbackhub.controller;

import com.feedbackhub.dto.FeedbackRequest;
import com.feedbackhub.dto.FeedbackResponse;
import com.feedbackhub.dto.FeedbackSummary;
import com.feedbackhub.dto.PageResponse;
import com.feedbackhub.model.Category;
import com.feedbackhub.service.FeedbackService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
@Validated
public class FeedbackController {

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FeedbackResponse> create(@Valid @RequestBody FeedbackRequest request) {
        FeedbackResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/feedback/" + created.id())).body(created);
    }

    @GetMapping
    public PageResponse<FeedbackResponse> list(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @Min(1) @Max(5) Integer minRating,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(category, minRating, pageable);
    }

    @GetMapping("/summary")
    public FeedbackSummary summary() {
        return service.summary();
    }

    @GetMapping("/{id}")
    public FeedbackResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public FeedbackResponse update(@PathVariable Long id, @Valid @RequestBody FeedbackRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
