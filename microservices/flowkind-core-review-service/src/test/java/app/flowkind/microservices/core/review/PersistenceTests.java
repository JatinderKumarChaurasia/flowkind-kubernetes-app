package app.flowkind.microservices.core.review;


import app.flowkind.microservices.core.review.persistence.ReviewEntity;
import app.flowkind.microservices.core.review.persistence.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersistenceTests extends MySqlTestBase{

    @Autowired
    private ReviewRepository reviewRepository;
    private ReviewEntity savedEntity;

    @BeforeEach
    void setUpDatabase() {
        reviewRepository.deleteAll();
        ReviewEntity reviewEntity = new ReviewEntity(1,2,"author","subject","content");
        savedEntity= reviewRepository.save(reviewEntity);
        assertEqualsReview(reviewEntity, savedEntity);
    }

    @Test
    void create() {
        ReviewEntity reviewEntity = new ReviewEntity(1,3,"author","subject","content");
        reviewRepository.save(reviewEntity);
        ReviewEntity foundEntity = reviewRepository.findById(reviewEntity.getId()).orElseThrow();
        assertEqualsReview(foundEntity, foundEntity);
        assertEquals(2, reviewRepository.count());
    }

    @Test
    void update() {
        savedEntity.setAuthor("author2");
        reviewRepository.save(savedEntity);
        ReviewEntity foundEntity = reviewRepository.findById(savedEntity.getId()).orElseThrow();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("author2", foundEntity.getAuthor());
    }

    @Test
    void delete() {
        reviewRepository.delete(savedEntity);
        assertFalse(reviewRepository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        List<ReviewEntity> entityList = reviewRepository.findByProductID(savedEntity.getProductID());
        assertThat(entityList, hasSize(1));
        assertEqualsReview(savedEntity, entityList.get(0));
    }

    @Test
    void duplicateError() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            ReviewEntity reviewEntity = new ReviewEntity(1,2,"author","subject","content");
            reviewRepository.save(reviewEntity);
        });
    }

    @Test
    void optimisticLockError() {
        ReviewEntity reviewEntity1 = reviewRepository.findById(savedEntity.getId()).orElseThrow();
        ReviewEntity reviewEntity2 = reviewRepository.findById(savedEntity.getId()).orElseThrow();
        reviewEntity1.setAuthor("a1");
        reviewRepository.save(reviewEntity1);
        assertThrows(OptimisticLockingFailureException.class, () -> {
            reviewEntity2.setAuthor("a2");
            reviewRepository.save(reviewEntity2);
        });
        ReviewEntity updatedReviewEntity = reviewRepository.findById(savedEntity.getId()).orElseThrow();

        assertEquals(1, updatedReviewEntity.getVersion());
        assertEquals("a1", updatedReviewEntity.getAuthor());
    }

    private void assertEqualsReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
        assertEquals(expectedEntity.getId(),        actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),   actualEntity.getVersion());
        assertEquals(expectedEntity.getProductID(), actualEntity.getProductID());
        assertEquals(expectedEntity.getReviewID(),  actualEntity.getReviewID());
        assertEquals(expectedEntity.getAuthor(),    actualEntity.getAuthor());
        assertEquals(expectedEntity.getSubject(),   actualEntity.getSubject());
        assertEquals(expectedEntity.getContent(),   actualEntity.getContent());
    }
}
