package app.flowkind.microservices.api.core.review;

public record Review(int productID,int reviewID,String author,String subject,String content,String serviceAddress) {
    public Review() {
        this(0,0,null,null,null,null);
    }
}