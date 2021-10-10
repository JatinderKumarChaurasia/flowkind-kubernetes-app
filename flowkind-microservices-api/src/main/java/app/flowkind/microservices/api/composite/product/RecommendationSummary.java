package app.flowkind.microservices.api.composite.product;

public record RecommendationSummary(int recommendationID, String author, int rate,String content) {
    public RecommendationSummary() {
        this(0,null,0,null);
    }
}