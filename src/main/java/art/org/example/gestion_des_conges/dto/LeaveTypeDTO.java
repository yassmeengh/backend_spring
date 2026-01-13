package art.org.example.gestion_des_conges.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaveTypeDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean requiresApproval;
    private Boolean isPaid;
    private Boolean deductsFromBalance;
    private Double maxDaysPerYear;
    private Double defaultAnnualAllowance;
    private Boolean allowCarryOver;
    private Integer maxCarryOverDays;
    private Boolean isActive;
    private String color;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isSystemType; // Calculé
}