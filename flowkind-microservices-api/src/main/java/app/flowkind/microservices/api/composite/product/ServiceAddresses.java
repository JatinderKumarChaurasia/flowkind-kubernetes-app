package app.flowkind.microservices.api.composite.product;

public record ServiceAddresses(String compositeServiceAddress, String productServiceAddress,String reviewServiceAddress,String recommendationServiceAddress) {
    public ServiceAddresses() {
        this(null,null,null,null);
    }
}