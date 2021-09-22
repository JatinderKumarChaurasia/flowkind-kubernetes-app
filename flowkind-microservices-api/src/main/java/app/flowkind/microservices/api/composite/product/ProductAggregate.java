package app.flowkind.microservices.api.composite.product;

import java.util.List;

public record ProductAggregate(int productID, String name ,int weight, List<RecommendationSummary> recommendationSummaries, List<ReviewSummary> reviewSummaries, ServiceAddresses serviceAddresses) {
    public ProductAggregate() {
        this(0,null,0,null,null,null);
    }
}

//public class ProductAggregate {
//    private final int productID;
//    private final String name;
//    private final int weight;
//    private final List<RecommendationSummary> recommendations;
//    private final List<ReviewSummary> reviews;
//    private final ServiceAddresses serviceAddresses;
//
//    public ProductAggregate() {
//        productID = 0;
//        name = null;
//        weight = 0;
//        recommendations = null;
//        reviews = null;
//        serviceAddresses = null;
//    }
//
//    public ProductAggregate(
//            int productID,
//            String name,
//            int weight,
//            List<RecommendationSummary> recommendations,
//            List<ReviewSummary> reviews,
//            ServiceAddresses serviceAddresses) {
//
//        this.productID = productID;
//        this.name = name;
//        this.weight = weight;
//        this.recommendations = recommendations;
//        this.reviews = reviews;
//        this.serviceAddresses = serviceAddresses;
//    }
//
//    public int getProductID() {
//        return productID;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public int getWeight() {
//        return weight;
//    }
//
//    public List<RecommendationSummary> getRecommendations() {
//        return recommendations;
//    }
//
//    public List<ReviewSummary> getReviews() {
//        return reviews;
//    }
//
//    public ServiceAddresses getServiceAddresses() {
//        return serviceAddresses;
//    }
//}