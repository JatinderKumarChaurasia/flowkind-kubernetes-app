package app.flowkind.microservices.api.composite.product;

public record RecommendationSummary(int recommendationID, String author, int rate,String content) {
    public RecommendationSummary() {
        this(0,null,0,null);
    }
}

//public class RecommendationSummary {
//
//    private final int recommendationID;
//    private final String author;
//    private final int rate;
//    private final String content;
//
//    public RecommendationSummary() {
//        this.recommendationID = 0;
//        this.author = null;
//        this.rate = 0;
//        this.content = null;
//    }
//
//    public RecommendationSummary(int recommendationID, String author, int rate, String content) {
//        this.recommendationID = recommendationID;
//        this.author = author;
//        this.rate = rate;
//        this.content = content;
//    }
//
//    public int getRecommendationID() {
//        return recommendationID;
//    }
//
//    public String getAuthor() {
//        return author;
//    }
//
//    public int getRate() {
//        return rate;
//    }
//
//    public String getContent() {
//        return content;
//    }
//}