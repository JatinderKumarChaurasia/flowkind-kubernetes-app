package app.flowkind.microservices.core.recommendation.services;

import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.api.core.recommendation.RecommendationService;
import app.flowkind.microservices.api.event.Event;
import app.flowkind.microservices.api.exceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessorConfig.class);
    private final RecommendationService recommendationService;

    @Autowired
    public MessageProcessorConfig(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Bean
    public Consumer<Event<Integer, Recommendation>> messageProcessor() {
        return integerRecommendationEvent -> {
            LOGGER.info("processor message created at :"+integerRecommendationEvent.getEventCreatedAt());
            switch (integerRecommendationEvent.eventType()) {
                case CREATE -> {
                    Recommendation recommendation = integerRecommendationEvent.data();
                    LOGGER.info("Create recommendation with ID: {}/{}", recommendation.getProductID(), recommendation.getRecommendationID());
                    recommendationService.createRecommendation(recommendation).block();
                }
                case DELETE -> {
                    int productID = integerRecommendationEvent.key();
                    LOGGER.info("Delete recommendations with ProductID: {}", productID);
                    recommendationService.deleteRecommendations(productID).block();
                }
                default -> {
                    String errorMessage = "Incorrect event type: " + integerRecommendationEvent.eventType() + ", expected a CREATE or DELETE event";
                    LOGGER.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }
            LOGGER.info("Message processing done!");
        };
    }
}
