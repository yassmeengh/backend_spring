package art.org.example.gestion_des_conges.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendWelcomeEmail(String to, String fullName, String tempPassword, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Bienvenue sur la plateforme de gestion des congés");
        message.setText("Bonjour " + fullName + ",\n\n"
                + "Votre compte a été créé avec succès.\n\n"
                + "🔑 Identifiants de connexion :\n"
                + "• Nom d'utilisateur : " + username + "\n"
                + "• Mot de passe temporaire : " + tempPassword + "\n"
                + "• Email : " + to + "\n\n"
                + "⚠️ Veuillez changer votre mot de passe à la première connexion.\n\n"
                + "Pour vous connecter, rendez-vous sur : " + to + "\n\n"
                + "Cordialement,\nL'équipe des Ressources Humaines");
        mailSender.send(message);
    }

    // AJOUTER CETTE MÉTHODE
    public void sendPasswordResetEmail(String to, String fullName, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText("Bonjour " + fullName + ",\n\n"
                + "Vous avez demandé la réinitialisation de votre mot de passe.\n\n"
                + "🔗 Cliquez sur ce lien pour créer un nouveau mot de passe :\n"
                + resetLink + "\n\n"
                + "⚠️ Ce lien expire dans 24 heures.\n\n"
                + "Si vous n'avez pas fait cette demande, veuillez ignorer cet email.\n\n"
                + "Cordialement,\nL'équipe des Ressources Humaines");
        mailSender.send(message);
    }

    // AJOUTER CETTE MÉTHODE POUR LES NOTIFICATIONS FUTURES
    public void sendLeaveRequestNotification(String to, String fullName, String status, String details) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Mise à jour de votre demande de congé");
        message.setText("Bonjour " + fullName + ",\n\n"
                + "Votre demande de congé a été " + status + ".\n\n"
                + "Détails : " + details + "\n\n"
                + "Cordialement,\nL'équipe des Ressources Humaines");
        mailSender.send(message);
    }
}