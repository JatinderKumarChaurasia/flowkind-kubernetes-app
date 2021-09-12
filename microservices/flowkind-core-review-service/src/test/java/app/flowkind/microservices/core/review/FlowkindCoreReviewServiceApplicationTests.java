package app.flowkind.microservices.core.review;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlowkindCoreReviewServiceApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getReviewsByProductId() {
        int productID = 1;
        webTestClient.get()
                .uri("/review?productID=" + productID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].productID").isEqualTo(productID);
    }

    @Test
    void getReviewsMissingParameter() {
        webTestClient.get()
                .uri("/review")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Required int parameter 'productID' is not present");
    }

    @Test
    void getReviewsInvalidParameter() {
        webTestClient.get()
                .uri("/review?productID=no-integer")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getReviewsNotFound() {
        int productIDNotFound = 213;
        webTestClient.get()
                .uri("/review?productID=" + productIDNotFound)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getReviewsInvalidParameterNegativeValue() {
        int productIDInvalid = -1;
        webTestClient.get()
                .uri("/review?productID=" + productIDInvalid)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Invalid productID: " + productIDInvalid);
    }

}
