package com.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;


@EnableKafka
@SpringBootApplication(
        scanBasePackages = "com.notification",
        exclude = {
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
        }
)
public class ApiNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiNotificationApplication.class, args);
	}

}
