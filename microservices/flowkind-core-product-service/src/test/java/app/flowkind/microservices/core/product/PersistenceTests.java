package app.flowkind.microservices.core.product;

import app.flowkind.microservices.core.product.persistence.ProductEntity;
import app.flowkind.microservices.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;

@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
class PersistenceTests {

    @Autowired
    private ProductRepository productRepository;
    private ProductEntity savedProductEntity;

    @BeforeEach
    void setUpDatabase() {
        StepVerifier.create(productRepository.deleteAll()).verifyComplete();
        ProductEntity productEntity = new ProductEntity(1,"name",1);
        StepVerifier.create(productRepository.save(productEntity)).expectNextMatches(createdEntity -> {
            savedProductEntity = createdEntity;
            return assertEqualsProduct(savedProductEntity,productEntity);
        }).verifyComplete();
    }

    @Test
    void create() {
        int productID=2;
        ProductEntity productEntity = new ProductEntity(productID,"name "+productID,2);
        StepVerifier.create(productRepository.save(productEntity)).expectNextMatches(createdProductEntity -> assertEqualsProduct(createdProductEntity,productEntity))
                .verifyComplete();
        StepVerifier.create(productRepository.count()).expectNext(2L).verifyComplete();
    }

    @Test
    void update() {
      savedProductEntity.setName("name2");
      StepVerifier.create(productRepository.save(savedProductEntity)).expectNextMatches(updatedProductEntity -> updatedProductEntity.getName().equals("name2")).verifyComplete();
      StepVerifier.create(productRepository.findById(savedProductEntity.getId())).expectNextMatches(foundProductEntity ->
          foundProductEntity.getVersion() == 1 && foundProductEntity.getName().equals("name2")
      ).verifyComplete();
    }

    @Test
    void delete() {
        StepVerifier.create(productRepository.delete(savedProductEntity)).verifyComplete();
        StepVerifier.create(productRepository.existsById(savedProductEntity.getId())).expectNext(false).verifyComplete();
    }

    @Test
    void getByProductID() {
        StepVerifier.create(productRepository.findByProductID(savedProductEntity.getProductID())).expectNextMatches(foundProductEntity -> assertEqualsProduct(foundProductEntity,savedProductEntity)).verifyComplete();
    }

    @Test
    void duplicateError() {
        ProductEntity productEntity = new ProductEntity(savedProductEntity.getProductID(), "name", 1);
        StepVerifier.create(productRepository.save(productEntity)).expectError(DuplicateKeyException.class).verify();
    }

    @Test
    void optimisticLockError() {
        ProductEntity productEntity1 = productRepository.findById(savedProductEntity.getId()).block();
        ProductEntity productEntity2 = productRepository.findById(savedProductEntity.getId()).block();
        assert productEntity1 != null && productEntity2 != null;
        productEntity1.setName("n1");
        productRepository.save(productEntity1).block();
        StepVerifier.create(productRepository.save(productEntity2)).expectError(OptimisticLockingFailureException.class).verify();
        StepVerifier.create(productRepository.findById(savedProductEntity.getId())).expectNextMatches(productEntity -> productEntity.getVersion() == 1 && productEntity.getName().equals("n1")).verifyComplete();
    }

    private boolean assertEqualsProduct(ProductEntity expectedProductEntity, ProductEntity actualProductEntity) {
        return (expectedProductEntity.getId().equals(actualProductEntity.getId())) && (expectedProductEntity.getVersion().equals(actualProductEntity.getVersion()))
                && (expectedProductEntity.getProductID() == actualProductEntity.getProductID())
                && (expectedProductEntity.getName().equals( actualProductEntity.getName()))
                && (expectedProductEntity.getWeight() == actualProductEntity.getWeight());
    }

}
