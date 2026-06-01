package tn.comping.spring.backendcomping.services.serviceImpl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tn.comping.spring.backendcomping.entities.Produit;

@Service
@RequiredArgsConstructor
public class EmailServiceProduct {

    private final JavaMailSender mailSender;
    private final String ADMIN_EMAIL = "barranifatma18@gmail.com";

    public void sendOutOfStockEmail(Produit produit) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(ADMIN_EMAIL);
        message.setSubject("⚠️ Action Required: Product Out of Stock");

        String emailBody =
                "Dear Admin,\n\n" +

                "We would like to inform you that a product is now OUT OF STOCK on the platform.\n\n" +

                "──────── PRODUCT DETAILS ────────\n" +
                "Product Name : " + produit.getNomProduit() + "\n" +
                "Product ID   : " + produit.getId() + "\n" +
                "Current Stock: 0 units\n" +
                "Status       : OUT OF STOCK\n" +
                "─────────────────────────────────\n\n" +

                "This product is no longer available for purchase.\n" +
                "Please proceed with restocking as soon as possible to avoid loss of sales.\n\n" +

                "Recommended actions:\n" +
                "• Check supplier availability\n" +
                "• Update stock quantity in the admin dashboard\n" +
                "• Verify product visibility on the storefront\n\n" +

                "This is an automated notification from your E-Commerce System.\n\n" +

                "Best regards,\n" +
                "Comping Stock Monitoring Service";

        message.setText(emailBody);

        mailSender.send(message);
    }
}
