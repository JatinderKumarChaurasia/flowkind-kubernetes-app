package app.flowkind.microservices.compose.product.services;

import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.api.core.product.ProductService;
import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.api.core.recommendation.RecommendationService;
import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.api.core.review.ReviewService;
import app.flowkind.microservices.api.event.Event;
import app.flowkind.microservices.utils.exceptions.InvalidInputException;
import app.flowkind.microservices.utils.exceptions.NotFoundException;
import app.flowkind.microservices.utils.http.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.logging.Level;

import static reactor.core.publisher.Flux.empty;

@Component
public class ProductCompositeIntegration implements ProductService, ReviewService, RecommendationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCompositeIntegration.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final StreamBridge streamBridge;
    private final Scheduler publishEventScheduler;

    private final String productServiceURL = "http://product";
    private final String recommendationServiceURL = "http://recommendation";
    private final String reviewServiceURL = "http://review";
    // urls
//    private final String productServiceURL;
//    private final String reviewServiceURL;
//    private final String recommendationServiceURL;

    public ProductCompositeIntegration(
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
            WebClient.Builder webclient,
            ObjectMapper objectMapper,
            StreamBridge streamBridge) {
//            @Value("${flowkind-app.flowkind-core-product-service.host}") String productServiceHost,
//            @Value("${flowkind-app.flowkind-core-product-service.port}") String productServicePort,
//            @Value("${flowkind-app.flowkind-core-review-service.host}") String reviewServiceHost,
//            @Value("${flowkind-app.flowkind-core-review-service.port}") String reviewServicePort,
//            @Value("${flowkind-app.flowkind-core-recommendation-service.host}") String recommendationServiceHost,
//            @Value("${flowkind-app.flowkind-core-recommendation-service.port}") String recommendationServicePort) {
        this.publishEventScheduler = publishEventScheduler;
        this.webClient = webclient.build();
        this.objectMapper = objectMapper;
        this.streamBridge = streamBridge;
//        this.productServiceURL = "http://" + productServiceHost + ":" + productServicePort + "/product";
//        this.reviewServiceURL = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
//        this.recommendationServiceURL = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).message();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    @Override
    public Mono<Product> getProduct(int productID) {
        String url = productServiceURL+"/product/" + productID;
        LOGGER.debug("Will call getProduct API on URL: {}", url);
        return webClient.get().uri(url).retrieve().bodyToMono(Product.class).log(LOGGER.getName(), Level.FINE).onErrorMap(WebClientResponseException.class, this::handleExceptions);
    }

    @Override
    public Mono<Product> createProduct(Product product) {
        LOGGER.info("Sending Create product re: {}",product);
        return Mono.fromCallable(() -> {
            sendMessage("products-out-0",new Event<>(Event.Type.CREATE,product.getProductID(),product));
            return product;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteProduct(int productID) {
        return Mono.fromRunnable(() -> sendMessage("products-out-0",new Event<>(Event.Type.DELETE,productID,null))).subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productID) {
        String url = recommendationServiceURL+"/recommendation?productID=" + productID;
        LOGGER.debug("Will call getRecommendations API on URL: {}", url);
        return webClient.get().uri(url).retrieve().bodyToFlux(Recommendation.class).log(LOGGER.getName(),Level.FINE).onErrorResume(error -> empty());
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation recommendation) {
        return Mono.fromCallable(() -> {
            sendMessage("recommendations-out-0",new Event<>(Event.Type.CREATE,recommendation.getProductID(),recommendation));
            return recommendation;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteRecommendations(int productID) {
        return Mono.fromRunnable(() -> sendMessage("recommendations-out-0",new Event<>(Event.Type.DELETE,productID,null))).subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Flux<Review> getReviews(int productID) {
        String url = reviewServiceURL+"/review?productID=" + productID;
        LOGGER.debug("Will call getReviews API on URL: {}", url);
        return webClient.get().uri(url).retrieve().bodyToFlux(Review.class).log(LOGGER.getName(),Level.FINE).onErrorResume(error -> empty());
    }

    @Override
    public Mono<Review> createReview(Review review) {
        return Mono.fromCallable(() -> {
            sendMessage("reviews-out-0", new Event<>(Event.Type.CREATE, review.getReviewID(), review));
            return review;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteReviews(int productID) {
        return Mono.fromRunnable(()-> sendMessage("reviews-out-0",new Event<>(Event.Type.DELETE,productID,null))).subscribeOn(publishEventScheduler).then();
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException exception) {
        switch (exception.getStatusCode()) {
            case NOT_FOUND -> {
                LOGGER.debug("Throwing Not found exception");
                return new NotFoundException(getErrorMessage(exception));
            }
            case UNPROCESSABLE_ENTITY -> {
                LOGGER.debug("Throwing invalid input exception");
                return new InvalidInputException(getErrorMessage(exception));
            }
            default -> {
                LOGGER.warn("Got an unexpected HTTP error: {}, will rethrow it", exception.getStatusCode());
                LOGGER.warn("Error body: {}", exception.getResponseBodyAsString());
                return exception;
            }
        }
    }

    private Throwable handleExceptions(Throwable exception) {
        if (!(exception instanceof WebClientResponseException webClientResponseException)) {
            LOGGER.warn("Got a unexpected error: {}, will rethrow it", exception.toString());
            return exception;
        }
        switch (webClientResponseException.getStatusCode()) {
            case NOT_FOUND -> throw new NotFoundException(getErrorMessage(webClientResponseException));
            case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(webClientResponseException));
            default -> {
                LOGGER.warn("Got an unexpected HTTP error: {}, will rethrow it", webClientResponseException.getStatusCode());
                LOGGER.warn("Error body: {}", webClientResponseException.getResponseBodyAsString());
                return exception;
            }
        }
    }

    private void sendMessage(String bindingName, Event<Integer,Object> event) {
        LOGGER.debug("Sending a {} message to {}", event.eventType(), bindingName);
        Message<Event<Integer,Object>> message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.key())
                .build();
        streamBridge.send(bindingName, message);
    }

    private String getErrorMessage(WebClientResponseException exception) {
        try {
            return objectMapper.readValue(exception.getResponseBodyAsString(), HttpErrorInfo.class).message();
        } catch (IOException ioException) {
            return exception.getMessage();
        }
    }

    public Mono<Health> getProductHealth() {
        String productsURL="http://localhost:7001";
        return getHealth(productsURL);
    }

    public Mono<Health> getRecommendationHealth() {
        String recommendationsURL="http://localhost:7003";
        return getHealth(recommendationsURL);
    }

    public Mono<Health> getReviewHealth() {
        String reviewsURL="http://localhost:7002";
        return getHealth(reviewsURL);
    }

    // Service Health Functions
    private Mono<Health> getHealth(String serviceURL) {
        serviceURL+="/actuator/health";
        LOGGER.debug("Will call the Health API on URL: {}", serviceURL);
        return webClient.get().uri(serviceURL).retrieve().bodyToMono(String.class).map(s -> new Health.Builder().up().build()).onErrorResume(exception -> Mono.just(new Health.Builder().down().build())).log(LOGGER.getName(),Level.FINE);
    }
}
