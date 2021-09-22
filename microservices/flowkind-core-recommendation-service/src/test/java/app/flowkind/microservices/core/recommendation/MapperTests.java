package app.flowkind.microservices.core.recommendation;

import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.core.recommendation.persistence.RecommendationEntity;
import app.flowkind.microservices.core.recommendation.services.RecommendationMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperTests {

    private final RecommendationMapper recommendationMapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(recommendationMapper);
        Recommendation recommendation = new Recommendation(1, 2, "a", 4, "C", "adr");
        RecommendationEntity recommendationEntity = recommendationMapper.recommendationApiToRecommendationEntity(recommendation);
        assertEquals(recommendation.getProductID(), recommendationEntity.getProductID());
        assertEquals(recommendation.getRecommendationID(), recommendationEntity.getRecommendationID());
        assertEquals(recommendation.getAuthor(), recommendationEntity.getAuthor());
        assertEquals(recommendation.getRate(), recommendationEntity.getRating());
        assertEquals(recommendation.getContent(), recommendationEntity.getContent());
        Recommendation recommendation1 = recommendationMapper.recommendationEntityToRecommendationApi(recommendationEntity);
        assertEquals(recommendation.getProductID(), recommendation1.getProductID());
        assertEquals(recommendation.getRecommendationID(), recommendation1.getRecommendationID());
        assertEquals(recommendation.getAuthor(), recommendation1.getAuthor());
        assertEquals(recommendation.getRate(), recommendation1.getRate());
        assertEquals(recommendation.getContent(), recommendation1.getContent());
        assertNull(recommendation1.getServiceAddress());
    }

    @Test
    void mapperListTests() {
        assertNotNull(recommendationMapper);
        Recommendation recommendation = new Recommendation(1, 2, "a", 4, "C", "adr");
        List<Recommendation> recommendationList = Collections.singletonList(recommendation);
        List<RecommendationEntity> recommendationEntityList = recommendationMapper.recommendationListToRecommendationEntityList(recommendationList);
        assertEquals(recommendationList.size(), recommendationEntityList.size());
        RecommendationEntity recommendationEntity = recommendationEntityList.get(0);
        assertEquals(recommendation.getProductID(), recommendationEntity.getProductID());
        assertEquals(recommendation.getRecommendationID(), recommendationEntity.getRecommendationID());
        assertEquals(recommendation.getAuthor(), recommendationEntity.getAuthor());
        assertEquals(recommendation.getRate(), recommendationEntity.getRating());
        assertEquals(recommendation.getContent(), recommendationEntity.getContent());
        List<Recommendation> recommendations = recommendationMapper.recommendationEntityListToRecommendationList(recommendationEntityList);
        assertEquals(recommendations.size(), recommendationList.size());
        Recommendation recommendation1 = recommendations.get(0);
        assertEquals(recommendation.getProductID(), recommendation1.getProductID());
        assertEquals(recommendation.getRecommendationID(), recommendation1.getRecommendationID());
        assertEquals(recommendation.getAuthor(), recommendation1.getAuthor());
        assertEquals(recommendation.getRate(), recommendation1.getRate());
        assertEquals(recommendation.getContent(), recommendation1.getContent());
        assertNull(recommendation1.getServiceAddress());
    }

}
