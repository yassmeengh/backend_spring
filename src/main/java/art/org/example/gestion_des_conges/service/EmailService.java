package art.org.example.gestion_des_conges.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // CORRIGEZ CETTE MÉTHODE : ajoutez le paramètre username
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
                + "Pour vous connecter, rendez-vous sur : http://localhost:8080\n\n"
                + "Cordialement,\nL'équipe des Ressources Humaines");
        mailSender.send(message);
    }

    // Optionnel : Ajoutez d'autres méthodes email
    public void sendPasswordResetEmail(String to, String fullName, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText("Bonjour " + fullName + ",\n\n"
                + "Vous avez demandé la réinitialisation de votre mot de passe.\n\n"
                + "🔗 Cliquez sur ce lien pour créer un nouveau mot de passe :\n"
                + resetLink + "\n\n"
                + "⚠️ Ce lien expire dans 24 heures.\n\n"
                + "Cordialement,\nL'équipe des Ressources Humaines");
        mailSender.send(message);
    }
}