package app.flowkind.microservices.core.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = "app.flowkind")
public class FlowkindCoreRecommendationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowkindCoreRecommendationServiceApplication.class, args);
    }

}
