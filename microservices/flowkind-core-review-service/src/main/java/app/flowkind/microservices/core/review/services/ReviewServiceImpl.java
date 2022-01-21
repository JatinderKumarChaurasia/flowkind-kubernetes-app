package app.flowkind.microservices.core.review.services;

import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.api.core.review.ReviewService;
import app.flowkind.microservices.utils.exceptions.InvalidInputException;
import app.flowkind.microservices.core.review.persistence.ReviewEntity;
import app.flowkind.microservices.core.review.persistence.ReviewRepository;
import app.flowkind.microservices.utils.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.logging.Level;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final Scheduler jdbcScheduler;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, ReviewMapper reviewMapper,ServiceUtil serviceUtil,@Qualifier("jdbcScheduler") Scheduler jdbcScheduler) {
        this.reviewMapper = reviewMapper;
        this.reviewRepository = reviewRepository;
        this.serviceUtil = serviceUtil;
        this.jdbcScheduler = jdbcScheduler;
    }

    @Override
    public Flux<Review> getReviews(int productID) {
        if (productID < 1) {
            throw new InvalidInputException("Invalid productID: " + productID);
        }
        LOGGER.info("Will get reviews for product with id={}", productID);
        return Mono.fromCallable(()-> getReviewsInternal(productID)).flatMapMany(Flux::fromIterable).log(LOGGER.getName(), Level.FINE).subscribeOn(jdbcScheduler);
    }

    private List<Review> getReviewsInternal(int productID) {
        List<ReviewEntity> reviewEntities = reviewRepository.findByProductID(productID);
        List<Review> reviews = reviewMapper.reviewEntityListToReviewApiList(reviewEntities);
        reviews.forEach(review -> review.setServiceAddress(serviceUtil.getServiceAddress()));
        LOGGER.debug("getReviews: response size: {}", reviews.size());
        LOGGER.info("reviews array: {}",reviews);
        return reviews;
    }

    @Override
    public Mono<Review> createReview(Review review) {
        if (review.getProductID() < 1) {
            throw new InvalidInputException("Invalid productID: " + review.getProductID());
        }
        return Mono.fromCallable(() -> createReviewInternal(review)).subscribeOn(jdbcScheduler);
    }

    private Review createReviewInternal(Review review) {
        try {
            ReviewEntity reviewEntity = reviewMapper.reviewApiToReviewEntity(review);
            ReviewEntity afterSaveEntity = reviewRepository.save(reviewEntity);
            LOGGER.debug("createReview: created a review entity: {}/{}", review.getProductID(), review.getReviewID());
            return reviewMapper.reviewEntityToReviewApi(afterSaveEntity);

        } catch (DataIntegrityViolationException exception) {
            throw new InvalidInputException("Duplicate key, Product Id: " + review.getProductID() + ", Review Id:" + review.getReviewID());
        }
    }

    @Override
    public Mono<Void> deleteReviews(int productID) {
        if (productID < 1) {
            throw new InvalidInputException("Invalid productID: " + productID);
        }
        return Mono.fromRunnable(() -> deleteReviewInternal(productID)).subscribeOn(jdbcScheduler).then();
    }

    private void deleteReviewInternal(int productID) {
        LOGGER.debug("deleteReviews: Deleting reviews for the product with productID: {}",productID);
        reviewRepository.deleteAll(reviewRepository.findByProductID(productID));
    }
}
