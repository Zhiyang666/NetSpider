package lianjianspider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author yuziyang
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SpiderApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpiderApplication.class, args);
    }
}
