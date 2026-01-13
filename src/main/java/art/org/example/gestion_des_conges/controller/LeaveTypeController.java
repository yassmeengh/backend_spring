package art.org.example.gestion_des_conges.controller;

import art.org.example.gestion_des_conges.dto.CreateLeaveTypeRequest;
import art.org.example.gestion_des_conges.dto.LeaveTypeDTO;
import art.org.example.gestion_des_conges.dto.UpdateLeaveTypeRequest;
import art.org.example.gestion_des_conges.service.LeaveTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave-types")
@PreAuthorize("hasRole('ADMIN')") // Seul ADMIN peut gérer les types de congés
public class LeaveTypeController {

    @Autowired
    private LeaveTypeService leaveTypeService;

    /**
     * US-03.1 : Créer un type de congé
     */
    @PostMapping
    public ResponseEntity<LeaveTypeDTO> createLeaveType(@Valid @RequestBody CreateLeaveTypeRequest request) {
        LeaveTypeDTO leaveType = leaveTypeService.createLeaveType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveType);
    }

    /**
     * US-03.2 : Modifier un type de congé
     */
    @PutMapping("/{id}")
    public ResponseEntity<LeaveTypeDTO> updateLeaveType(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeaveTypeRequest request) {
        LeaveTypeDTO leaveType = leaveTypeService.updateLeaveType(id, request);
        return ResponseEntity.ok(leaveType);
    }

    /**
     * US-03.2 : Supprimer un type de congé
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeaveType(@PathVariable Long id) {
        leaveTypeService.deleteLeaveType(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupérer tous les types de congés
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Tous les utilisateurs connectés
    public ResponseEntity<List<LeaveTypeDTO>> getAllLeaveTypes(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly,
            @RequestParam(required = false) String search) {

        List<LeaveTypeDTO> leaveTypes;

        if (search != null && !search.trim().isEmpty()) {
            leaveTypes = leaveTypeService.searchLeaveTypes(search);
        } else if (activeOnly) {
            leaveTypes = leaveTypeService.getActiveLeaveTypes();
        } else {
            leaveTypes = leaveTypeService.getAllLeaveTypes();
        }

        return ResponseEntity.ok(leaveTypes);
    }

    /**
     * Récupérer un type de congé par ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveTypeDTO> getLeaveTypeById(@PathVariable Long id) {
        LeaveTypeDTO leaveType = leaveTypeService.getLeaveTypeById(id);
        return ResponseEntity.ok(leaveType);
    }

    /**
     * Activer/Désactiver un type de congé
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<LeaveTypeDTO> toggleLeaveTypeStatus(@PathVariable Long id) {
        LeaveTypeDTO leaveType = leaveTypeService.toggleLeaveTypeStatus(id);
        return ResponseEntity.ok(leaveType);
    }

    /**
     * Vérifier la disponibilité d'un nom
     */
    @GetMapping("/check-name-availability")
    public ResponseEntity<Map<String, Boolean>> checkNameAvailability(
            @RequestParam String name,
            @RequestParam(required = false) Long excludeId) {

        boolean isAvailable = leaveTypeService.isLeaveTypeNameAvailable(name, excludeId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        return ResponseEntity.ok(response);
    }
}