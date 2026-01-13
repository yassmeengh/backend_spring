package art.org.example.gestion_des_conges.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateLeaveTypeRequest {

    private String name;
    private String description;
    private Boolean requiresApproval;
    private Boolean isPaid;
    private Boolean deductsFromBalance;

    @Min(value = 0, message = "Le nombre de jours doit être positif")
    private Double maxDaysPerYear;

    @Min(value = 0, message = "Les droits annuels doivent être positifs")
    private Double defaultAnnualAllowance;

    private Boolean allowCarryOver;

    @Min(value = 0, message = "Le nombre de jours reportables doit être positif")
    private Integer maxCarryOverDays;

    private Boolean isActive;
    private String color;

    @Min(value = 0, message = "L'ordre d'affichage doit être positif")
    private Integer displayOrder;
}