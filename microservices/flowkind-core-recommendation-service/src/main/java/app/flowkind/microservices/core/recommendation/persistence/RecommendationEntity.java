package app.flowkind.microservices.core.recommendation.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "recommendations")
@CompoundIndex(name = "prod-rec-id", unique = true, def = "{'productID': 1, 'recommendationID' : 1}")
public class RecommendationEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    private int productID;
    private int recommendationID;
    private String author;
    private int rating;
    private String content;

    public RecommendationEntity() {
    }

    public RecommendationEntity(int productID, int recommendationID, String author, int rating, String content) {
        this.productID = productID;
        this.recommendationID = recommendationID;
        this.author = author;
        this.rating = rating;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public Integer getVersion() {
        return version;
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

    public int getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setContent(String content) {
        this.content = content;
    }
}