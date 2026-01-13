package art.org.example.gestion_des_conges.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLeaveTypeRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    private String description;

    @NotNull(message = "Le champ 'requiresApproval' est obligatoire")
    private Boolean requiresApproval = true;

    @NotNull(message = "Le champ 'isPaid' est obligatoire")
    private Boolean isPaid = true;

    @NotNull(message = "Le champ 'deductsFromBalance' est obligatoire")
    private Boolean deductsFromBalance = true;

    @NotNull(message = "Le nombre maximum de jours par an est obligatoire")
    @Min(value = 0, message = "Le nombre de jours doit être positif")
    private Double maxDaysPerYear = 25.0;

    @NotNull(message = "Les droits annuels par défaut sont obligatoires")
    @Min(value = 0, message = "Les droits annuels doivent être positifs")
    private Double defaultAnnualAllowance = 25.0;

    @NotNull(message = "Le champ 'allowCarryOver' est obligatoire")
    private Boolean allowCarryOver = true;

    @Min(value = 0, message = "Le nombre de jours reportables doit être positif")
    private Integer maxCarryOverDays = 10;

    private String color = "#3B82F6";

    @Min(value = 0, message = "L'ordre d'affichage doit être positif")
    private Integer displayOrder = 0;
}