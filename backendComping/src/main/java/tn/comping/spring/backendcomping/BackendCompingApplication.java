package tn.comping.spring.backendcomping;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class BackendCompingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendCompingApplication.class, args);
    }
    @Bean
    CommandLineRunner showDb(MongoTemplate mongoTemplate){
        return args -> {
            System.out.println("DATABASE USED : " + mongoTemplate.getDb().getName());
        };
    }

}
