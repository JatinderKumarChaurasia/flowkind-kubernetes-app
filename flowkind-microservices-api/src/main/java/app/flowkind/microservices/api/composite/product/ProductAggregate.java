package app.flowkind.microservices.api.composite.product;

import java.util.List;

public record ProductAggregate(int productID, String name ,int weight, List<RecommendationSummary> recommendationSummaries, List<ReviewSummary> reviewSummaries, ServiceAddresses serviceAddresses) {
    public ProductAggregate() {
        this(0,null,0,null,null,null);
    }
}