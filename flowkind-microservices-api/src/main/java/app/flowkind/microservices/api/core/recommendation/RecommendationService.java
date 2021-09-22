package app.flowkind.microservices.api.core.recommendation;

import org.springframework.web.bind.annotation.*;

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

    /**
     * Sample usage, see below.
     *
     * curl -X POST $HOST:$PORT/recommendation \
     *   -H "Content-Type: application/json" --data \
     *   '{"productID":123,"recommendationID":456,"author":"me","rate":5,"content":"yada, yada, yada"}'
     *
     * @param recommendation A JSON representation of the new recommendation
     * @return A JSON representation of the newly created recommendation
     */
    @PostMapping(value    = "/recommendation", consumes = "application/json", produces = "application/json")
    Recommendation createRecommendation(@RequestBody Recommendation recommendation);

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/recommendation?productID=1".
     *
     * @param productID Id of the product
     */
    @DeleteMapping(value = "/recommendation")
    void deleteRecommendations(@RequestParam(value = "productID", required = true)  int productID);
}
