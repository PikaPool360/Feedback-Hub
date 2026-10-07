package com.feedbackhub.repository;

import com.feedbackhub.model.Category;
import com.feedbackhub.model.Feedback;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Page<Feedback> findByCategory(Category category, Pageable pageable);

    Page<Feedback> findByRatingGreaterThanEqual(int minRating, Pageable pageable);

    Page<Feedback> findByCategoryAndRatingGreaterThanEqual(Category category, int minRating, Pageable pageable);

    @Query("select avg(f.rating) from Feedback f")
    Double findAverageRating();

    @Query("select f.category, count(f) from Feedback f group by f.category")
    List<Object[]> countGroupedByCategory();

    @Query("select f.rating, count(f) from Feedback f group by f.rating")
    List<Object[]> countGroupedByRating();
}
