package app.flowkind.microservices.core.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
@ComponentScan(value = "app.flowkind")
public class FlowkindCoreReviewServiceApplication {

    private final Integer threadPoolSize;
    private final Integer taskQueueSize;

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowkindCoreReviewServiceApplication.class);

    @Autowired
    public FlowkindCoreReviewServiceApplication(@Value("${app.threadPoolSize:10}") Integer threadPoolSize,@Value("${app.taskQueueSize:100}") Integer taskQueueSize) {
        this.threadPoolSize = threadPoolSize;
        this.taskQueueSize = taskQueueSize;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(FlowkindCoreReviewServiceApplication.class, args);
        String mysqlURI= configurableApplicationContext.getEnvironment().getProperty("spring.datasource.url");
        LOGGER.info("Connected to MySQL: with uri {}",mysqlURI);
    }

    @Bean
    public Scheduler jdbcScheduler() {
        LOGGER.info("Creates a jdbcScheduler with thread pool size = {}", threadPoolSize);
        return Schedulers.newBoundedElastic(threadPoolSize,taskQueueSize,"jdbc-pool");
    }

}