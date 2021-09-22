package app.flowkind.microservices.core.review.services;

import app.flowkind.microservices.api.core.review.Review;
import app.flowkind.microservices.core.review.persistence.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "serviceAddress", ignore = true)
    Review reviewEntityToReviewApi(ReviewEntity reviewEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    ReviewEntity reviewApiToReviewEntity(Review review);

    List<Review> reviewEntityListToReviewApiList(List<ReviewEntity> reviewEntities);

    List<ReviewEntity> reviewApiListToReviewEntityList(List<Review> reviews);
}
