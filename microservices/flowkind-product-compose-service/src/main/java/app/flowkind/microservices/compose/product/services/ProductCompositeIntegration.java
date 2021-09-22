package app.flowkind.microservices.compose.product.services;

import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.api.core.product.ProductService;
import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.api.core.recommendation.RecommendationService;
import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.api.core.review.ReviewService;
import app.flowkind.microservices.api.exceptions.InvalidInputException;
import app.flowkind.microservices.api.exceptions.NotFoundException;
import app.flowkind.microservices.utils.http.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductCompositeIntegration implements ProductService, ReviewService, RecommendationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCompositeIntegration.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // urls
    private final String productServiceURL;
    private final String reviewServiceURL;
    private final String recommendationServiceURL;

    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${flowkind-app.flowkind-core-product-service.host}") String productServiceHost,
            @Value("${flowkind-app.flowkind-core-product-service.port}") String productServicePort,
            @Value("${flowkind-app.flowkind-core-review-service.host}") String reviewServiceHost,
            @Value("${flowkind-app.flowkind-core-review-service.port}") String reviewServicePort,
            @Value("${flowkind-app.flowkind-core-recommendation-service.host}") String recommendationServiceHost,
            @Value("${flowkind-app.flowkind-core-recommendation-service.port}") String recommendationServicePort) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.productServiceURL = "http://" + productServiceHost + ":" + productServicePort + "/product";
        this.reviewServiceURL = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
        this.recommendationServiceURL = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).message();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    @Override
    public Product getProduct(int productID) {
        try {
            String url = productServiceURL+"/" + productID;
            LOGGER.debug("Will call getProduct API on URL: {}", url);
            Product product = restTemplate.getForObject(url, Product.class);
            assert product != null;
            LOGGER.debug("Found a product with id: {}", product.getProductID());
            return product;
        } catch (HttpClientErrorException exception) {
            switch (exception.getStatusCode()) {
                case NOT_FOUND -> throw new NotFoundException(getErrorMessage(exception));
                case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(exception));
                default -> {
                    LOGGER.warn("Got an unexpected HTTP error: {}, will rethrow it", exception.getStatusCode());
                    LOGGER.warn("Error body: {}", exception.getResponseBodyAsString());
                    throw exception;
                }
            }
        }
    }

    @Override
    public Product createProduct(Product product) {
        try {
            String url = productServiceURL;
            LOGGER.debug("Will post a new product to URL: {}", url);
            Product product1 = restTemplate.postForObject(url, product, Product.class);
            assert product1 != null;
            LOGGER.debug("Created a product with id: {}", product1.getProductID());
            return product;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deleteProduct(int productID) {
        try {
            String url = productServiceURL + "/" + productID;
            LOGGER.debug("Will call the deleteProduct API on URL: {}", url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productID) {
        try {
            String url = recommendationServiceURL+"?productID=" + productID;
            LOGGER.debug("Will call getRecommendations API on URL: {}", url);
            List<Recommendation> recommendations = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Recommendation>>() {
            }).getBody();
            assert recommendations != null;
            LOGGER.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productID);
            return recommendations;
        } catch (Exception e) {
            LOGGER.warn("Got an exception while requesting recommendations, return zero recommendations: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Recommendation createRecommendation(Recommendation recommendation) {
        try {
            String url = recommendationServiceURL;
            LOGGER.debug("Will post a new recommendation to URL: {}", url);
            Recommendation recommendation1 = restTemplate.postForObject(url,recommendation,Recommendation.class);
            LOGGER.debug("Created a recommendation with id: {}", recommendation.getProductID());
            return recommendation1;
        } catch (HttpClientErrorException exception) {
            throw handleHttpClientException(exception);
        }
    }

    @Override
    public void deleteRecommendations(int productID) {
        try {
            String url = recommendationServiceURL + "?productId=" + productID;
            LOGGER.debug("Will call the deleteRecommendations API on URL: {}", url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException exception) {
            throw handleHttpClientException(exception);
        }
    }

    @Override
    public List<Review> getReviews(int productID) {
        try {
            String url = reviewServiceURL+"?productID" + productID;
            LOGGER.debug("Will call getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate
                    .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Review>>() {
                    })
                    .getBody();
            assert reviews != null;
            LOGGER.debug("Found {} reviews for a product with id: {}", reviews.size(), productID);
            return reviews;
        } catch (Exception ex) {
            LOGGER.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Review createReview(Review review) {
        try {
            String url = reviewServiceURL;
            LOGGER.debug("Will post a new review to URL: {}", url);
            Review review1 = restTemplate.postForObject(url,review,Review.class);
            assert review1 != null;
            LOGGER.debug("Created a review with id: {}", review1.getProductID());
            return review1;
        } catch (HttpClientErrorException exception) {
            throw handleHttpClientException(exception);
        }
    }

    @Override
    public void deleteReviews(int productID) {
        try {
            String url = reviewServiceURL+"?productID="+productID;
            LOGGER.debug("Will call the deleteReviews API on URL: {}", url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException exception) {
            throw handleHttpClientException(exception);
        }
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
}
