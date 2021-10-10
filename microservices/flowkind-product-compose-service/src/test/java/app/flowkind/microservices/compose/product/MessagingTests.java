package app.flowkind.microservices.compose.product;

import app.flowkind.microservices.api.composite.product.ProductAggregate;
import app.flowkind.microservices.api.composite.product.RecommendationSummary;
import app.flowkind.microservices.api.composite.product.ReviewSummary;
import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.api.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;

import static app.flowkind.microservices.compose.product.IsSameEvent.sameEventExceptCreatedAt;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true"})
@Import(value = {TestChannelBinderConfiguration.class})
class MessagingTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingTests.class);

    @Autowired
    private WebTestClient webTestClient;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public OutputDestination targetOutputDestination;

    @BeforeEach
    void setUp() {
        purgeMessages("products");
        purgeMessages("recommendations");
        purgeMessages("reviews");
    }

    @Test
    void createCompositeProduct1() {

        ProductAggregate composite = new ProductAggregate(1, "name", 1, null, null, null);
        postAndVerifyProduct(composite, HttpStatus.ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");
        assertEquals(1, productMessages.size());
        Event<Integer, Product> expectedEvent =
                new Event<>(Event.Type.CREATE, composite.productID(), new Product(composite.productID(), composite.name(), composite.weight(), null));
        assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedEvent)));
        assertEquals(0, recommendationMessages.size());
        assertEquals(0, reviewMessages.size());
    }

    @Test
    void createCompositeProduct2() {

        ProductAggregate composite = new ProductAggregate(1, "name", 1,
                singletonList(new RecommendationSummary(1, "a", 1, "c")),
                singletonList(new ReviewSummary(1, "a", "s", "c")), null);
        postAndVerifyProduct(composite, HttpStatus.ACCEPTED);
        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");
        assertEquals(1, productMessages.size());
        Event<Integer, Product> expectedProductEvent =
                new Event<>(Event.Type.CREATE, composite.productID(), new Product(composite.productID(), composite.name(), composite.weight(), null));
        assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedProductEvent)));
        assertEquals(1, recommendationMessages.size());
        RecommendationSummary rec = composite.recommendationSummaries().get(0);
        Event<Integer, Recommendation> expectedRecommendationEvent =
                new Event<>(Event.Type.CREATE, composite.productID(),
                        new Recommendation(composite.productID(), rec.recommendationID(), rec.author(), rec.rate(), rec.content(), null));
        assertThat(recommendationMessages.get(0), is(sameEventExceptCreatedAt(expectedRecommendationEvent)));
        assertEquals(1, reviewMessages.size());
        ReviewSummary rev = composite.reviewSummaries().get(0);
        Event<Integer, Review> expectedReviewEvent =
                new Event<>(Event.Type.CREATE, composite.productID(), new Review(composite.productID(), rev.reviewID(), rev.author(), rev.subject(), rev.content(), null));
        assertThat(reviewMessages.get(0), is(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    @Test
    void deleteCompositeProduct() {
        deleteAndVerifyProduct(1, HttpStatus.ACCEPTED);
        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");
        assertEquals(1, productMessages.size());
        Event<Integer, Product> expectedProductEvent = new Event<>(Event.Type.DELETE, 1, null);
        assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedProductEvent)));
        assertEquals(1, recommendationMessages.size());
        Event<Integer, Product> expectedRecommendationEvent = new Event<>(Event.Type.DELETE, 1, null);
        assertThat(recommendationMessages.get(0), is(sameEventExceptCreatedAt(expectedRecommendationEvent)));
        assertEquals(1, reviewMessages.size());
        Event<Integer, Product> expectedReviewEvent = new Event<>(Event.Type.DELETE, 1, null);
        assertThat(reviewMessages.get(0), is(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    private void purgeMessages(String bindingName) {
        getMessages(bindingName);
    }

    private List<String> getMessages(String bindingName) {
        List<String> messages = new ArrayList<>();
        boolean anyMoreMessages = true;
        while (anyMoreMessages) {
            Message<byte[]> message = getMessage(bindingName);
            if (message == null) {
                anyMoreMessages = false;
            } else {
                messages.add(new String(message.getPayload()));
            }
        }
        return messages;
    }

    private Message<byte[]> getMessage(String bindingName) {
        try {
            return targetOutputDestination.receive(0,bindingName);
        }catch (NullPointerException exception) {
            LOGGER.error("getMessage() received a NPE with binding = {}", bindingName);
            return null;
        }
    }
    private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
        webTestClient.post()
                .uri("/product-composite")
                .body(just(compositeProduct), ProductAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    private void deleteAndVerifyProduct(int productID, HttpStatus expectedStatus) {
        webTestClient.delete()
                .uri("/product-composite/" + productID)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }
}
