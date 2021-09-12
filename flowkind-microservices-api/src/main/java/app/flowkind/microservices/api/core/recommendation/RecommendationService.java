package app.flowkind.microservices.api.core.recommendation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface RecommendationService {
    /**
     * Sample usage: "curl $HOST:$PORT/recommendation?productId=1".
     *
     * @param productID ID of the product
     * @return the recommendations of the product
     */
    @GetMapping(value = "/recommendation", produces = "application/json")
    List<Recommendation> getRecommendations(@RequestParam(value = "productID",required = true) int productID);
}
