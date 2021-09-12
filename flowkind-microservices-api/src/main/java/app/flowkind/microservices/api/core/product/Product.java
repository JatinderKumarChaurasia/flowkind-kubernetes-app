package app.flowkind.microservices.api.core.product;

public record Product( int productID,String name,int weight,String serviceAddress ) {
    public Product() {
        this(0,null,0,null);
    }
}
