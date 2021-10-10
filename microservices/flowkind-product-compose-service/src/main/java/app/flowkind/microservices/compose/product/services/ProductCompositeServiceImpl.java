package app.flowkind.microservices.compose.product.services;

import app.flowkind.microservices.api.composite.product.*;
import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.utils.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration productCompositeIntegration;

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration productCompositeIntegration) {
        this.serviceUtil = serviceUtil;
        this.productCompositeIntegration = productCompositeIntegration;
    }

    @Override
    public Mono<ProductAggregate> getProduct(int productID) {
        LOGGER.debug("getCompositeProduct: lookup a product aggregate for productID: {}", productID);
        return Mono.zip(values -> createProductAggregate((Product) values[0],
                (List<Recommendation>) values[1],
                (List<Review>) values[2],
                serviceUtil.getServiceAddress()),productCompositeIntegration.getProduct(productID),productCompositeIntegration.getRecommendations(productID).collectList(),productCompositeIntegration.getReviews(productID).collectList()).doOnError(exception -> LOGGER.warn("getCompositeProduct failed: {}", exception.toString())).log(LOGGER.getName(), Level.FINE);
    }

    @Override
    public Mono<Void> createProduct(ProductAggregate productAggregate) {
        try {
            List<Mono> monoList = new ArrayList<>();
            LOGGER.debug("createCompositeProduct: creates a new composite entity for productId: {}", productAggregate.productID());
            LOGGER.info("createCompositeProduct: productAggregate {}",productAggregate.toString());

            Product product = new Product(productAggregate.productID(), productAggregate.name(), productAggregate.weight(), null);
            monoList.add(productCompositeIntegration.createProduct(product));
            if (productAggregate.recommendationSummaries() != null) {
                productAggregate.recommendationSummaries().forEach(r -> {
                    Recommendation recommendation = new Recommendation(productAggregate.productID(), r.recommendationID(), r.author(), r.rate(), r.content(), null);
                    monoList.add(productCompositeIntegration.createRecommendation(recommendation));
                });
            }

            if (productAggregate.reviewSummaries() != null) {
                productAggregate.reviewSummaries().forEach(r -> {
                    Review review = new Review(productAggregate.productID(), r.reviewID(), r.author(), r.subject(), r.content(), null);
                    monoList.add(productCompositeIntegration.createReview(review));
                });
            }
            LOGGER.debug("createCompositeProduct: composite entities created for productId: {}", productAggregate.productID());
            return Mono.zip(result -> "",monoList.toArray(new Mono[0])).doOnError(exception -> LOGGER.warn("createCompositeProduct failed: {}", exception.toString())).then();

        } catch (RuntimeException runtimeException) {
            LOGGER.warn("createCompositeProduct failed", runtimeException);
            throw runtimeException;
        }
    }

    @Override
    public Mono<Void> deleteProduct(int productID) {
        try {
            LOGGER.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productID);
            return Mono.zip(result -> "",productCompositeIntegration.deleteProduct(productID),productCompositeIntegration.deleteRecommendations(productID),productCompositeIntegration.deleteReviews(productID)).doOnError(exception -> LOGGER.warn("delete failed: {}", exception.toString())).log(LOGGER.getName(),Level.FINE).then();
        } catch (RuntimeException exception) {
            LOGGER.warn("deleteCompositeProduct failed: {}", exception.toString());
            throw  exception;
        }
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {
        int productID = product.getProductID();
        String name = product.getName();
        int weight = product.getWeight();
        String productAddress = product.getServiceAddress();
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null: recommendations.stream().map(recommendation -> new RecommendationSummary(recommendation.getRecommendationID(),recommendation.getAuthor(),recommendation.getRate(),recommendation.getContent())).toList();
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null : reviews.stream().map(review -> new ReviewSummary(review.getReviewID(), review.getAuthor(), review.getSubject(),review.getContent())).toList();
        String reviewAddress = (reviews != null && !reviews.isEmpty())?reviews.get(0).getServiceAddress():"";
        String recommendationAddress = (recommendations != null && !recommendations.isEmpty()) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);
        return new ProductAggregate(productID,name,weight,recommendationSummaries,reviewSummaries,serviceAddresses);
    }
}
