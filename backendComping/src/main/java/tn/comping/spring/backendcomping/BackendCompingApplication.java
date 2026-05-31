package tn.comping.spring.backendcomping;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
@EnableCaching
public class BackendCompingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendCompingApplication.class, args);
    }
    
   @Bean
    @Profile("!test")
    CommandLineRunner showDb(MongoTemplate mongoTemplate){
        return args -> {
            System.out.println("DATABASE USED : "
                    + mongoTemplate.getDb().getName());
        };
    }

}