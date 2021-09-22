package app.flowkind.microservices.core.product;

import app.flowkind.microservices.core.product.persistence.ProductEntity;
import app.flowkind.microservices.core.product.persistence.ProductRepository;
import com.mongodb.DuplicateKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
class PersistenceTests {

    @Autowired
    private ProductRepository productRepository;
    private ProductEntity savedProductEntity;

    @BeforeEach
    void setUpDatabase() {
        productRepository.deleteAll();
        ProductEntity productEntity = new ProductEntity(1,"name",1);
        savedProductEntity = productRepository.save(productEntity);
        assertEqualsProduct(productEntity,savedProductEntity);
    }

    @Test
    void createDocument() {
        int productID=2;
        ProductEntity productEntity = new ProductEntity(productID,"name "+productID,2);
        productRepository.save(productEntity);
        ProductEntity productEntity1 = productRepository.findByProductID(productID).orElseThrow();
        assertEqualsProduct(productEntity, productEntity1);
        assertEquals(2, productRepository.count());
    }

    @Test
    void update() {
      savedProductEntity.setName("name2");
      productRepository.save(savedProductEntity);
      ProductEntity foundEntity = productRepository.findById(savedProductEntity.getId()).orElseThrow();
      assertEquals(1, (long)foundEntity.getVersion());
      assertEquals("name2", foundEntity.getName());
    }

    @Test
    void delete() {
        productRepository.delete(savedProductEntity);
        assertFalse(productRepository.existsById(savedProductEntity.getId()));
    }

    @Test
    void getByProductID() {
        Optional<ProductEntity> productEntity = productRepository.findByProductID(savedProductEntity.getProductID());
        assertTrue(productEntity.isPresent());
        assertEqualsProduct(savedProductEntity, productEntity.get());
    }

    @Test
    void duplicateError() {
        assertThrows(DuplicateKeyException.class, () -> {
            ProductEntity entity = new ProductEntity(savedProductEntity.getProductID(), "name", 1);
            productRepository.save(entity);
        });
    }

    @Test
    void optimisticLockError() {
        ProductEntity productEntity1 = productRepository.findById(savedProductEntity.getId()).orElseThrow();
        ProductEntity productEntity2 = productRepository.findById(savedProductEntity.getId()).orElseThrow();
        productEntity1.setName("n1");
        productRepository.save(productEntity1);
        assertThrows(OptimisticLockingFailureException.class, () -> {
            productEntity2.setName("n2");
            productRepository.save(productEntity2);
        });
        ProductEntity updatedProductEntity = productRepository.findById(savedProductEntity.getId()).orElseThrow();
        assertEquals(1, (int)updatedProductEntity.getVersion());
        assertEquals("n1", updatedProductEntity.getName());
    }

    @Test
    void paging() {
        productRepository.deleteAll();
        List<ProductEntity> productEntities = rangeClosed(1001, 1010).mapToObj(i -> new ProductEntity(i, "name " + i, i)).collect(Collectors.toList());
        productRepository.saveAll(productEntities);
        Pageable nextPage = PageRequest.of(0, 4, Sort.Direction.ASC, "productID");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
    }

    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
        Page<ProductEntity> productPage = productRepository.findAll(nextPage);
        assertEquals(expectedProductIds, productPage.getContent().stream().map(ProductEntity::getProductID).collect(Collectors.toList()).toString());
        assertEquals(expectsNextPage, productPage.hasNext());
        return productPage.nextPageable();
    }

    private void assertEqualsProduct(ProductEntity expectedProductEntity, ProductEntity actualProductEntity) {
        assertEquals(expectedProductEntity.getId(),actualProductEntity.getId());
        assertEquals(expectedProductEntity.getVersion(),actualProductEntity.getVersion());
        assertEquals(expectedProductEntity.getProductID(),actualProductEntity.getProductID());
        assertEquals(expectedProductEntity.getName(),actualProductEntity.getName());
        assertEquals(expectedProductEntity.getWeight(),actualProductEntity.getWeight());
    }

}
