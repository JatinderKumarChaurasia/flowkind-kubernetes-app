package app.flowkind.microservices.core.recommendation;

import app.flowkind.microservices.api.core.recommendation.Recommendation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlowkindCoreRecommendationServiceApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getRecommendationsByProductID() {
        int productID=1;
        webTestClient.get()
                .uri("/recommendation?productID="+productID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].productID").isEqualTo(productID);
//        String value = webTestClient.get()
//                .uri("/recommendation?productID="+productID)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange().expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody().jsonPath("$.length()").isEqualTo(3)
//                .jsonPath("$").isEqualTo(productID)
//                .returnResult().toString();
//        System.out.println(value);
    }

    @Test
    void getRecommendationsMissingParameter() {
        webTestClient.get()
                .uri("/recommendation")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Required int parameter 'productID' is not present");
    }

    @Test
    void getRecommendationsInvalidParameter() {
        webTestClient.get()
                .uri("/recommendation?productID=no-integer")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getRecommendationsNotFound() {
        int productIDNotFound = 113;
        webTestClient.get()
                .uri("/recommendation?productID=" + productIDNotFound)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getRecommendationsInvalidParameterNegativeValue() {
        System.out.println("Getting Negative Value");
        int productIDInvalid = -1;
        webTestClient.get()
                .uri("/recommendation?productID=" + productIDInvalid)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/recommendation");
    }
}
