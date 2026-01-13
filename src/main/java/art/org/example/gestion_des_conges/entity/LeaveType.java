package art.org.example.gestion_des_conges.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "leave_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // "Congé payé", "Maladie", "Sans solde", "Exceptionnel"

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean requiresApproval = true; // Nécessite validation ?

    @Column(nullable = false)
    private Boolean isPaid = true; // Congé payé ou non ?

    @Column(nullable = false)
    private Boolean deductsFromBalance = true; // Déduit du solde ?

    @Column(nullable = false)
    private Double maxDaysPerYear = 25.0; // Nombre max de jours par an

    @Column(nullable = false)
    private Double defaultAnnualAllowance = 25.0; // Droits annuels par défaut

    @Column(nullable = false)
    private Boolean allowCarryOver = true; // Permet le report ?

    @Column
    private Integer maxCarryOverDays = 10; // Max jours reportables

    @Column(nullable = false)
    private Boolean isActive = true; // Type actif/inactif

    @Column
    private String color = "#3B82F6"; // Couleur pour le planning (hex)

    @Column(nullable = false)
    private Integer displayOrder = 0; // Ordre d'affichage

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Méthode utilitaire pour vérifier si c'est un type système (non supprimable)
    public boolean isSystemType() {
        return "CONGE_PAYE".equals(name) ||
                "MALADIE".equals(name) ||
                "SANS_SOLDE".equals(name) ||
                "EXCEPTIONNEL".equals(name);
    }
}