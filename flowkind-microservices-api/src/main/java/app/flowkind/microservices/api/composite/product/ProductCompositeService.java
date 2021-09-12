package app.flowkind.microservices.api.composite.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductCompositeService {

    /**
     * Sample usage: "curl $HOST:$PORT/product-composite/1".
     *
     * @param productID Id of the product
     * @return the composite product info, if found, else null
     */
    @GetMapping(value = "/product-composite/{productID}",produces = "application/json")
    ProductAggregate getProduct(@PathVariable int productID);
}
