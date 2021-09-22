package app.flowkind.microservices.core.review;

import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.core.review.persistence.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static reactor.core.publisher.Mono.just;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlowkindCoreReviewServiceApplicationTests {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setUpDatabase() {
        reviewRepository.deleteAll();
    }

    @Test
    void getReviewsByProductId() {
        int productID = 1;
        assertEquals(0,reviewRepository.findByProductID(productID).size());

        postAndVerifyReview(productID, 1, HttpStatus.OK);
        postAndVerifyReview(productID, 2, HttpStatus.OK);
        postAndVerifyReview(productID, 3, HttpStatus.OK);

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
        postAndVerifyReview(productID, reviewID, HttpStatus.OK)
                .jsonPath("$.productID").isEqualTo(productID)
                .jsonPath("$.reviewID").isEqualTo(reviewID);
        assertEquals(1, reviewRepository.count());
        postAndVerifyReview(productID, reviewID, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Review Id:1");
        assertEquals(1, reviewRepository.count());
    }

    @Test
    void deleteReviews() {
        int productID = 1;
        int reviewID = 1;
        postAndVerifyReview(productID, reviewID, HttpStatus.OK);
        assertEquals(1, reviewRepository.findByProductID(productID).size());
        deleteAndVerifyReviewsByProductID(productID, HttpStatus.OK);
        assertEquals(0, reviewRepository.findByProductID(productID).size());
        deleteAndVerifyReviewsByProductID(productID, HttpStatus.OK);
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

    private WebTestClient.BodyContentSpec postAndVerifyReview(int productID, int reviewID, HttpStatus expectedStatus) {
        Review review = new Review(productID, reviewID, "Author " + reviewID, "Subject " + reviewID, "Content " + reviewID, "SA");
        return webTestClient.post()
                .uri("/review")
                .body(just(review), Review.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByProductID(int productID, HttpStatus expectedStatus) {
        return webTestClient.delete()
                .uri("/review?productID=" + productID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}
