package app.flowkind.microservices.compose.product;

import app.flowkind.microservices.compose.product.services.ProductCompositeIntegration;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootApplication
@ComponentScan("app.flowkind")
public class FlowkindProductComposeServiceApplication {

    @Value("${api.common.version}") String apiVersion;
    @Value("${api.common.title}")           String apiTitle;
    @Value("${api.common.description}")     String apiDescription;
    @Value("${api.common.termsOfService}")  String apiTermsOfService;
    @Value("${api.common.license}")         String apiLicense;
    @Value("${api.common.licenseURL}")      String apiLicenseUrl;
    @Value("${api.common.externalDocDesc}") String apiExternalDocDesc;
    @Value("${api.common.externalDocUrl}")  String apiExternalDocUrl;
    @Value("${api.common.contact.name}")    String apiContactName;
    @Value("${api.common.contact.url}")     String apiContactUrl;
    @Value("${api.common.contact.email}")   String apiContactEmail;
    private final Integer threadPoolSize;
    private final Integer taskQueueSize;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowkindProductComposeServiceApplication.class);

    @Autowired
    private ProductCompositeIntegration productCompositeIntegration;

    @Autowired
    public FlowkindProductComposeServiceApplication(@Value("${app.threadPoolSize:10}") Integer threadPoolSize, @Value("${app.taskQueueSize:100}") Integer taskQueueSize) {
        this.threadPoolSize = threadPoolSize;
        this.taskQueueSize = taskQueueSize;
    }


    /**
     * Will exposed on $HOST:$PORT/swagger-ui.html
     *
     * @return the common OpenAPI documentation
     */
    @Bean
    public OpenAPI getOpenAPIDocumentation() {
        return new OpenAPI().info(new Info().title(apiTitle).description(apiDescription).version(apiVersion).contact(new Contact().name(apiContactName).url(apiContactUrl).email(apiContactEmail)).termsOfService(apiTermsOfService).license(new License().name(apiLicense).url(apiLicenseUrl))).externalDocs(new ExternalDocumentation().url(apiExternalDocUrl).description(apiExternalDocDesc));
    }

    @Bean
    public Scheduler publishEventScheduler() {
        LOGGER.info("Creates a messagingScheduler with connectionPoolSize = {}", threadPoolSize);
        return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "publish-pool");
    }

    public static void main(String[] args) {
        SpringApplication.run(FlowkindProductComposeServiceApplication.class, args);
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }


    @Bean
    ReactiveHealthContributor coreServices() {
        final Map<String, ReactiveHealthIndicator> reactiveHealthIndicatorMap = new LinkedHashMap<>();
        reactiveHealthIndicatorMap.put("product",productCompositeIntegration::getProductHealth);
        reactiveHealthIndicatorMap.put("recommendation", productCompositeIntegration::getRecommendationHealth);
        reactiveHealthIndicatorMap.put("review", productCompositeIntegration::getReviewHealth);
        return CompositeReactiveHealthContributor.fromMap(reactiveHealthIndicatorMap);
    }

}
