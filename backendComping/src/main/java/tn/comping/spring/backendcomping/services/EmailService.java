package tn.comping.spring.backendcomping.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.entities.Reservation;
import jakarta.mail.internet.MimeMessage;

// @Service
@RequiredArgsConstructor
public class EmailService {

    public void sendPasswordResetEmail(String email, String token, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("🔑 Réinitialisation mot de passe Campino");
            String resetUrl = "http://localhost:4200/reset-password?token=" + token;
            helper.setText(
                "<!DOCTYPE html>" +
                "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Bonjour " + userName + ",</h2>" +
                "<p>Vous avez demandé une réinitialisation de mot de passe.</p>" +
                "<a href='" + resetUrl + "' style='background-color: #4CAF50; color: white; padding: 14px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;'>🔑 RÉINITIALISER MON MOT DE PASSE</a>" +
                "<p>Le lien expire dans 1 heure.</p>" +
                "<p>Si vous n'avez pas demandé cela, ignorez cet email.</p>" +
                "</body>" +
                "</html>",
                true
            );

            mailSender.send(message);
            System.out.println("✅ Email reset envoyé à " + email);

        } catch (Exception e) {
            System.err.println("⚠️ Erreur envoi email reset: " + e.getMessage());
        }
    }

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