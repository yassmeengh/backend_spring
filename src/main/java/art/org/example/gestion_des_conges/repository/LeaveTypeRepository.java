package art.org.example.gestion_des_conges.repository;

import art.org.example.gestion_des_conges.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    // Trouver par nom
    Optional<LeaveType> findByName(String name);

    // Vérifier si un nom existe
    boolean existsByName(String name);

    // Trouver les types actifs uniquement
    List<LeaveType> findByIsActiveTrue();

    // Trouver par ordre d'affichage
    List<LeaveType> findAllByOrderByDisplayOrderAsc();

    // Trouver les types actifs par ordre d'affichage
    List<LeaveType> findByIsActiveTrueOrderByDisplayOrderAsc();

    // Recherche par nom ou description
    @Query("SELECT lt FROM LeaveType lt WHERE " +
            "LOWER(lt.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(lt.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<LeaveType> searchLeaveTypes(@Param("search") String search);

    // Vérifier unicité du nom (sauf pour l'ID courant)
    @Query("SELECT CASE WHEN COUNT(lt) > 0 THEN true ELSE false END " +
            "FROM LeaveType lt WHERE LOWER(lt.name) = LOWER(:name) AND lt.id != :excludeId")
    boolean existsByNameExcludingId(@Param("name") String name, @Param("excludeId") Long excludeId);

    // Trouver les types qui nécessitent validation
    List<LeaveType> findByRequiresApprovalTrue();

    // Trouver les types payés
    List<LeaveType> findByIsPaidTrue();

    // Trouver les types qui permettent le report
    List<LeaveType> findByAllowCarryOverTrue();
}