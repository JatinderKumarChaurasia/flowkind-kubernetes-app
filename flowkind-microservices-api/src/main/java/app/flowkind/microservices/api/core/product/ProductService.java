package app.flowkind.microservices.api.core.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductService {

    /**
     * Sample usage: "curl $HOST:$PORT/product/1".
     *
     * @param productID ID of the product
     * @return the product, if found, else null
     */
    @GetMapping(value = "/product/{productID}" , produces = "application/json")
    Product getProduct(@PathVariable int productID);
}
