package app.flowkind.microservices.core.recommendation.services;

import app.flowkind.microservices.api.core.recommendation.Recommendation;
import app.flowkind.microservices.api.core.recommendation.RecommendationService;
import app.flowkind.microservices.api.exceptions.InvalidInputException;
import app.flowkind.microservices.utils.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }


    @Override
    public List<Recommendation> getRecommendations(int productID) {
        if (productID < 1) {
            throw new InvalidInputException("Invalid productID: "+productID);
        }
        if (productID == 113) {
            LOGGER.debug("No recommendations found for productID: {}", productID);
            return new ArrayList<>();
        }
        List<Recommendation> recommendationList = new ArrayList<>();
        recommendationList.add(new Recommendation(productID, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
        recommendationList.add(new Recommendation(productID, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
        recommendationList.add(new Recommendation(productID, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));
        return recommendationList;
    }
}
