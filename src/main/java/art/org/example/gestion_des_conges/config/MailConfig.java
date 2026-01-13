package art.org.example.gestion_des_conges.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        System.out.println("=== Creating JavaMailSender bean (development mode) ===");

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl() {
            @Override
            public void send(SimpleMailMessage simpleMessage) {
                System.out.println("\n" +
                        "═══════════════════════════════════════════════════\n" +
                        "📧 EMAIL (Development Mode - Not Actually Sent):\n" +
                        "═══════════════════════════════════════════════════\n" +
                        "To: " + String.join(", ", simpleMessage.getTo()) + "\n" +
                        "Subject: " + simpleMessage.getSubject() + "\n" +
                        "Content:\n" + simpleMessage.getText() + "\n" +
                        "═══════════════════════════════════════════════════\n");
            }
        };

        // Basic configuration (not really used since we override send)
        mailSender.setHost("localhost");
        mailSender.setPort(25);

        Properties props = new Properties();
        props.put("mail.debug", "true");
        mailSender.setJavaMailProperties(props);

        return mailSender;
    }
}