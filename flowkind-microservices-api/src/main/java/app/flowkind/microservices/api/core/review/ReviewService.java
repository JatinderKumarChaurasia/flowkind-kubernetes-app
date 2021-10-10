package app.flowkind.microservices.api.core.review;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReviewService {

    /**
     * Sample usage: "curl $HOST:$PORT/review?productId=1".
     *
     * @param productID Id of the product
     * @return the reviews of the product
     */
    @GetMapping(value = "/review",produces = "application/json")
    Flux<Review> getReviews (@RequestParam(value = "productID",required = true) int productID);

    Mono<Review> createReview(Review review);

    Mono<Void> deleteReviews(int productID);
}
