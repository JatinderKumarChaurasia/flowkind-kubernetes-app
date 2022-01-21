package app.flowkind.microservices.core.product;

import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.api.event.Event;
import app.flowkind.microservices.utils.exceptions.InvalidInputException;
import app.flowkind.microservices.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static reactor.core.publisher.Mono.just;

@TestInstance(value = TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlowkindCoreProductServiceApplicationTests extends MongoDbTestBase{

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowkindCoreProductServiceApplicationTests.class);
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer,Product>> messageProcessor;

    @BeforeEach
    void setUpDatabase() {
        productRepository.deleteAll().block();
    }

    @AfterEach
    void cleanUpTestResult() {
        productRepository.deleteAll().block();
    }

    @Test
    void getProductById() {
        int productID = 1;
        assertNull(productRepository.findByProductID(productID).block());
        assertEquals (0,(long) productRepository.count().block());
        sendCreateProductEvent(productID);
        assertNotNull(productRepository.findByProductID(productID).block());
        assertEquals (1,(long) productRepository.count().block());
        getAndVerifyProduct(productID, HttpStatus.OK).jsonPath("$.productID").isEqualTo(productID);
    }

    @Test
    void duplicateError() {
        LOGGER.info("Checking Duplicate Error");
        int productID = 1;
        assertNull(productRepository.findByProductID(productID).block());
        sendCreateProductEvent(productID);
        assertNotNull(productRepository.findByProductID(productID).block());
        InvalidInputException invalidInputException = assertThrows(InvalidInputException.class,()->
            sendCreateProductEvent(productID),"expected a InvalidInputException exception!"
        );
        assertEquals("Duplicate key, Product Id: " + productID,invalidInputException.getMessage());
    }

    @Test
    void deleteProduct() {
        int productID = 1;
        sendCreateProductEvent(productID);
        assertNotNull(productRepository.findByProductID(productID).block());
        sendDeleteProductEvent(productID);
        assertNull(productRepository.findByProductID(productID).block());
        sendDeleteProductEvent(productID);
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

    private void sendCreateProductEvent(int productID) {
        Product product = new Product(productID,"Name "+productID,productID,"SA");
        Event<Integer,Product> productEvent = new Event<>(Event.Type.CREATE,productID,product);
        messageProcessor.accept(productEvent);
    }

    private void sendDeleteProductEvent(int productID) {
        Event<Integer,Product> productEvent = new Event<>(Event.Type.DELETE,productID,null);
        messageProcessor.accept(productEvent);
    }
}
