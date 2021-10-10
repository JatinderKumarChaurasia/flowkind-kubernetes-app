package app.flowkind.microservices.api.core.product;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface ProductService {

    /**
     * Sample usage: "curl $HOST:$PORT/product/1".
     *
     * @param productID ID of the product
     * @return the product, if found, else null
     */
    @GetMapping(value = "/product/{productID}" , produces = "application/json")
    Mono<Product> getProduct(@PathVariable int productID);

    Mono<Product> createProduct(Product product);

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/product/1".
     *
     * @param productID Id of the product
     */
    @DeleteMapping(value = "/product/{productID}")
    Mono<Void> deleteProduct(@PathVariable int productID);
}
