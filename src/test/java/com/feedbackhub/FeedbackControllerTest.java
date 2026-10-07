package com.feedbackhub;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.feedbackhub.repository.FeedbackRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeedbackRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private static String json(String email, String category, int rating, String message) {
        return """
                {"name":"Asha","email":"%s","category":"%s","rating":%d,"message":"%s"}
                """.formatted(email, category, rating, message);
    }

    private long createOne(String category, int rating) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("asha@example.com", category, rating, "Works well")))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    @Test
    void createWithValidData_returns201AndLocation() throws Exception {
        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("asha@example.com", "BUG", 4, "Button is misaligned")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.category").value("BUG"))
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    void createWithInvalidEmail_returns400WithFieldDetails() throws Exception {
        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("not-an-email", "BUG", 4, "Hello")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void createWithRatingOutOfRange_returns400() throws Exception {
        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("asha@example.com", "BUG", 9, "Hello")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.rating").exists());
    }

    @Test
    void createWithUnknownCategory_returns400() throws Exception {
        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("asha@example.com", "NONSENSE", 3, "Hello")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUnknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/feedback/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listFiltersByCategory() throws Exception {
        createOne("BUG", 2);
        createOne("GENERAL", 5);

        mockMvc.perform(get("/api/feedback").param("category", "BUG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].category").value("BUG"));
    }

    @Test
    void listFiltersByMinimumRating() throws Exception {
        createOne("BUG", 2);
        createOne("GENERAL", 5);

        mockMvc.perform(get("/api/feedback").param("minRating", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].rating").value(5));
    }

    @Test
    void listWithInvalidMinRating_returns400() throws Exception {
        mockMvc.perform(get("/api/feedback").param("minRating", "7"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateChangesFields() throws Exception {
        long id = createOne("GENERAL", 3);

        mockMvc.perform(put("/api/feedback/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("asha@example.com", "PRAISE", 5, "Much better now")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("PRAISE"))
                .andExpect(jsonPath("$.message").value("Much better now"));
    }

    @Test
    void deleteRemovesFeedback() throws Exception {
        long id = createOne("GENERAL", 3);

        mockMvc.perform(delete("/api/feedback/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/feedback/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void summaryReportsCountsAndAverage() throws Exception {
        createOne("BUG", 4);
        createOne("GENERAL", 2);

        mockMvc.perform(get("/api/feedback/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.averageRating").value(3.0))
                .andExpect(jsonPath("$.countByCategory.BUG").value(1))
                .andExpect(jsonPath("$.countByRating.4").value(1));
    }
}
