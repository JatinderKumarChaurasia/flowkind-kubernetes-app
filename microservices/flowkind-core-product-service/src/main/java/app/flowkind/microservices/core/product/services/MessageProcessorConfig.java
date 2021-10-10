package app.flowkind.microservices.core.product.services;

import app.flowkind.microservices.api.event.Event;
import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.api.core.product.ProductService;
import app.flowkind.microservices.api.exceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessorConfig.class);
    private final ProductService productService;

    @Autowired
    public MessageProcessorConfig(ProductService productService) {
        this.productService = productService;
    }

    @Bean
    public Consumer<Event<Integer, Product>> messageProcessor() {
        return integerProductEvent -> {
            LOGGER.info("processor message created at :"+integerProductEvent.getEventCreatedAt());
            switch (integerProductEvent.eventType()) {
                case CREATE -> {
                    Product product = integerProductEvent.data();
                    LOGGER.info("Create product with ID: {}", product.getProductID());
                    productService.createProduct(product).block();
                }
                case DELETE -> {
                    int productID = integerProductEvent.key();
                    LOGGER.info("Delete recommendations with ProductID: {}", productID);
                    productService.deleteProduct(productID).block();
                }
                default -> {
                    String errorMessage = "Incorrect event type: " + integerProductEvent.eventType() + ", expected a CREATE or DELETE event";
                    LOGGER.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }
            LOGGER.info("Message processing done!");
        };
    }
}
