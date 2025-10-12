package vroong.laas.order.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
    scanBasePackages = {
        "vroong.laas.order.job",
        "vroong.laas.order.core",
        "vroong.laas.order.infrastructure"
    }
)
@EnableScheduling
public class JobApplication {

  public static void main(String[] args) {
    SpringApplication.run(JobApplication.class, args);
  }
}
