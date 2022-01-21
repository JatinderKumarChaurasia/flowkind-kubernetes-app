package app.flowkind.microservices.core.review;

import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.api.event.Event;
import app.flowkind.microservices.utils.exceptions.InvalidInputException;
import app.flowkind.microservices.core.review.persistence.ReviewRepository;
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
class FlowkindCoreReviewServiceApplicationTests {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer,Review>> messageProcessor;

    @BeforeEach
    void setUpDatabase() {
        reviewRepository.deleteAll();
    }

    @Test
    void getReviewsByProductId() {
        int productID = 1;
        assertEquals(0,reviewRepository.findByProductID(productID).size());
        sendCreateReviewEvent(productID,1);
        sendCreateReviewEvent(productID,2);
        sendCreateReviewEvent(productID,3);
        assertEquals(3, reviewRepository.findByProductID(productID).size());
        getAndVerifyReviewsByProductID(productID, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productID").isEqualTo(productID)
                .jsonPath("$[2].reviewID").isEqualTo(3);
    }

    @Test
    void duplicateError() {
        int productID = 1;
        int reviewID = 1;
        assertEquals(0, reviewRepository.count());
        sendCreateReviewEvent(productID,reviewID);
        assertEquals(1, reviewRepository.count());
        InvalidInputException invalidInputException = assertThrows(InvalidInputException.class,() ->
            sendCreateReviewEvent(productID,reviewID),"Expected a InvalidInputException here!"
        );
        assertEquals("Duplicate key, Product Id: 1, Review Id:1",invalidInputException.getMessage());
        assertEquals(1, reviewRepository.count());
    }

    @Test
    void deleteReviews() {
        int productID = 1;
        int reviewID = 1;
        sendCreateReviewEvent(productID,reviewID);
        assertEquals(1, reviewRepository.findByProductID(productID).size());
        sendDeleteReviewEvent(productID);
        assertEquals(0, reviewRepository.findByProductID(productID).size());
        sendDeleteReviewEvent(productID);
    }

    @Test
    void getReviewsMissingParameter() {
        getAndVerifyReviewsByProductID("", HttpStatus.BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Required int parameter 'productID' is not present");
    }

    @Test
    void getReviewsInvalidParameter() {
        getAndVerifyReviewsByProductID("?productID=no-integer", HttpStatus.BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getReviewsNotFound() {
        getAndVerifyReviewsByProductID("?productID=213", HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getReviewsInvalidParameterNegativeValue() {
        int productIdInvalid = -1;
        getAndVerifyReviewsByProductID("?productID=" + productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Invalid productID: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductID(int productID, HttpStatus expectedStatus) {
        return getAndVerifyReviewsByProductID("?productID=" + productID, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductID(String productIDQuery, HttpStatus expectedStatus) {
        return webTestClient.get()
                .uri("/review" + productIDQuery)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateReviewEvent(int productID, int reviewID) {
        Review review = new Review(productID, reviewID, "Author " + reviewID, "Subject " + reviewID, "Content " + reviewID, "SA");
        Event<Integer, Review> reviewEvent = new Event<>(Event.Type.CREATE, productID, review);
        messageProcessor.accept(reviewEvent);
    }

    private void sendDeleteReviewEvent(int productID) {
        Event<Integer, Review> reviewEvent = new Event<>(Event.Type.DELETE, productID, null);
        messageProcessor.accept(reviewEvent);
    }
}
