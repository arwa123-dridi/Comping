package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.entities.Reservation;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void envoyerConfirmation(Reservation reservation) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo("destinataire@example.com"); // ou récupérer l'email de l'utilisateur
            helper.setSubject("✅ Confirmation de réservation TuniCamp");
            helper.setText(
                    "<h2>Réservation confirmée !</h2>" +
                            "<p>Site : " + reservation.getSiteCampingId() + "</p>" +
                            "<p>Du : " + reservation.getDateDebut() + " au " + reservation.getDateFin() + "</p>" +
                            "<p>Statut : " + reservation.getStatut() + "</p>",
                    true
            );

            mailSender.send(message);
            System.out.println("✅ Email envoyé avec succès");

        } catch (Exception e) {
            // ⚠️ En dev : on log l'erreur sans bloquer
            System.err.println("⚠️ Email non envoyé : " + e.getMessage());
        }
    }
}