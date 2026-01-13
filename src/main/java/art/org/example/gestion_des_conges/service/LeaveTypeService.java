package art.org.example.gestion_des_conges.service;

import art.org.example.gestion_des_conges.dto.CreateLeaveTypeRequest;
import art.org.example.gestion_des_conges.dto.LeaveTypeDTO;
import art.org.example.gestion_des_conges.dto.UpdateLeaveTypeRequest;
import art.org.example.gestion_des_conges.entity.LeaveType;
import art.org.example.gestion_des_conges.repository.LeaveTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveTypeService {

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    /**
     * US-03.1 : Créer un type de congé
     */
    @Transactional
    public LeaveTypeDTO createLeaveType(CreateLeaveTypeRequest request) {
        // Vérifier si un type avec ce nom existe déjà
        if (leaveTypeRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Un type de congé avec ce nom existe déjà");
        }

        LeaveType leaveType = new LeaveType();
        leaveType.setName(request.getName());
        leaveType.setDescription(request.getDescription());
        leaveType.setRequiresApproval(request.getRequiresApproval());
        leaveType.setIsPaid(request.getIsPaid());
        leaveType.setDeductsFromBalance(request.getDeductsFromBalance());
        leaveType.setMaxDaysPerYear(request.getMaxDaysPerYear());
        leaveType.setDefaultAnnualAllowance(request.getDefaultAnnualAllowance());
        leaveType.setAllowCarryOver(request.getAllowCarryOver());
        leaveType.setMaxCarryOverDays(request.getMaxCarryOverDays());
        leaveType.setColor(request.getColor());
        leaveType.setDisplayOrder(request.getDisplayOrder());
        leaveType.setIsActive(true);

        LeaveType savedLeaveType = leaveTypeRepository.save(leaveType);
        return convertToDTO(savedLeaveType);
    }

    /**
     * US-03.2 : Modifier un type de congé
     */
    @Transactional
    public LeaveTypeDTO updateLeaveType(Long id, UpdateLeaveTypeRequest request) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Type de congé non trouvé"));

        // Mise à jour conditionnelle
        if (request.getName() != null && !request.getName().equals(leaveType.getName())) {
            if (leaveTypeRepository.existsByNameExcludingId(request.getName(), id)) {
                throw new IllegalArgumentException("Un autre type de congé utilise déjà ce nom");
            }
            leaveType.setName(request.getName());
        }

        if (request.getDescription() != null) leaveType.setDescription(request.getDescription());
        if (request.getRequiresApproval() != null) leaveType.setRequiresApproval(request.getRequiresApproval());
        if (request.getIsPaid() != null) leaveType.setIsPaid(request.getIsPaid());
        if (request.getDeductsFromBalance() != null) leaveType.setDeductsFromBalance(request.getDeductsFromBalance());
        if (request.getMaxDaysPerYear() != null) leaveType.setMaxDaysPerYear(request.getMaxDaysPerYear());
        if (request.getDefaultAnnualAllowance() != null) leaveType.setDefaultAnnualAllowance(request.getDefaultAnnualAllowance());
        if (request.getAllowCarryOver() != null) leaveType.setAllowCarryOver(request.getAllowCarryOver());
        if (request.getMaxCarryOverDays() != null) leaveType.setMaxCarryOverDays(request.getMaxCarryOverDays());
        if (request.getIsActive() != null) leaveType.setIsActive(request.getIsActive());
        if (request.getColor() != null) leaveType.setColor(request.getColor());
        if (request.getDisplayOrder() != null) leaveType.setDisplayOrder(request.getDisplayOrder());

        LeaveType updatedLeaveType = leaveTypeRepository.save(leaveType);
        return convertToDTO(updatedLeaveType);
    }

    /**
     * US-03.2 : Supprimer un type de congé
     */
    @Transactional
    public void deleteLeaveType(Long id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Type de congé non trouvé"));

        // Empêcher la suppression des types système
        if (leaveType.isSystemType()) {
            throw new IllegalStateException("Impossible de supprimer un type de congé système");
        }

        // TODO Sprint 4 : Vérifier qu'aucune demande de congé n'utilise ce type
        // if (leaveRequestRepository.existsByLeaveType(leaveType)) {
        //     throw new IllegalStateException("Impossible de supprimer un type de congé utilisé dans des demandes");
        // }

        leaveTypeRepository.delete(leaveType);
    }

    /**
     * Récupérer tous les types de congés
     */
    public List<LeaveTypeDTO> getAllLeaveTypes() {
        return leaveTypeRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les types de congés actifs uniquement
     */
    public List<LeaveTypeDTO> getActiveLeaveTypes() {
        return leaveTypeRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer un type de congé par ID
     */
    public LeaveTypeDTO getLeaveTypeById(Long id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Type de congé non trouvé"));
        return convertToDTO(leaveType);
    }

    /**
     * Rechercher des types de congés
     */
    public List<LeaveTypeDTO> searchLeaveTypes(String search) {
        return leaveTypeRepository.searchLeaveTypes(search).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Activer/Désactiver un type de congé
     */
    @Transactional
    public LeaveTypeDTO toggleLeaveTypeStatus(Long id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Type de congé non trouvé"));

        leaveType.setIsActive(!leaveType.getIsActive());
        LeaveType updatedLeaveType = leaveTypeRepository.save(leaveType);
        return convertToDTO(updatedLeaveType);
    }

    /**
     * Convertir LeaveType en LeaveTypeDTO
     */
    private LeaveTypeDTO convertToDTO(LeaveType leaveType) {
        LeaveTypeDTO dto = new LeaveTypeDTO();
        dto.setId(leaveType.getId());
        dto.setName(leaveType.getName());
        dto.setDescription(leaveType.getDescription());
        dto.setRequiresApproval(leaveType.getRequiresApproval());
        dto.setIsPaid(leaveType.getIsPaid());
        dto.setDeductsFromBalance(leaveType.getDeductsFromBalance());
        dto.setMaxDaysPerYear(leaveType.getMaxDaysPerYear());
        dto.setDefaultAnnualAllowance(leaveType.getDefaultAnnualAllowance());
        dto.setAllowCarryOver(leaveType.getAllowCarryOver());
        dto.setMaxCarryOverDays(leaveType.getMaxCarryOverDays());
        dto.setIsActive(leaveType.getIsActive());
        dto.setColor(leaveType.getColor());
        dto.setDisplayOrder(leaveType.getDisplayOrder());
        dto.setCreatedAt(leaveType.getCreatedAt());
        dto.setUpdatedAt(leaveType.getUpdatedAt());
        dto.setIsSystemType(leaveType.isSystemType());
        return dto;
    }

    /**
     * Vérifier si un nom est disponible
     */
    public boolean isLeaveTypeNameAvailable(String name, Long excludeId) {
        if (excludeId == null) {
            return !leaveTypeRepository.existsByName(name);
        }
        return !leaveTypeRepository.existsByNameExcludingId(name, excludeId);
    }
}