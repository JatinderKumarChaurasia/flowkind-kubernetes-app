package app.flowkind.microservices.core.recommendation;

import app.flowkind.microservices.core.recommendation.persistence.RecommendationEntity;
import app.flowkind.microservices.core.recommendation.persistence.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
class PersistenceTests {
    @Autowired
    private RecommendationRepository recommendationRepository;
    private RecommendationEntity savedEntity;

    @BeforeEach
    void setupDb() {
        recommendationRepository.deleteAll().block();
        RecommendationEntity recommendationEntity = new RecommendationEntity(1, 2, "a", 3, "c");
        savedEntity = recommendationRepository.save(recommendationEntity).block();
        assert savedEntity != null;
        assertEqualsRecommendation(recommendationEntity, savedEntity);
    }

    @Test
    void create() {
        RecommendationEntity recommendationEntity = new RecommendationEntity(1, 3, "a", 3, "c");
        recommendationRepository.save(recommendationEntity).block();
        RecommendationEntity foundEntity = recommendationRepository.findById(recommendationEntity.getId()).block();
        assert foundEntity != null;
        assertEqualsRecommendation(recommendationEntity, foundEntity);
        assertEquals(2, recommendationRepository.count().block());
    }

    @Test
    void update() {
        savedEntity.setAuthor("a2");
        recommendationRepository.save(savedEntity).block();
        RecommendationEntity foundEntity = recommendationRepository.findById(savedEntity.getId()).block();
        assert foundEntity != null;
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
    void delete() {
        recommendationRepository.delete(savedEntity).block();
        assertNotEquals(Boolean.TRUE, recommendationRepository.existsById(savedEntity.getId()).block());
    }

    @Test
    void getByProductId() {
        List<RecommendationEntity> recommendationEntities = recommendationRepository.findByProductID(savedEntity.getProductID()).collectList().block();
        assertThat(recommendationEntities, hasSize(1));
        assertEqualsRecommendation(savedEntity, recommendationEntities.get(0));
    }

    @Test
    void duplicateError() {
        assertThrows(DuplicateKeyException.class, () -> {
            RecommendationEntity recommendationEntity = new RecommendationEntity(1, 2, "a", 3, "c");
            recommendationRepository.save(recommendationEntity).block();
        });
    }

    @Test
    void optimisticLockError() {
        RecommendationEntity recommendationEntity1 = recommendationRepository.findById(savedEntity.getId()).block();
        RecommendationEntity recommendationEntity2 = recommendationRepository.findById(savedEntity.getId()).block();
        assert recommendationEntity1 != null && recommendationEntity2 != null;
        recommendationEntity1.setAuthor("a1");
        recommendationRepository.save(recommendationEntity1).block();
        assertThrows(OptimisticLockingFailureException.class, () -> {
            recommendationEntity2.setAuthor("a2");
            recommendationRepository.save(recommendationEntity2).block();
        });
        RecommendationEntity updatedEntity = recommendationRepository.findById(savedEntity.getId()).block();
        assert updatedEntity != null;
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }

    private void assertEqualsRecommendation(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductID(), actualEntity.getProductID());
        assertEquals(expectedEntity.getRecommendationID(), actualEntity.getRecommendationID());
        assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        assertEquals(expectedEntity.getRating(), actualEntity.getRating());
        assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    }
}
