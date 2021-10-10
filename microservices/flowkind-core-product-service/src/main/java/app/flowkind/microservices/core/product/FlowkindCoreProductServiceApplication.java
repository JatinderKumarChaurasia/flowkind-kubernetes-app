package app.flowkind.microservices.core.product;

import app.flowkind.microservices.core.product.persistence.ProductEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

@SpringBootApplication
@ComponentScan(value = {"app.flowkind"})
public class FlowkindCoreProductServiceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowkindCoreProductServiceApplication.class);

    @Autowired
    private ReactiveMongoOperations mongoOperations;
    public static void main(String[] args) {
        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(FlowkindCoreProductServiceApplication.class, args);
        String mongoDBHost=configurableApplicationContext.getEnvironment().getProperty("spring.data.mongodb.host");
        String mongoDBPort=configurableApplicationContext.getEnvironment().getProperty("spring.data.mongodb.port");
        LOGGER.info("Connected to MongoDb: {}:{}", mongoDBHost, mongoDBPort);
    }

    @EventListener(value = ContextRefreshedEvent.class)
    public void initIndicesAfterStartup() {
        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoOperations.getConverter().getMappingContext();
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);
        ReactiveIndexOperations indexOps = mongoOperations.indexOps(ProductEntity.class);
        resolver.resolveIndexFor(ProductEntity.class).forEach(indexOps::ensureIndex);
    }
}
