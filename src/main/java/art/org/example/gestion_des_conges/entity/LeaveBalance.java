package art.org.example.gestion_des_conges.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "leave_balances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "leave_type_id", "year"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer year; // 2025

    @Column(nullable = false)
    private Double totalAllowance = 0.0; // Droits totaux (initial + reportés)

    @Column(nullable = false)
    private Double usedDays = 0.0; // Jours utilisés

    @Column(nullable = false)
    private Double pendingDays = 0.0; // Jours en attente de validation

    @Column(nullable = false)
    private Double remainingDays = 0.0; // Jours restants

    @Column(nullable = false)
    private Double carriedOverDays = 0.0; // Jours reportés de l'année précédente

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Méthode utilitaire pour recalculer le solde
    public void recalculateRemaining() {
        this.remainingDays = this.totalAllowance - this.usedDays - this.pendingDays;
    }

    // Vérifier si assez de solde disponible
    public boolean hasSufficientBalance(Double requestedDays) {
        return this.remainingDays >= requestedDays;
    }
}
