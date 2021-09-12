package app.flowkind.microservices.core.review.services;

import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.api.core.review.ReviewService;
import app.flowkind.microservices.api.exceptions.InvalidInputException;
import app.flowkind.microservices.utils.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Review> getReviews(int productID) {
        if (productID < 1) {
            throw new InvalidInputException("Invalid productID: " + productID);
        }
        if (productID == 213) {
            LOGGER.debug("No reviews found for productID: {}", productID);
            return new ArrayList<>();
        }
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review(productID, 1, "Author 1", "Subject 1", "Content 1", serviceUtil.getServiceAddress()));
        reviews.add(new Review(productID, 2, "Author 2", "Subject 2", "Content 2", serviceUtil.getServiceAddress()));
        reviews.add(new Review(productID, 3, "Author 3", "Subject 3", "Content 3", serviceUtil.getServiceAddress()));
        LOGGER.debug("/reviews response size: {}", reviews.size());
        LOGGER.info("reviews array: {}",reviews);

        return reviews;
    }
}
