package app.flowkind.microservices.core.product;

import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static reactor.core.publisher.Mono.just;

@TestInstance(value = TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlowkindCoreProductServiceApplicationTests extends MongoDbTestBase{

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUpDatabase() {
        productRepository.deleteAll();
    }

    @AfterEach
    void cleanUpTestResult() {
        productRepository.deleteAll();
    }

    @Test
    void getProductById() {
        int productID = 1;
        postAndVerifyProduct(productID, HttpStatus.OK);
        assertTrue(productRepository.findByProductID(productID).isPresent());
        getAndVerifyProduct(productID, HttpStatus.OK).jsonPath("$.productID").isEqualTo(productID);
    }

    @Test
    void duplicateError() {
        int productID = 1;
        postAndVerifyProduct(productID, HttpStatus.OK);
        assertTrue(productRepository.findByProductID(productID).isPresent());
        postAndVerifyProduct(productID, HttpStatus.UNPROCESSABLE_ENTITY).jsonPath("$.path").isEqualTo("/product")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: " + productID);
    }

    @Test
    void deleteProduct() {
        int productID = 1;
        postAndVerifyProduct(productID, HttpStatus.OK);
        assertTrue(productRepository.findByProductID(productID).isPresent());
        deleteAndVerifyProduct(productID, HttpStatus.OK);
        assertFalse(productRepository.findByProductID(productID).isPresent());
        deleteAndVerifyProduct(productID, HttpStatus.OK);
    }

    @Test
    void getProductInvalidParameterString() {
        getAndVerifyProduct("/no-integer", HttpStatus.BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/product/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getProductNotFound() {
        int productIDNotFound = 13;
        getAndVerifyProduct(productIDNotFound, HttpStatus.NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product/" + productIDNotFound)
                .jsonPath("$.message").isEqualTo("No product found for productID: " + productIDNotFound);
    }

    @Test
    void getProductInvalidParameterNegativeValue() {
        int productIDInvalid = -1;
        getAndVerifyProduct(productIDInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product/" + productIDInvalid)
                .jsonPath("$.message").isEqualTo("Invalid productID: " + productIDInvalid);
    }

    private WebTestClient.BodyContentSpec postAndVerifyProduct(int productID, HttpStatus expectedHTTPStatus) {
        Product product = new Product(productID, "Name " + productID, productID, "SA");
        return webTestClient.post()
                .uri("/product")
                .body(just(product), Product.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedHTTPStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productID, HttpStatus expectedHTTPStatus) {
        return getAndVerifyProduct("/"+productID,expectedHTTPStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIDPath,HttpStatus expectedHTTPStatus) {
        return webTestClient.get().uri("/product"+productIDPath).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isEqualTo(expectedHTTPStatus).expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productID, HttpStatus expectedHTTPStatus) {
        return webTestClient.delete()
                .uri("/product/" + productID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedHTTPStatus)
                .expectBody();
    }
}
