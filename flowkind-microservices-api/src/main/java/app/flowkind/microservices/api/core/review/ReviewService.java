package app.flowkind.microservices.api.core.review;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ReviewService {

    /**
     * Sample usage: "curl $HOST:$PORT/review?productId=1".
     *
     * @param productID Id of the product
     * @return the reviews of the product
     */
    @GetMapping(value = "/review",produces = "application/json")
    List<Review> getReviews (@RequestParam(value = "productID",required = true) int productID);

    /**
     * Sample usage, see below.
     *
     * curl -X POST $HOST:$PORT/review \
     *   -H "Content-Type: application/json" --data \
     *   '{"productID":123,"reviewID":456,"author":"me","subject":"yada, yada, yada","content":"yada, yada, yada"}'
     *
     * @param review A JSON representation of the new review
     * @return A JSON representation of the newly created review
     */
    @PostMapping(
            value    = "/review",
            consumes = "application/json",
            produces = "application/json")
    Review createReview(@RequestBody Review review);

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/review?productID=1".
     *
     * @param productID Id of the product
     */
    @DeleteMapping(value = "/review")
    void deleteReviews(@RequestParam(value = "productID", required = true)  int productID);
}
