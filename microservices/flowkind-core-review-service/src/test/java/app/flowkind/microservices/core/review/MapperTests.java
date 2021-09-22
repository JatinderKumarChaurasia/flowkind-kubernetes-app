package app.flowkind.microservices.core.review;

import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.core.review.persistence.ReviewEntity;
import app.flowkind.microservices.core.review.services.ReviewMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperTests {

    private final ReviewMapper reviewMapper = Mappers.getMapper(ReviewMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(reviewMapper);
        Review review = new Review(1,2,"author","subject","content","serviceAddress");
        ReviewEntity reviewEntity = reviewMapper.reviewApiToReviewEntity(review);
        assertEquals(review.getProductID(), reviewEntity.getProductID());
        assertEquals(review.getReviewID(), reviewEntity.getReviewID());
        assertEquals(review.getAuthor(), reviewEntity.getAuthor());
        assertEquals(review.getSubject(), reviewEntity.getSubject());
        assertEquals(review.getContent(), reviewEntity.getContent());
        // converting back
        Review review1 = reviewMapper.reviewEntityToReviewApi(reviewEntity);
        assertEquals(review.getProductID(), review1.getProductID());
        assertEquals(review.getReviewID(), review1.getReviewID());
        assertEquals(review.getAuthor(), review1.getAuthor());
        assertEquals(review.getSubject(), review1.getSubject());
        assertEquals(review.getContent(), review1.getContent());
        assertNull(review1.getServiceAddress());
    }

    @Test
    void mapperListTests() {
       assertNotNull(reviewMapper);
        Review review = new Review(1,2,"author","subject","content","serviceAddress");
        List<Review> reviews = Collections.singletonList(review);
        List<ReviewEntity> reviewEntities = reviewMapper.reviewApiListToReviewEntityList(reviews);
        assertEquals(reviewEntities.size(),reviews.size());
        ReviewEntity reviewEntity = reviewEntities.get(0);
        assertEquals(review.getProductID(), reviewEntity.getProductID());
        assertEquals(review.getReviewID(), reviewEntity.getReviewID());
        assertEquals(review.getAuthor(), reviewEntity.getAuthor());
        assertEquals(review.getSubject(), reviewEntity.getSubject());
        assertEquals(review.getContent(), reviewEntity.getContent());

        List<Review> reviews1 = reviewMapper.reviewEntityListToReviewApiList(reviewEntities);
        assertEquals(reviews.size(), reviews.size());
        Review review1 = reviews1.get(0);

        assertEquals(review.getProductID(), review1.getProductID());
        assertEquals(review.getReviewID(), review1.getReviewID());
        assertEquals(review.getAuthor(), review1.getAuthor());
        assertEquals(review.getSubject(), review1.getSubject());
        assertEquals(review.getContent(), review1.getContent());
        assertNull(review1.getServiceAddress());
    }
}
