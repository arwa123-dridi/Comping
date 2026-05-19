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

    public void sendVerificationEmail(String email, String token, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("📧 Vérification de votre compte Campino");
            String verificationUrl = "http://localhost:4200/verify-email?token=" + token;
            helper.setText(
                "<!DOCTYPE html>" +
                "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Bienvenue " + userName + " !</h2>" +
                "<p>Merci de vous être inscrit sur Campino. Veuillez cliquer sur le bouton ci-dessous pour vérifier votre adresse email :</p>" +
                "<a href='" + verificationUrl + "' style='background-color: #4CAF50; color: white; padding: 14px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;'>✅ VÉRIFIER MON COMPTE</a>" +
                "<p>Si vous n'avez pas créé de compte, vous pouvez ignorer cet email.</p>" +
                "</body>" +
                "</html>",
                true
            );
            mailSender.send(message);
            System.out.println("✅ Email de vérification envoyé à " + email);
        } catch (Exception e) {
            System.err.println("⚠️ Erreur envoi email vérification: " + e.getMessage());
        }
    }

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

    public void envoyerConfirmation(Reservation reservation) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo("destinataire@example.com");
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
            System.err.println("⚠️ Email non envoyé : " + e.getMessage());
        }
    }

    public void sendPaymentLink(String email, String reservationId, double montant) {
        try {
            String lien = "http://localhost:4200/paiement/" + reservationId;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("💳 Finalisez votre paiement - TuniCamp");
            helper.setText(
                "<div>" +
                "<h2>💳 Paiement</h2>" +
                "<p>Cliquez sur le bouton ci-dessous :</p>" +
                "<a href='" + lien + "' style='padding:12px 20px;background:#5469d4;color:white;text-decoration:none;border-radius:6px'>" +
                "Payer maintenant</a>" +
                "<p>Si le bouton ne marche pas :</p>" +
                "<p>" + lien + "</p>" +
                "</div>",
                true
            );
            mailSender.send(message);
            System.out.println("✅ Email de paiement envoyé à " + email);
        } catch (Exception e) {
            System.err.println("⚠️ Email paiement non envoyé : " + e.getMessage());
        }
    }

    public void sendConfirmationEmail(String email, String reservationId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("✅ Paiement confirmé - TuniCamp");
            helper.setText(
                "<div style='font-family:Arial,sans-serif;max-width:500px;margin:auto;" +
                "padding:24px;border:1px solid #e0e0e0;border-radius:12px'>" +
                "<h2 style='color:#27ae60'>✅ Paiement reçu avec succès !</h2>" +
                "<p>Bonjour,</p>" +
                "<p>Votre paiement a bien été reçu. Votre réservation est maintenant <strong>confirmée</strong>.</p>" +
                "<p><strong>Référence :</strong> " + reservationId + "</p><br>" +
                "<p>Merci et bonne aventure avec TuniCamp ! 🏕️</p>" +
                "</div>", true
            );
            mailSender.send(message);
            System.out.println("✅ Email confirmation envoyé à " + email);
        } catch (Exception e) {
            System.err.println("⚠️ Email confirmation non envoyé : " + e.getMessage());
        }
    }
}
