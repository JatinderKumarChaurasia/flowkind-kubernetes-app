package app.flowkind.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class FlowkindEurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowkindEurekaServerApplication.class, args);
    }

}
