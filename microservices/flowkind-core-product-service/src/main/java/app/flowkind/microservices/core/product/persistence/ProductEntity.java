package app.flowkind.microservices.core.product.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "products")
public class ProductEntity {

    @Id
    private String id;

    @Version
    private  Integer version;
    @Indexed(unique = true) private int productID;
    private String name;
    private int weight;
    public ProductEntity(int productID, String name, int weight) {
        this.productID = productID;
        this.name = name;
        this.weight = weight;
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

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
