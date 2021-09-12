package app.flowkind.microservices.compose.product.services;

import app.flowkind.microservices.api.composite.product.*;
import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.api.exceptions.NotFoundException;
import app.flowkind.microservices.utils.http.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration productCompositeIntegration;

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration productCompositeIntegration) {
        this.serviceUtil = serviceUtil;
        this.productCompositeIntegration = productCompositeIntegration;
    }

    @Override
    public ProductAggregate getProduct(int productID) {
        Product product = productCompositeIntegration.getProduct(productID);
        if (product == null) {
            throw new NotFoundException("No product found for productID: " + productID);
        }
        List<Recommendation> recommendations = productCompositeIntegration.getRecommendations(productID);
        List<Review> reviews = productCompositeIntegration.getReviews(productID);
        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {
        int productID = product.productID();
        String name = product.name();
        int weight = product.weight();
        String productAddress = product.serviceAddress();
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null: recommendations.stream().map(recommendation -> new RecommendationSummary(recommendation.recommendationID(),recommendation.author(),recommendation.rate())).collect(Collectors.toList());
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null : reviews.stream().map(review -> new ReviewSummary(review.reviewID(), review.author(), review.subject())).collect(Collectors.toList());
        String reviewAddress = (reviews != null && !reviews.isEmpty())?reviews.get(0).serviceAddress():"";
        String recommendationAddress = (recommendations != null && !recommendations.isEmpty()) ? recommendations.get(0).serviceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);
        return new ProductAggregate(productID,name,weight,recommendationSummaries,reviewSummaries,serviceAddresses);
    }
}
