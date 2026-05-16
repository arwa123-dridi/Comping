package tn.comping.spring.backendcomping.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.*;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedDatabase(
            EquipeRepository equipeRepo,
            SiteCampingRepository siteRepo,
            ProduitRepository produitRepo,
            ActivityRepository activityRepo) {
        return args -> {
            // Seed data only if repositories are empty
            if (equipeRepo.count() == 0) {
                Equipe equipe1 = new Equipe();
                equipe1.setNom("Equipe Alpha");
                equipeRepo.save(equipe1);
            }
            if (siteRepo.count() == 0) {
                SiteCamping site = new SiteCamping();
                site.setNom("Camping de la Plage");
                site.setLocalisation("123 Beach Ave");
                siteRepo.save(site);
            }
            if (produitRepo.count() == 0) {
                Produit prod = new Produit();
                prod.setNomProduit("Tente 4 Personnes");
                prod.setPrixProduit(199.99);
                produitRepo.save(prod);
            }
            if (activityRepo.count() == 0) {
                Activity activity = new Activity();
                activity.setNom("Randonnée Matinale");
                activity.setDescription("Trajet de 5km autour du lac.");
                activityRepo.save(activity);
            }
        };
    }
}
