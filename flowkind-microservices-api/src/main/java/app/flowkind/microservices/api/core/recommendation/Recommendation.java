package app.flowkind.microservices.api.core.recommendation;

public record Recommendation( int productID, int recommendationID, String author, int rate, String content, String serviceAddress ){
    public Recommendation() {
        this(0,0,null,0,null,null);
    }
}