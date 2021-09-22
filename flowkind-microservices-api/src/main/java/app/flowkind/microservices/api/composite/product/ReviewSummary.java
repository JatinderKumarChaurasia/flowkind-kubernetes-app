package app.flowkind.microservices.api.composite.product;

public record ReviewSummary(int reviewID,String author,String subject,String content) {
    public ReviewSummary() {
        this(0,null,null,null);
    }
}
