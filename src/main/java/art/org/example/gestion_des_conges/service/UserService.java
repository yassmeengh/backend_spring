package art.org.example.gestion_des_conges.service;

import art.org.example.gestion_des_conges.dto.CreateUserRequest;
import art.org.example.gestion_des_conges.dto.UpdateUserRequest;
import art.org.example.gestion_des_conges.dto.UserDTO;
import art.org.example.gestion_des_conges.entity.Team;
import art.org.example.gestion_des_conges.entity.User;
import art.org.example.gestion_des_conges.repository.TeamRepository;
import art.org.example.gestion_des_conges.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${app.default-password-length:8}")
    private int defaultPasswordLength;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";

    /**
     * US-02.1 : Créer un utilisateur avec envoi d'email
     */
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        // Vérifier si le username existe déjà
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris");
        }

        // Générer un mot de passe temporaire
        String tempPassword = generateTemporaryPassword();
        String hashedPassword = passwordEncoder.encode(tempPassword);

        // Créer l'utilisateur
        User user = new User();
        user.setFirstName(request.getPrenom());
        user.setLastName(request.getNom());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(hashedPassword);
        user.setRole(request.getRole() != null ? request.getRole() : User.Role.EMPLOYE);
        user.setActive(true);

        // Assigner l'équipe si spécifiée
        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new EntityNotFoundException("Équipe non trouvée"));
            user.setTeam(team);
        }

        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);

        // Envoyer l'email de bienvenue
        try {
            emailService.sendWelcomeEmail(
                    savedUser.getEmail(),
                    savedUser.getFirstName() + " " + savedUser.getLastName(),
                    tempPassword,
                    savedUser.getUsername()
            );
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer la création
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }

        return convertToDTO(savedUser);
    }

    /**
     * US-02.2 : Modifier un utilisateur
     */
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        // Mettre à jour les champs
        if (request.getNom() != null) user.setLastName(request.getNom());
        if (request.getPrenom() != null) user.setFirstName(request.getPrenom());

        // Mettre à jour le username avec vérification d'unicité
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsernameExcludingId(request.getUsername(), id)) {
                throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé");
            }
            user.setUsername(request.getUsername());
        }

        // Mettre à jour l'email avec vérification d'unicité
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailExcludingId(request.getEmail(), id)) {
                throw new IllegalArgumentException("Cet email est déjà utilisé");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getRole() != null) user.setRole(request.getRole());

        // Mettre à jour le statut actif
        if (request.getActif() != null) user.setActive(request.getActif());

        // Gérer l'équipe
        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new EntityNotFoundException("Équipe non trouvée"));
            user.setTeam(team);
        } else if (request.getTeamId() == null && user.getTeam() != null) {
            // Retirer de l'équipe si teamId est explicitement null
            user.setTeam(null);
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    /**
     * US-02.2 : Activer/Désactiver un utilisateur
     */
    @Transactional
    public UserDTO toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        boolean newStatus = !user.getActive();
        user.setActive(newStatus);

        // Si on désactive un validateur, le retirer comme validateur des équipes
        if (!newStatus && user.getRole() == User.Role.VALIDATEUR) {
            List<Team> teams = teamRepository.findByValidator(user);
            for (Team team : teams) {
                team.setValidator(null);
                teamRepository.save(team);
            }
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    /**
     * US-02.3 : Rechercher des utilisateurs avec filtres
     */
    public List<UserDTO> searchUsers(String search, User.Role role, Long teamId) {
        List<User> users;

        if (search != null && !search.trim().isEmpty()) {
            // Recherche par nom, prénom, email ou username
            users = userRepository.searchUsers(search.trim());
        } else {
            // Récupérer tous les utilisateurs
            users = userRepository.findAll();
        }

        // Appliquer les filtres supplémentaires
        return users.stream()
                .filter(user -> role == null || user.getRole() == role)
                .filter(user -> teamId == null ||
                        (user.getTeam() != null && user.getTeam().getId().equals(teamId)))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * US-02.4 : Changer le rôle d'un utilisateur
     */
    @Transactional
    public UserDTO changeUserRole(Long id, User.Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        User.Role oldRole = user.getRole();
        user.setRole(newRole);

        // Si on retire le rôle VALIDATEUR, retirer comme validateur des équipes
        if (oldRole == User.Role.VALIDATEUR && newRole != User.Role.VALIDATEUR) {
            List<Team> teams = teamRepository.findByValidator(user);
            for (Team team : teams) {
                team.setValidator(null);
                teamRepository.save(team);
            }
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    /**
     * Récupérer un utilisateur par ID
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        return convertToDTO(user);
    }

    /**
     * Récupérer tous les utilisateurs
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer tous les validateurs disponibles (non assignés à une équipe)
     */
    public List<UserDTO> getAvailableValidateurs() {
        List<User> validateurs = userRepository.findAvailableValidateurs();
        return validateurs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les utilisateurs par équipe
     */
    public List<UserDTO> getUsersByTeam(Long teamId) {
        List<User> users = userRepository.findByTeamId(teamId);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les utilisateurs sans équipe
     */
    public List<UserDTO> getUsersWithoutTeam() {
        List<User> users = userRepository.findByTeamIsNull();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Générer un mot de passe temporaire aléatoire
     */
    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(defaultPasswordLength);

        for (int i = 0; i < defaultPasswordLength; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }

        return password.toString();
    }

    /**
     * Convertir User en UserDTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setNom(user.getLastName());
        dto.setPrenom(user.getFirstName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActif(user.getActive());

        if (user.getTeam() != null) {
            dto.setTeamId(user.getTeam().getId());
            dto.setTeamNom(user.getTeam().getName());
        }

        // Ajouter les soldes de congés
        dto.setSoldeCongesPayes(user.getSoldeCongesPayes());
        dto.setSoldeMaladie(user.getSoldeMaladie());
        dto.setSoldeExceptionnel(user.getSoldeExceptionnel());

        return dto;
    }

    /**
     * Vérifier si un email est disponible
     */
    public boolean isEmailAvailable(String email, Long excludeUserId) {
        if (excludeUserId == null) {
            return !userRepository.existsByEmail(email);
        }
        return !userRepository.existsByEmailExcludingId(email, excludeUserId);
    }

    /**
     * Vérifier si un username est disponible
     */
    public boolean isUsernameAvailable(String username, Long excludeUserId) {
        if (excludeUserId == null) {
            return !userRepository.existsByUsername(username);
        }
        return !userRepository.existsByUsernameExcludingId(username, excludeUserId);
    }
}