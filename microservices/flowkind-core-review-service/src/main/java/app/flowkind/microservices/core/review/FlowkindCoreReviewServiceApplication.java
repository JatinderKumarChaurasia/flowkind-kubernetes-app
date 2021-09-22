package app.flowkind.microservices.core.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = "app.flowkind")
public class FlowkindCoreReviewServiceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowkindCoreReviewServiceApplication.class);
    public static void main(String[] args) {
        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(FlowkindCoreReviewServiceApplication.class, args);
        String mysqlURI= configurableApplicationContext.getEnvironment().getProperty("spring.datasource.url");
        LOGGER.info("Connected to MySQL: with uri {}",mysqlURI);
    }

}