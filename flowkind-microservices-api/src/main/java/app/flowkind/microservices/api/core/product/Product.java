package app.flowkind.microservices.api.core.product;

//public record Product( int productID,String name,int weight,String serviceAddress ) {
//    public Product() {
//        this(0,null,0,null);
//    }
//}

public class Product {
    private int productID;
    private String name;
    private int weight;
    private String serviceAddress;

    public Product() {
        productID = 0;
        name = null;
        weight = 0;
        serviceAddress = null;
    }

    public Product(int productID, String name, int weight, String serviceAddress) {
        this.productID = productID;
        this.name = name;
        this.weight = weight;
        this.serviceAddress = serviceAddress;
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

    public String getServiceAddress() {
        return serviceAddress;
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

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}

