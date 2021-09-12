package app.flowkind.microservices.core.product.services;

import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.api.core.product.ProductService;
import app.flowkind.microservices.api.exceptions.InvalidInputException;
import app.flowkind.microservices.api.exceptions.NotFoundException;
import app.flowkind.microservices.utils.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Product getProduct(int productID) {
        LOGGER.debug("/product return the found product for productID= {}", productID);
        if (productID < 1) {
            throw new InvalidInputException("Invalid productID: " + productID);
        }
        if (productID == 13) {
            throw new NotFoundException("No product found for productID: " + productID);
        }
        return new Product(productID,"name-"+productID,123,serviceUtil.getServiceAddress());
    }
}
