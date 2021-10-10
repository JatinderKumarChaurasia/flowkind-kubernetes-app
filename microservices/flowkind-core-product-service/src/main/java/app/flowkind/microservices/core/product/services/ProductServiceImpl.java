package app.flowkind.microservices.core.product.services;

import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.api.core.product.ProductService;
import app.flowkind.microservices.api.exceptions.InvalidInputException;
import app.flowkind.microservices.api.exceptions.NotFoundException;
import app.flowkind.microservices.core.product.persistence.ProductEntity;
import app.flowkind.microservices.core.product.persistence.ProductRepository;
import app.flowkind.microservices.utils.http.ServiceUtil;
import org.springframework.dao.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

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
    public Mono<Product> getProduct(int productID) {
        if (productID < 1) {
            throw new InvalidInputException("Invalid productID: " + productID);
        }
        LOGGER.info("Will get product info for id={}", productID);
        return productRepository.findByProductID(productID)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productID: " + productID))).log(LOGGER.getName(), Level.FINE).map(productMapper::productEntityToProductApi).map(this::setServiceAddress);
    }

    @Override
    public Mono<Product> createProduct(Product product) {
        if (product.getProductID() < 1) {
            throw new InvalidInputException("Invalid productID: " + product.getProductID());
        }
        ProductEntity productEntity = productMapper.productApiToProductEntity(product);
        Mono<Product> productMono = productRepository.save(productEntity).log(LOGGER.getName(),Level.FINE).onErrorMap(DuplicateKeyException.class, ex -> new InvalidInputException("Duplicate key, Product Id: " + product.getProductID())).map(productMapper::productEntityToProductApi);
        LOGGER.debug("createProduct: entity created for productID: {}", product.getProductID());
        return productMono;
    }

    @Override
    public Mono<Void> deleteProduct(int productID) {
        if (productID < 1) {
            throw new InvalidInputException("Invalid productID: " + productID);
        }
        LOGGER.debug("deleteProduct: tries to delete an entity with productID: {}", productID);
        return productRepository.findByProductID(productID).log(LOGGER.getName(),Level.FINE).map(productRepository::delete).flatMap(e->e);
    }

    private Product setServiceAddress(Product product) {
        product.setServiceAddress(serviceUtil.getServiceAddress());
        return product;
    }
}
