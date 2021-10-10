package app.flowkind.microservices.compose.product;

import app.flowkind.microservices.api.composite.product.ProductAggregate;
import app.flowkind.microservices.api.composite.product.RecommendationSummary;
import app.flowkind.microservices.api.composite.product.ReviewSummary;
import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.api.exceptions.InvalidInputException;
import app.flowkind.microservices.api.exceptions.NotFoundException;
import app.flowkind.microservices.compose.product.services.ProductCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlowkindProductComposeServiceApplicationTests {

    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = 2;
    private static final int PRODUCT_ID_INVALID = 3;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductCompositeIntegration productCompositeIntegration;

    @BeforeEach
    void setup() {
        when(productCompositeIntegration.getProduct(PRODUCT_ID_OK))
                .thenReturn(Mono.just(new Product(PRODUCT_ID_OK, "name", 1, "mock-address")));
        when(productCompositeIntegration.getRecommendations(PRODUCT_ID_OK))
                .thenReturn(Flux.fromIterable(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address"))));
        when(productCompositeIntegration.getReviews(PRODUCT_ID_OK))
                .thenReturn(Flux.fromIterable(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address"))));
        when(productCompositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
                .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));
        when(productCompositeIntegration.getProduct(PRODUCT_ID_INVALID))
                .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    }

//    @Test
//    void createProductComposite1() {
//        ProductAggregate productAggregate =  new ProductAggregate(1, "name", 1, null, null, null);
//        postAndVerifyProductAggregate(productAggregate,HttpStatus.OK);
//    }
//
//    @Test
//    void createProductComposite2() {
//        ProductAggregate productAggregate = new ProductAggregate(1, "name", 1,
//                singletonList(new RecommendationSummary(1, "a", 1, "c")),
//                singletonList(new ReviewSummary(1, "a", "s", "c")), null);
//        postAndVerifyProductAggregate(productAggregate,HttpStatus.OK);
//    }
//
//    @Test
//    void deleteProductComposite() {
//        ProductAggregate productAggregate = new ProductAggregate(1, "name", 1,
//                singletonList(new RecommendationSummary(1, "a", 1, "c")),
//                singletonList(new ReviewSummary(1, "a", "s", "c")), null);
//        postAndVerifyProductAggregate(productAggregate,HttpStatus.OK);
//        deleteAndVerifyProductAggregate(productAggregate.productID(),HttpStatus.OK);
//        deleteAndVerifyProductAggregate(productAggregate.productID(),HttpStatus.OK);
//    }

//    private void deleteAndVerifyProductAggregate(int productID, HttpStatus httpStatus) {
//        webTestClient.delete().uri("/product-composite/"+productID).exchange().expectStatus().isEqualTo(httpStatus);
//    }
//
//
//    private void postAndVerifyProductAggregate(ProductAggregate productAggregate, HttpStatus httpStatus) {
//        webTestClient.post().uri("/product-composite").body(just(productAggregate),ProductAggregate.class).exchange().expectStatus().isEqualTo(httpStatus);
//    }

    @Test
    void getProductById() {
        getAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
                .jsonPath("$.productID").isEqualTo(PRODUCT_ID_OK)
                .jsonPath("$.recommendationSummaries.length()").isEqualTo(1)
                .jsonPath("$.reviewSummaries.length()").isEqualTo(1);
    }

    @Test
    void getProductNotFound() {
        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND,HttpStatus.NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
    }

    @Test
    void getProductInvalidInput() {
        getAndVerifyProduct(PRODUCT_ID_INVALID,HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
                .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productID, HttpStatus expectedStatus) {
        return webTestClient.get()
                .uri("/product-composite/" + productID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }
}