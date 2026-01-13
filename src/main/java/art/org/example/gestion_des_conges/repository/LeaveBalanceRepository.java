package art.org.example.gestion_des_conges.repository;

import art.org.example.gestion_des_conges.entity.LeaveBalance;
import art.org.example.gestion_des_conges.entity.LeaveType;
import art.org.example.gestion_des_conges.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    // Trouver le solde d'un utilisateur pour un type et une année
    Optional<LeaveBalance> findByUserAndLeaveTypeAndYear(User user, LeaveType leaveType, Integer year);

    // Trouver tous les soldes d'un utilisateur pour une année
    List<LeaveBalance> findByUserAndYear(User user, Integer year);

    // Trouver tous les soldes d'un utilisateur (toutes années)
    List<LeaveBalance> findByUser(User user);

    // Trouver les soldes par utilisateur ID
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.user.id = :userId AND lb.year = :year")
    List<LeaveBalance> findByUserIdAndYear(@Param("userId") Long userId, @Param("year") Integer year);

    // Trouver les utilisateurs avec solde faible
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.remainingDays < :threshold AND lb.year = :year")
    List<LeaveBalance> findUsersWithLowBalance(@Param("threshold") Double threshold, @Param("year") Integer year);

    // Trouver les soldes à reporter (année précédente)
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.year = :year AND lb.remainingDays > 0 " +
            "AND lb.leaveType.allowCarryOver = true")
    List<LeaveBalance> findBalancesToCarryOver(@Param("year") Integer year);

    // Vérifier si un solde existe
    @Query("SELECT CASE WHEN COUNT(lb) > 0 THEN true ELSE false END " +
            "FROM LeaveBalance lb WHERE lb.user.id = :userId AND lb.leaveType.id = :leaveTypeId AND lb.year = :year")
    boolean existsByUserIdAndLeaveTypeIdAndYear(@Param("userId") Long userId,
                                                @Param("leaveTypeId") Long leaveTypeId,
                                                @Param("year") Integer year);

    // Statistiques : Total jours utilisés par équipe
    @Query("SELECT SUM(lb.usedDays) FROM LeaveBalance lb " +
            "WHERE lb.user.team.id = :teamId AND lb.year = :year")
    Double getTotalUsedDaysByTeam(@Param("teamId") Long teamId, @Param("year") Integer year);

    // Supprimer les soldes d'un utilisateur
    void deleteByUser(User user);
}