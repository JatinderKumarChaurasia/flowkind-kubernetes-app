package app.flowkind.microservices.core.recommendation.services;

import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.api.core.recommendation.RecommendationService;
import app.flowkind.microservices.api.exceptions.InvalidInputException;
import app.flowkind.microservices.core.recommendation.persistence.RecommendationEntity;
import app.flowkind.microservices.core.recommendation.persistence.RecommendationRepository;
import app.flowkind.microservices.utils.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final RecommendationRepository recommendationRepository;
    private final RecommendationMapper recommendationMapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepository recommendationRepository,RecommendationMapper recommendationMapper,ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
        this.recommendationRepository = recommendationRepository;
        this.recommendationMapper = recommendationMapper;
    }

    @Override
    public List<Recommendation> getRecommendations(int productID) {
        if (productID < 1) {
            throw new InvalidInputException("Invalid productID: "+productID);
        }
        List<RecommendationEntity> recommendationEntities = recommendationRepository.findByProductID(productID);
        List<Recommendation> recommendations = recommendationMapper.recommendationEntityListToRecommendationList(recommendationEntities);
        recommendations.forEach(recommendation -> recommendation.setServiceAddress(serviceUtil.getServiceAddress()));
        return recommendations;
    }

    @Override
    public Recommendation createRecommendation(Recommendation recommendation) {
        try {
            RecommendationEntity entity = recommendationMapper.recommendationApiToRecommendationEntity(recommendation);
            RecommendationEntity savedEntity = recommendationRepository.save(entity);
            LOGGER.debug("createRecommendation: created a recommendation entity: {}/{}", recommendation.getProductID(), recommendation.getRecommendationID());
            return recommendationMapper.recommendationEntityToRecommendationApi(savedEntity);

        } catch (DuplicateKeyException duplicateKeyException) {
            throw new InvalidInputException("Duplicate key, Product Id: " + recommendation.getProductID() + ", Recommendation Id:" + recommendation.getRecommendationID());
        }
    }

    @Override
    public void deleteRecommendations(int productID) {
        LOGGER.debug("deleteRecommendations: tries to delete recommendations for the product with productID: {}", productID);
        recommendationRepository.deleteAll(recommendationRepository.findByProductID(productID));
    }
}
