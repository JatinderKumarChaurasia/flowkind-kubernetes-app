package app.flowkind.microservices.compose.product.services;

import app.flowkind.microservices.api.composite.product.*;
import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.api.exceptions.NotFoundException;
import app.flowkind.microservices.utils.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ProductAggregate getProduct(int productID) {
        LOGGER.debug("getCompositeProduct: lookup a product aggregate for productID: {}", productID);
        Product product = productCompositeIntegration.getProduct(productID);
        if (product == null) {
            throw new NotFoundException("No product found for productID: " + productID);
        }
        List<Recommendation> recommendations = productCompositeIntegration.getRecommendations(productID);
        List<Review> reviews = productCompositeIntegration.getReviews(productID);
        LOGGER.debug("getCompositeProduct: aggregate entity found for productID: {}", productID);
        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public void createProduct(ProductAggregate productAggregate) {
        try {

            LOGGER.debug("createCompositeProduct: creates a new composite entity for productId: {}", productAggregate.productID());

            Product product = new Product(productAggregate.productID(), productAggregate.name(), productAggregate.weight(), null);
            productCompositeIntegration.createProduct(product);
            if (productAggregate.recommendationSummaries() != null) {
                productAggregate.recommendationSummaries().forEach(r -> {
                    Recommendation recommendation = new Recommendation(productAggregate.productID(), r.recommendationID(), r.author(), r.rate(), r.content(), null);
                    productCompositeIntegration.createRecommendation(recommendation);
                });
            }

            if (productAggregate.reviewSummaries() != null) {
                productAggregate.reviewSummaries().forEach(r -> {
                    Review review = new Review(productAggregate.productID(), r.reviewID(), r.author(), r.subject(), r.content(), null);
                    productCompositeIntegration.createReview(review);
                });
            }
            LOGGER.debug("createCompositeProduct: composite entities created for productId: {}", productAggregate.productID());

        } catch (RuntimeException runtimeException) {
            LOGGER.warn("createCompositeProduct failed", runtimeException);
            throw runtimeException;
        }
    }

    @Override
    public void deleteProduct(int productID) {
        LOGGER.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productID);
        productCompositeIntegration.deleteProduct(productID);
        productCompositeIntegration.deleteRecommendations(productID);
        productCompositeIntegration.deleteReviews(productID);
        LOGGER.debug("deleteCompositeProduct: aggregate entities deleted for productID: {}", productID);
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
