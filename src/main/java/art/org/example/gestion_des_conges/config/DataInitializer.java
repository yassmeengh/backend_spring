package art.org.example.gestion_des_conges.config;

import art.org.example.gestion_des_conges.entity.LeaveType;
import art.org.example.gestion_des_conges.entity.User;
import art.org.example.gestion_des_conges.repository.LeaveTypeRepository;
import art.org.example.gestion_des_conges.repository.UserRepository;
import art.org.example.gestion_des_conges.service.LeaveBalanceService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      PasswordEncoder passwordEncoder,
                                      LeaveTypeRepository leaveTypeRepository,
                                      LeaveBalanceService leaveBalanceService) {
        return args -> {
            // ========== ÉTAPE 1 : CRÉER LES UTILISATEURS ==========
            if (userRepository.count() == 0) {
                System.out.println("👥 Initialisation des utilisateurs...");

                // 1. Admin
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@conges.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFirstName("Admin");
                admin.setLastName("System");
                admin.setRole(User.Role.ADMIN);
                admin.setActive(true);
                userRepository.save(admin);
                System.out.println("✅ Admin créé: username=admin, password=admin123");

                // 2. Employé
                User employe = new User();
                employe.setUsername("employe");
                employe.setEmail("employe@conges.com");
                employe.setPassword(passwordEncoder.encode("employe123"));
                employe.setFirstName("Jean");
                employe.setLastName("Dupont");
                employe.setRole(User.Role.EMPLOYE);
                employe.setActive(true);
                userRepository.save(employe);
                System.out.println("✅ Employé créé: username=employe, password=employe123");

                // 3. Validateur
                User validateur = new User();
                validateur.setUsername("validateur");
                validateur.setEmail("validateur@conges.com");
                validateur.setPassword(passwordEncoder.encode("validateur123"));
                validateur.setFirstName("Marie");
                validateur.setLastName("Martin");
                validateur.setRole(User.Role.VALIDATEUR);
                validateur.setActive(true);
                userRepository.save(validateur);
                System.out.println("✅ Validateur créé: username=validateur, password=validateur123");
            }

            // ========== ÉTAPE 2 : CRÉER LES TYPES DE CONGÉS ==========
            if (leaveTypeRepository.count() == 0) {
                System.out.println("🏖️ Initialisation des types de congés...");

                // 1. Congé payé
                LeaveType congePaye = new LeaveType();
                congePaye.setName("CONGE_PAYE");
                congePaye.setDescription("Congés payés annuels");
                congePaye.setRequiresApproval(true);
                congePaye.setIsPaid(true);
                congePaye.setDeductsFromBalance(true);
                congePaye.setMaxDaysPerYear(25.0);
                congePaye.setDefaultAnnualAllowance(25.0);
                congePaye.setAllowCarryOver(true);
                congePaye.setMaxCarryOverDays(10);
                congePaye.setColor("#3B82F6"); // Bleu
                congePaye.setDisplayOrder(1);
                congePaye.setIsActive(true);
                leaveTypeRepository.save(congePaye);

                // 2. Maladie
                LeaveType maladie = new LeaveType();
                maladie.setName("MALADIE");
                maladie.setDescription("Arrêt maladie");
                maladie.setRequiresApproval(false);
                maladie.setIsPaid(true);
                maladie.setDeductsFromBalance(false);
                maladie.setMaxDaysPerYear(365.0);
                maladie.setDefaultAnnualAllowance(0.0);
                maladie.setAllowCarryOver(false);
                maladie.setMaxCarryOverDays(0);
                maladie.setColor("#EF4444"); // Rouge
                maladie.setDisplayOrder(2);
                maladie.setIsActive(true);
                leaveTypeRepository.save(maladie);

                // 3. Sans solde
                LeaveType sansSolde = new LeaveType();
                sansSolde.setName("SANS_SOLDE");
                sansSolde.setDescription("Congé sans solde");
                sansSolde.setRequiresApproval(true);
                sansSolde.setIsPaid(false);
                sansSolde.setDeductsFromBalance(false);
                sansSolde.setMaxDaysPerYear(30.0);
                sansSolde.setDefaultAnnualAllowance(0.0);
                sansSolde.setAllowCarryOver(false);
                sansSolde.setMaxCarryOverDays(0);
                sansSolde.setColor("#9CA3AF"); // Gris
                sansSolde.setDisplayOrder(3);
                sansSolde.setIsActive(true);
                leaveTypeRepository.save(sansSolde);

                // 4. Exceptionnel
                LeaveType exceptionnel = new LeaveType();
                exceptionnel.setName("EXCEPTIONNEL");
                exceptionnel.setDescription("Congé exceptionnel (mariage, décès, etc.)");
                exceptionnel.setRequiresApproval(true);
                exceptionnel.setIsPaid(true);
                exceptionnel.setDeductsFromBalance(false);
                exceptionnel.setMaxDaysPerYear(10.0);
                exceptionnel.setDefaultAnnualAllowance(10.0);
                exceptionnel.setAllowCarryOver(false);
                exceptionnel.setMaxCarryOverDays(0);
                exceptionnel.setColor("#10B981"); // Vert
                exceptionnel.setDisplayOrder(4);
                exceptionnel.setIsActive(true);
                leaveTypeRepository.save(exceptionnel);

                System.out.println("✅ 4 types de congés créés");

                // ========== ÉTAPE 3 : INITIALISER LES SOLDES ==========
                System.out.println("💰 Initialisation des soldes...");
                int currentYear = LocalDate.now().getYear();
                List<User> users = userRepository.findAll();

                for (User user : users) {
                    leaveBalanceService.initializeUserBalances(user.getId(), currentYear);
                }

                System.out.println("✅ Soldes initialisés pour " + users.size() + " utilisateurs");
                System.out.println("🎉 Initialisation terminée avec succès !");
            }
        };
    }
}