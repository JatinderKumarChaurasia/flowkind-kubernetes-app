package app.flowkind.microservices.core.product.services;

import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.api.core.product.ProductService;
import app.flowkind.microservices.api.exceptions.InvalidInputException;
import app.flowkind.microservices.api.exceptions.NotFoundException;
import app.flowkind.microservices.core.product.persistence.ProductEntity;
import app.flowkind.microservices.core.product.persistence.ProductRepository;
import app.flowkind.microservices.utils.http.ServiceUtil;
import com.mongodb.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,ProductMapper productMapper,ServiceUtil serviceUtil) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Product getProduct(int productID) {
        if (productID < 1) {
            throw new InvalidInputException("Invalid productID: " + productID);
        }
        ProductEntity entity = productRepository.findByProductID(productID)
                .orElseThrow(() -> new NotFoundException("No product found for productID: " + productID));
        Product response = productMapper.productEntityToProductApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());
        LOGGER.debug("getProduct: found productID: {}", response.getProductID());
        return response;
    }

    @Override
    public Product createProduct(Product product) {
        try {
            ProductEntity productEntity = productMapper.productApiToProductEntity(product);
            ProductEntity productEntity1 = productRepository.save(productEntity);
            LOGGER.debug("createProduct: entity created for productID: {}", product.getProductID());
            return productMapper.productEntityToProductApi(productEntity1);
        } catch (DuplicateKeyException exception) {
            throw new InvalidInputException("Duplicate key, Product Id: " + product.getProductID());
        }
    }

    @Override
    public void deleteProduct(int productID) {
        LOGGER.debug("deleteProduct: tries to delete an entity with productID: {}", productID);
        productRepository.findByProductID(productID).ifPresent(productRepository::delete);
    }
}
