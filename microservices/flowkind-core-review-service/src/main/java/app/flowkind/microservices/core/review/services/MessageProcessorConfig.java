package app.flowkind.microservices.core.review.services;

import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.api.core.review.ReviewService;
import app.flowkind.microservices.api.event.Event;
import app.flowkind.microservices.utils.exceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessorConfig.class);
    private final ReviewService reviewService;

    @Autowired
    public MessageProcessorConfig(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Bean
    public Consumer<Event<Integer, Review>> messageProcessor() {
        return integerReviewEvent -> {
            LOGGER.info("Process message created at {}...", integerReviewEvent.getEventCreatedAt());
            switch (integerReviewEvent.eventType()) {
                case CREATE -> {
                    Review review = integerReviewEvent.data();
                    LOGGER.info("Create review with ID: {}/{}", review.getProductID(), review.getReviewID());
                    reviewService.createReview(review).block();
                }
                case DELETE -> {
                    int productID = integerReviewEvent.key();
                    LOGGER.info("Delete reviews with ProductID: {}", productID);
                    reviewService.deleteReviews(productID).block();
                }
                default -> {
                    String errorMessage = "Incorrect event type: " + integerReviewEvent.eventType() + ", expected a CREATE or DELETE event";
                    LOGGER.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }
            LOGGER.info("Message processing done!");
        };
    }
}
