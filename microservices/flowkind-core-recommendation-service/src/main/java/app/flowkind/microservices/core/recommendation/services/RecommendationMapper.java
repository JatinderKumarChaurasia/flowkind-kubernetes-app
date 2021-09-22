package app.flowkind.microservices.core.recommendation.services;

import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.core.recommendation.persistence.RecommendationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    @Mapping(target = "rate",source = "entity.rating")
    @Mapping(target = "serviceAddress",ignore = true)
    Recommendation recommendationEntityToRecommendationApi(RecommendationEntity entity);

    @Mapping(target = "rating", source = "rate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    RecommendationEntity recommendationApiToRecommendationEntity(Recommendation recommendation);

    List<Recommendation> recommendationEntityListToRecommendationList(List<RecommendationEntity> recommendationEntities);

    List<RecommendationEntity> recommendationListToRecommendationEntityList(List<Recommendation> recommendations);
}
