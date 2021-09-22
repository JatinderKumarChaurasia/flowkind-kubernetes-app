package app.flowkind.microservices.api.core.recommendation;

//public record Recommendation( int productID, int recommendationID, String author, int rate, String content, String serviceAddress ){
//    public Recommendation() {
//        this(0,0,null,0,null,null);
//    }
//}

public class Recommendation {
    private int productID;
    private int recommendationID;
    private String author;
    private int rate;
    private String content;
    private String serviceAddress;

    public Recommendation() {
        productID = 0;
        recommendationID = 0;
        author = null;
        rate = 0;
        content = null;
        serviceAddress = null;
    }

    public Recommendation(int productID, int recommendationID, String author, int rate, String content, String serviceAddress) {
        this.productID = productID;
        this.recommendationID = recommendationID;
        this.author = author;
        this.rate = rate;
        this.content = content;
        this.serviceAddress = serviceAddress;
    }

    public int getProductID() {
        return productID;
    }

    public int getRecommendationID() {
        return recommendationID;
    }

    public String getAuthor() {
        return author;
    }

    public int getRate() {
        return rate;
    }

    public String getContent() {
        return content;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public void setRecommendationID(int recommendationID) {
        this.recommendationID = recommendationID;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}