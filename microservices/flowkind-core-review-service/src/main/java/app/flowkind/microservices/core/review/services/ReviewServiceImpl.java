package app.flowkind.microservices.core.review.services;

import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.api.core.review.ReviewService;
import app.flowkind.microservices.api.exceptions.InvalidInputException;
import app.flowkind.microservices.core.review.persistence.ReviewEntity;
import app.flowkind.microservices.core.review.persistence.ReviewRepository;
import app.flowkind.microservices.utils.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, ReviewMapper reviewMapper,ServiceUtil serviceUtil) {
        this.reviewMapper = reviewMapper;
        this.reviewRepository = reviewRepository;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Review> getReviews(int productID) {
        if (productID < 1) {
            throw new InvalidInputException("Invalid productID: " + productID);
        }
        List<ReviewEntity> reviewEntities = reviewRepository.findByProductID(productID);
        List<Review> reviews = reviewMapper.reviewEntityListToReviewApiList(reviewEntities);
        reviews.forEach(review -> review.setServiceAddress(serviceUtil.getServiceAddress()));
        LOGGER.debug("getReviews: response size: {}", reviews.size());
        LOGGER.info("reviews array: {}",reviews);
        return reviews;
    }

    @Override
    public Review createReview(Review review) {
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
    public void deleteReviews(int productID) {
        LOGGER.debug("deleteReviews: Deleting reviews for the product with productID: {}",productID);
        reviewRepository.deleteAll(reviewRepository.findByProductID(productID));
    }
}
