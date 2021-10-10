package app.flowkind.microservices.api.core.recommendation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RecommendationService {
    /**
     * Sample usage: "curl $HOST:$PORT/recommendation?productId=1".
     *
     * @param productID ID of the product
     * @return the recommendations of the product
     */
    @GetMapping(value = "/recommendation", produces = "application/json")
    Flux<Recommendation> getRecommendations(@RequestParam(value = "productID",required = true) int productID);

    Mono<Recommendation> createRecommendation( Recommendation recommendation);

    Mono<Void> deleteRecommendations(int productID);
}
