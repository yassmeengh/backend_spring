package art.org.example.gestion_des_conges.controller;

import art.org.example.gestion_des_conges.dto.LeaveBalanceDTO;
import art.org.example.gestion_des_conges.service.AuthService;
import art.org.example.gestion_des_conges.service.LeaveBalanceService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leave-balances")
public class LeaveBalanceController {

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private AuthService authService;

    /**
     * US-03.5 : Consulter son solde (employé)
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaveBalanceDTO>> getMyBalances(
            @RequestParam(required = false) Integer year) {

        Long userId = authService.getCurrentUser().getId();
        Integer targetYear = year != null ? year : LocalDate.now().getYear();

        List<LeaveBalanceDTO> balances = leaveBalanceService.getUserBalances(userId, targetYear);
        return ResponseEntity.ok(balances);
    }

    /**
     * US-03.3 : Consulter le solde d'un utilisateur (admin)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveBalanceDTO>> getUserBalances(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer year) {

        Integer targetYear = year != null ? year : LocalDate.now().getYear();
        List<LeaveBalanceDTO> balances = leaveBalanceService.getUserBalances(userId, targetYear);
        return ResponseEntity.ok(balances);
    }

    /**
     * US-03.3 : Définir le solde d'un utilisateur (admin)
     */
    @PostMapping("/set-balance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveBalanceDTO> setUserBalance(@RequestBody SetBalanceRequest request) {
        LeaveBalanceDTO balance = leaveBalanceService.setUserBalance(
                request.getUserId(),
                request.getLeaveTypeId(),
                request.getYear(),
                request.getAllowance()
        );
        return ResponseEntity.ok(balance);
    }

    /**
     * US-03.3 : Initialiser les soldes d'un utilisateur (admin)
     */
    @PostMapping("/initialize/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> initializeUserBalances(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer year) {

        Integer targetYear = year != null ? year : LocalDate.now().getYear();
        leaveBalanceService.initializeUserBalances(userId, targetYear);
        return ResponseEntity.ok().build();
    }

    /**
     * US-03.4 : Reporter les soldes (admin)
     */
    @PostMapping("/carry-over")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> carryOverBalances(@RequestBody CarryOverRequest request) {
        leaveBalanceService.carryOverBalances(request.getFromYear(), request.getToYear());
        return ResponseEntity.ok().build();
    }

    /**
     * Initialiser tous les utilisateurs pour une année (admin)
     */
    @PostMapping("/initialize-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> initializeAllUsersForYear(@RequestParam Integer year) {
        leaveBalanceService.initializeAllUsersForYear(year);
        return ResponseEntity.ok().build();
    }

    // ========== DTOs pour les requêtes ==========

    @Data
    public static class SetBalanceRequest {
        private Long userId;
        private Long leaveTypeId;
        private Integer year;
        private Double allowance;
    }

    @Data
    public static class CarryOverRequest {
        private Integer fromYear;
        private Integer toYear;
    }
}