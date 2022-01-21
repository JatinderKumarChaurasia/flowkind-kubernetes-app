package app.flowkind.microservices.core.recommendation;

import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.api.event.Event;
import app.flowkind.microservices.utils.exceptions.InvalidInputException;
import app.flowkind.microservices.core.recommendation.persistence.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlowkindCoreRecommendationServiceApplicationTests extends MongoDbTestBase{

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer,Recommendation>> messageProcessor;

    @BeforeEach
    void setupDb() {
        recommendationRepository.deleteAll().block();
    }

    @Test
    void getRecommendationsByProductID() {
        int productID=1;
        sendCreateRecommendationEvent(productID,1);
        sendCreateRecommendationEvent(productID,2);
        sendCreateRecommendationEvent(productID,3);

        assertEquals(3, recommendationRepository.findByProductID(productID).count().block());
        getAndVerifyRecommendationsByProductID(productID, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productID").isEqualTo(productID)
                .jsonPath("$[2].recommendationID").isEqualTo(3);
    }

    @Test
    void duplicateError() {
        int productID = 1;
        int recommendationID = 1;
        sendCreateRecommendationEvent(productID,recommendationID);
        assertEquals(1, recommendationRepository.count().block());
        InvalidInputException invalidInputException = assertThrows(InvalidInputException.class,() -> sendCreateRecommendationEvent(productID,recommendationID),"Expected a InvalidInputException here!");
        assertEquals("Duplicate key, Product Id: 1, Recommendation Id:1", invalidInputException.getMessage());
        assertEquals(1, recommendationRepository.count().block());
    }

    @Test
    void deleteRecommendations() {
        int productID = 1;
        int recommendationID = 1;
        sendCreateRecommendationEvent(productID,recommendationID);
        assertEquals(1, recommendationRepository.findByProductID(productID).count().block());
        sendDeleteRecommendationEvent(productID);
        assertEquals(0, recommendationRepository.findByProductID(productID).count().block());
        sendDeleteRecommendationEvent(productID);
    }

    @Test
    void getRecommendationsMissingParameter() {
        getAndVerifyRecommendationsByProductID("", HttpStatus.BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Required int parameter 'productID' is not present");
    }

    @Test
    void getRecommendationsInvalidParameter() {
        getAndVerifyRecommendationsByProductID("?productID=no-integer", HttpStatus.BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getRecommendationsNotFound() {
        int productIDNotFound = 113;
        getAndVerifyRecommendationsByProductID("?productID="+productIDNotFound,HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getRecommendationsInvalidParameterNegativeValue() {
        System.out.println("Getting Negative Value");
        int productIDInvalid = -1;
        getAndVerifyRecommendationsByProductID("?productID="+productIDInvalid,HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Invalid productID: " + productIDInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductID(int productID, HttpStatus expectedStatus) {
        return getAndVerifyRecommendationsByProductID("?productID=" + productID, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductID(String productIDQuery, HttpStatus expectedStatus) {
        return webTestClient.get()
                .uri("/recommendation" + productIDQuery)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateRecommendationEvent(int productID,int recommendationID) {
        Recommendation recommendation = new Recommendation(productID, recommendationID, "Author " + recommendationID, recommendationID, "Content " + recommendationID, "SA");
        Event<Integer,Recommendation> recommendationEvent = new Event<>(Event.Type.CREATE,productID,recommendation);
        messageProcessor.accept(recommendationEvent);
    }

    private void sendDeleteRecommendationEvent(int productID) {
        Event<Integer,Recommendation> recommendationEvent = new Event<>(Event.Type.DELETE,productID,null);
        messageProcessor.accept(recommendationEvent);
    }
}
