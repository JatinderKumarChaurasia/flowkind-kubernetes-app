package app.flowkind.microservices.core.recommendation;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

abstract class MongoDbTestBase {
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    static {
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", mongoDBContainer::getContainerIpAddress);
        registry.add("spring.data.mongodb.port", () -> mongoDBContainer.getMappedPort(27017));
        registry.add("spring.data.mongodb.database", () -> "test");
    }
}
