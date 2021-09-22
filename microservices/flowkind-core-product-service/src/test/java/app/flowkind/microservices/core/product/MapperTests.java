package app.flowkind.microservices.core.product;

import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.core.product.persistence.ProductEntity;
import app.flowkind.microservices.core.product.services.ProductMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapperTests {

    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(productMapper);

        Product product = new Product(1, "name", 1, "serviceAddress");

        ProductEntity productEntity = productMapper.productApiToProductEntity(product);

        assertEquals(product.getProductID(), productEntity.getProductID());
        assertEquals(product.getName(), productEntity.getName());
        assertEquals(product.getWeight(), productEntity.getWeight());

        Product product1 = productMapper.productEntityToProductApi(productEntity);

        assertEquals(product.getProductID(),product1.getProductID());
        assertEquals(product.getProductID(),product1.getProductID());
        assertEquals(product.getName(),product1.getName());
        assertEquals(product.getWeight(),product1.getWeight());
        assertNull(product1.getServiceAddress());
    }

    void mapperListTests() {

    }

}
