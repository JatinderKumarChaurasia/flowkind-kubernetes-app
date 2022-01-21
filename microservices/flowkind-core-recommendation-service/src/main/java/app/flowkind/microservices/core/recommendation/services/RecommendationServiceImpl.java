package app.flowkind.microservices.core.recommendation.services;

import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.api.core.recommendation.RecommendationService;
import app.flowkind.microservices.utils.exceptions.InvalidInputException;
import app.flowkind.microservices.core.recommendation.persistence.RecommendationRepository;
import app.flowkind.microservices.utils.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

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
    public Flux<Recommendation> getRecommendations(int productID) {
        if (productID < 1) {
            throw new InvalidInputException("Invalid productID: "+productID);
        }
        return recommendationRepository.findByProductID(productID).log(LOGGER.getName(), Level.FINE).map(recommendationMapper::recommendationEntityToRecommendationApi).map(this::setServiceAddress);
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation recommendation) {
        if (recommendation.getProductID() < 1) {
            throw new InvalidInputException("Invalid productID: " + recommendation.getProductID());
        }
        return recommendationRepository.save(recommendationMapper.recommendationApiToRecommendationEntity(recommendation)).log(LOGGER.getName(),Level.FINE)
                .onErrorMap(DuplicateKeyException.class,exception-> new InvalidInputException("Duplicate key, Product Id: " + recommendation.getProductID() + ", Recommendation Id:" + recommendation.getRecommendationID())).map(recommendationMapper::recommendationEntityToRecommendationApi);
    }

    @Override
    public Mono<Void> deleteRecommendations(int productID) {
        if (productID < 1) {
            throw new InvalidInputException("Invalid productID: " + productID);
        }
        LOGGER.debug("deleteRecommendations: tries to delete recommendations for the product with productID: {}", productID);
        return recommendationRepository.deleteAll(recommendationRepository.findByProductID(productID));
    }

    private Recommendation setServiceAddress(Recommendation recommendation) {
        recommendation.setServiceAddress(serviceUtil.getServiceAddress());
        return recommendation;
    }
}
