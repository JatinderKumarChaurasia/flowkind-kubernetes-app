package app.flowkind.microservices.core.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = {"app.flowkind"})

public class FlowkindCoreProductServiceApplication {
    // this is application
    public static void main(String[] args) {
        SpringApplication.run(FlowkindCoreProductServiceApplication.class, args);
    }

}
