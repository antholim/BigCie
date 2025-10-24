package bigcie.bigcie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BigcieApplication {

	public static void main(String[] args) {
		SpringApplication.run(BigcieApplication.class, args);
	}

}
