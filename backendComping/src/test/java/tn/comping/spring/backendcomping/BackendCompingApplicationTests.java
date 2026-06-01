package tn.comping.spring.backendcomping;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Nécessite MongoDB démarré + variables d'environnement (YouTube API, Groq, Stripe, etc.)")
@SpringBootTest
class BackendCompingApplicationTests {

    @Test
    void contextLoads() {
    }

}
