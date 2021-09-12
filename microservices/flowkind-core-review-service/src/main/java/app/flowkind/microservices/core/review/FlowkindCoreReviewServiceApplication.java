package app.flowkind.microservices.core.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = "app.flowkind")
public class FlowkindCoreReviewServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowkindCoreReviewServiceApplication.class, args);
    }

}
