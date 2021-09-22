package app.flowkind.microservices.core.recommendation;

import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.core.recommendation.persistence.RecommendationRepository;
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
class FlowkindCoreRecommendationServiceApplicationTests extends MongoDbTestBase{

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @BeforeEach
    void setupDb() {
        recommendationRepository.deleteAll();
    }

    @Test
    void getRecommendationsByProductID() {
        int productID=1;
        postAndVerifyRecommendation(productID, 1, HttpStatus.OK);
        postAndVerifyRecommendation(productID, 2, HttpStatus.OK);
        postAndVerifyRecommendation(productID, 3, HttpStatus.OK);

        assertEquals(3, recommendationRepository.findByProductID(productID).size());
        getAndVerifyRecommendationsByProductID(productID, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productID").isEqualTo(productID)
                .jsonPath("$[2].recommendationID").isEqualTo(3);
    }

    @Test
    void duplicateError() {
        int productID = 1;
        int recommendationID = 1;
        postAndVerifyRecommendation(productID, recommendationID, HttpStatus.OK)
                .jsonPath("$.productID").isEqualTo(productID)
                .jsonPath("$.recommendationID").isEqualTo(recommendationID);
        assertEquals(1, recommendationRepository.count());
        postAndVerifyRecommendation(productID, recommendationID, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Recommendation Id:1");
        assertEquals(1, recommendationRepository.count());
    }

    @Test
    void deleteRecommendations() {
        int productID = 1;
        int recommendationID = 1;
        postAndVerifyRecommendation(productID, recommendationID, HttpStatus.OK);
        assertEquals(1, recommendationRepository.findByProductID(productID).size());
        deleteAndVerifyRecommendationsByProductId(productID, HttpStatus.OK);
        assertEquals(0, recommendationRepository.findByProductID(productID).size());
        deleteAndVerifyRecommendationsByProductId(productID, HttpStatus.OK);
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

    private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productID, int recommendationID, HttpStatus expectedStatus) {
        Recommendation recommendation = new Recommendation(productID, recommendationID, "Author " + recommendationID, recommendationID, "Content " + recommendationID, "SA");
        return webTestClient.post()
                .uri("/recommendation")
                .body(just(recommendation), Recommendation.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductId(int productID, HttpStatus expectedStatus) {
        return webTestClient.delete()
                .uri("/recommendation?productID=" + productID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}
