package art.org.example.gestion_des_conges.service;

import art.org.example.gestion_des_conges.dto.TeamDTO;
import art.org.example.gestion_des_conges.dto.UserDTO;
import art.org.example.gestion_des_conges.entity.Team;
import art.org.example.gestion_des_conges.entity.User;
import art.org.example.gestion_des_conges.repository.TeamRepository;
import art.org.example.gestion_des_conges.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * US-02.5 : Créer une nouvelle équipe
     */
    @Transactional
    public TeamDTO createTeam(String name, String description) {
        // Vérifier si une équipe avec ce nom existe déjà
        if (teamRepository.existsByName(name)) {
            throw new IllegalArgumentException("Une équipe avec ce nom existe déjà");
        }

        Team team = new Team();
        team.setName(name);
        team.setDescription(description);

        Team savedTeam = teamRepository.save(team);
        return convertToDTO(savedTeam);
    }

    /**
     * US-02.5 : Modifier une équipe
     */
    @Transactional
    public TeamDTO updateTeam(Long id, String newName, String newDescription) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Équipe non trouvée"));

        // Vérifier si le nouveau nom n'est pas déjà utilisé par une autre équipe
        if (!team.getName().equals(newName) && teamRepository.existsByNameExcludingId(newName, id)) {
            throw new IllegalArgumentException("Une autre équipe utilise déjà ce nom");
        }

        team.setName(newName);
        if (newDescription != null) {
            team.setDescription(newDescription);
        }

        Team updatedTeam = teamRepository.save(team);
        return convertToDTO(updatedTeam);
    }

    /**
     * US-02.5 : Supprimer une équipe
     */
    @Transactional
    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Équipe non trouvée"));

        // Vérifier si l'équipe a des membres
        if (!team.getMembers().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer une équipe avec des membres. "
                    + "Veuillez d'abord retirer tous les membres.");
        }

        teamRepository.delete(team);
    }

    /**
     * US-02.5 : Ajouter un membre à une équipe
     */
    @Transactional
    public TeamDTO addMember(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Équipe non trouvée"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        // Vérifier si l'utilisateur n'est pas déjà dans l'équipe
        if (team.getMembers().contains(user)) {
            throw new IllegalArgumentException("L'utilisateur est déjà membre de cette équipe");
        }

        // Vérifier si l'utilisateur n'est pas déjà dans une autre équipe
        if (user.getTeam() != null && !user.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("L'utilisateur est déjà membre d'une autre équipe. "
                    + "Veuillez d'abord le retirer de son équipe actuelle.");
        }

        // Ajouter l'utilisateur à l'équipe
        team.getMembers().add(user);
        user.setTeam(team);

        userRepository.save(user);
        Team updatedTeam = teamRepository.save(team);

        return convertToDTO(updatedTeam);
    }

    /**
     * US-02.5 : Retirer un membre d'une équipe
     */
    @Transactional
    public TeamDTO removeMember(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Équipe non trouvée"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        // Vérifier si l'utilisateur est bien dans cette équipe
        if (!team.getMembers().contains(user)) {
            throw new IllegalArgumentException("L'utilisateur n'est pas membre de cette équipe");
        }

        // Si l'utilisateur est le validateur de l'équipe, le retirer de ce rôle
        if (team.getValidator() != null && team.getValidator().getId().equals(userId)) {
            team.setValidator(null);
        }

        // Retirer l'utilisateur de l'équipe
        team.getMembers().remove(user);
        user.setTeam(null);

        userRepository.save(user);
        Team updatedTeam = teamRepository.save(team);

        return convertToDTO(updatedTeam);
    }

    /**
     * US-02.6 : Désigner un validateur pour une équipe
     */
    @Transactional
    public TeamDTO setValidator(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Équipe non trouvée"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        // Vérifier que l'utilisateur a le rôle VALIDATEUR
        if (user.getRole() != User.Role.VALIDATEUR) {
            throw new IllegalArgumentException("L'utilisateur doit avoir le rôle VALIDATEUR pour être désigné comme validateur d'équipe");
        }

        // Vérifier que l'utilisateur est membre de l'équipe
        if (user.getTeam() == null || !user.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("Le validateur doit être membre de l'équipe");
        }

        // Si l'utilisateur est déjà validateur d'une autre équipe, le retirer
        List<Team> otherTeams = teamRepository.findByValidator(user);
        for (Team otherTeam : otherTeams) {
            if (!otherTeam.getId().equals(teamId)) {
                otherTeam.setValidator(null);
                teamRepository.save(otherTeam);
            }
        }

        team.setValidator(user);
        Team savedTeam = teamRepository.save(team);
        return convertToDTO(savedTeam);
    }

    /**
     * US-02.6 : Retirer le validateur d'une équipe
     */
    @Transactional
    public TeamDTO removeValidator(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Équipe non trouvée"));

        if (team.getValidator() == null) {
            throw new IllegalArgumentException("Cette équipe n'a pas de validateur désigné");
        }

        team.setValidator(null);
        Team updatedTeam = teamRepository.save(team);
        return convertToDTO(updatedTeam);
    }

    /**
     * Récupérer une équipe par ID avec ses membres
     */
    public TeamDTO getTeamById(Long id) {
        Team team = teamRepository.findTeamWithMembersById(id)
                .orElseThrow(() -> new EntityNotFoundException("Équipe non trouvée"));
        return convertToDTO(team);
    }

    /**
     * Récupérer toutes les équipes
     */
    public List<TeamDTO> getAllTeams() {
        List<Team> teams = teamRepository.findAllWithValidator();
        return teams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les équipes sans validateur
     */
    public List<TeamDTO> getTeamsWithoutValidator() {
        List<Team> teams = teamRepository.findByValidatorIsNull();
        return teams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les équipes vides (sans membres)
     */
    public List<TeamDTO> getEmptyTeams() {
        List<Team> teams = teamRepository.findEmptyTeams();
        return teams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les équipes d'un validateur spécifique
     */
    public List<TeamDTO> getTeamsByValidator(Long validatorId) {
        List<Team> teams = teamRepository.findAllByValidatorId(validatorId);
        return teams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher des équipes par nom ou description
     */
    public List<TeamDTO> searchTeams(String search) {
        List<Team> teams = teamRepository.searchTeams(search);
        return teams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer toutes les équipes avec leur nombre de membres
     */
    public List<TeamDTO> getAllTeamsWithMemberCount() {
        List<Object[]> results = teamRepository.findAllTeamsWithMemberCount();
        return results.stream()
                .map(result -> {
                    Team team = (Team) result[0];
                    Long memberCount = (Long) result[1];
                    TeamDTO dto = convertToDTO(team);
                    dto.setNombreMembres(memberCount.intValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Convertir Team en TeamDTO
     */
    private TeamDTO convertToDTO(Team team) {
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setNom(team.getName());
        dto.setDescription(team.getDescription());
        dto.setCreatedAt(team.getCreatedAt());
        dto.setUpdatedAt(team.getUpdatedAt());

        // Informations sur le validateur
        if (team.getValidator() != null) {
            dto.setValidateurId(team.getValidator().getId());
            dto.setValidateurNom(team.getValidator().getFirstName() + " " + team.getValidator().getLastName());
        }

        // Liste des membres
        List<UserDTO> membresDTO = team.getMembers().stream()
                .map(user -> {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setId(user.getId());
                    userDTO.setNom(user.getLastName());
                    userDTO.setPrenom(user.getFirstName());
                    userDTO.setUsername(user.getUsername());
                    userDTO.setEmail(user.getEmail());
                    userDTO.setRole(user.getRole());
                    userDTO.setActif(user.getActive());
                    return userDTO;
                })
                .collect(Collectors.toList());

        dto.setMembres(membresDTO);
        dto.setNombreMembres(membresDTO.size());

        return dto;
    }

    /**
     * Vérifier si un nom d'équipe est disponible
     */
    public boolean isTeamNameAvailable(String name, Long excludeTeamId) {
        if (excludeTeamId == null) {
            return !teamRepository.existsByName(name);
        }
        return !teamRepository.existsByNameExcludingId(name, excludeTeamId);
    }
}