package art.org.example.gestion_des_conges.dto;

import lombok.Data;

@Data
public class LeaveBalanceDTO {
    private Long id;
    private Long userId;
    private String userName; // Nom complet de l'utilisateur
    private Long leaveTypeId;
    private String leaveTypeName;
    private String leaveTypeColor;
    private Integer year;
    private Double totalAllowance;
    private Double usedDays;
    private Double pendingDays;
    private Double remainingDays;
    private Double carriedOverDays;

    // Calculés
    private Double availableDays; // remainingDays (pour l'affichage)
    private Integer percentageUsed; // (usedDays / totalAllowance) * 100
}