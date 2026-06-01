package tn.comping.spring.backendcomping.TestIntegration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tn.comping.spring.backendcomping.entities.Role;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.AbonnementRepository;
import tn.comping.spring.backendcomping.repositories.AvisRepository;
import tn.comping.spring.backendcomping.repositories.CommentaireRepository;
import tn.comping.spring.backendcomping.repositories.ConversationRepository;
import tn.comping.spring.backendcomping.repositories.InteractionRepository;
import tn.comping.spring.backendcomping.repositories.MessageRepository;
import tn.comping.spring.backendcomping.repositories.PostRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.EmailService;

import java.time.Instant;
import java.util.Date;

@SpringBootTest(properties = {
        "youtube.api.key=test-youtube-key",
        "groq.api.key=test-groq-key",
        "groq.api.url=https://example.test/groq",
        "groq.model=test-model",
        "ai.rss.feeds=https://example.test/feed.xml",
        "ia.api.url=http://localhost:5000/recommend",
        "stripe.secret.key=sk_test_dummy",
        "stripe.webhook.secret=whsec_test_dummy",
        "cloudinary.cloud.name=test-cloud",
        "cloudinary.api.key=123456789",
        "cloudinary.api.secret=test-secret"
})
@AutoConfigureMockMvc
abstract class SocialIntegrationTestSupport {

    protected static final String ALICE = "alice.social@test.com";
    protected static final String BOB = "bob.social@test.com";
    protected static final String CAROL = "carol.social@test.com";
    protected static final String ADMIN = "admin.social@test.com";

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected SignupRepository signupRepository;
    @Autowired protected PostRepository postRepository;
    @Autowired protected CommentaireRepository commentaireRepository;
    @Autowired protected InteractionRepository interactionRepository;
    @Autowired protected AbonnementRepository abonnementRepository;
    @Autowired protected ConversationRepository conversationRepository;
    @Autowired protected MessageRepository messageRepository;
    @Autowired protected AvisRepository avisRepository;

    @MockBean protected EmailService emailService;
    @MockBean protected JavaMailSender javaMailSender;

    protected SignupEntity aliceUser;
    protected SignupEntity bobUser;
    protected SignupEntity carolUser;
    protected SignupEntity adminUser;

    @BeforeEach
    void setUpSocialData() {
        cleanSocialCollections();

        aliceUser = saveUser("Alice", "Social", ALICE, Role.USER);
        bobUser = saveUser("Bob", "Social", BOB, Role.USER);
        carolUser = saveUser("Carol", "Social", CAROL, Role.USER);
        adminUser = saveUser("Admin", "Social", ADMIN, Role.ADMIN);
    }

    @AfterEach
    void tearDownSocialData() {
        cleanSocialCollections();
    }

    protected SignupEntity saveUser(String firstName, String lastName, String email, Role role) {
        return signupRepository.save(SignupEntity.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode("Password123!"))
                .role(role)
                .statut(true)
                .online(false)
                .lastSeen(Date.from(Instant.now()))
                .build());
    }

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private void cleanSocialCollections() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        commentaireRepository.deleteAll();
        interactionRepository.deleteAll();
        postRepository.deleteAll();
        avisRepository.deleteAll();
        abonnementRepository.deleteAll();
        signupRepository.deleteAll();
    }
}
