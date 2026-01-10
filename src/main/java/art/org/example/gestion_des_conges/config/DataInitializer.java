package art.org.example.gestion_des_conges.config;

import art.org.example.gestion_des_conges.entity.User;
import art.org.example.gestion_des_conges.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Créer un admin par défaut si aucun utilisateur n'existe
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@conges.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFirstName("Admin");
                admin.setLastName("System");
                admin.setRole(User.Role.ADMIN);
                admin.setActive(true);

                userRepository.save(admin);
                System.out.println("Admin user created: username=admin, password=admin123");

                // Créer un employé de test
                User employe = new User();
                employe.setUsername("employe");
                employe.setEmail("employe@conges.com");
                employe.setPassword(passwordEncoder.encode("employe123"));
                employe.setFirstName("Jean");
                employe.setLastName("Dupont");
                employe.setRole(User.Role.EMPLOYE);
                employe.setActive(true);

                userRepository.save(employe);
                System.out.println("Employee user created: username=employe, password=employe123");

                // Créer un validateur de test
                User validateur = new User();
                validateur.setUsername("validateur");
                validateur.setEmail("validateur@conges.com");
                validateur.setPassword(passwordEncoder.encode("validateur123"));
                validateur.setFirstName("Marie");
                validateur.setLastName("Martin");
                validateur.setRole(User.Role.VALIDATEUR);
                validateur.setActive(true);

                userRepository.save(validateur);
                System.out.println("Validator user created: username=validateur, password=validateur123");
            }
        };
    }
}