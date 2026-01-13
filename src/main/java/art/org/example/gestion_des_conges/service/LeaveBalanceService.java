package art.org.example.gestion_des_conges.service;

import art.org.example.gestion_des_conges.dto.LeaveBalanceDTO;
import art.org.example.gestion_des_conges.entity.LeaveBalance;
import art.org.example.gestion_des_conges.entity.LeaveType;
import art.org.example.gestion_des_conges.entity.User;
import art.org.example.gestion_des_conges.repository.LeaveBalanceRepository;
import art.org.example.gestion_des_conges.repository.LeaveTypeRepository;
import art.org.example.gestion_des_conges.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveBalanceService {

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    /**
     * US-03.3 : Initialiser les soldes d'un utilisateur pour une année
     */
    @Transactional
    public void initializeUserBalances(Long userId, Integer year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        // Récupérer tous les types de congés actifs
        List<LeaveType> activeLeaveTypes = leaveTypeRepository.findByIsActiveTrue();

        for (LeaveType leaveType : activeLeaveTypes) {
            // Vérifier si le solde existe déjà
            if (!leaveBalanceRepository.existsByUserIdAndLeaveTypeIdAndYear(userId, leaveType.getId(), year)) {
                LeaveBalance balance = new LeaveBalance();
                balance.setUser(user);
                balance.setLeaveType(leaveType);
                balance.setYear(year);
                balance.setTotalAllowance(leaveType.getDefaultAnnualAllowance());
                balance.setUsedDays(0.0);
                balance.setPendingDays(0.0);
                balance.setRemainingDays(leaveType.getDefaultAnnualAllowance());
                balance.setCarriedOverDays(0.0);

                leaveBalanceRepository.save(balance);
            }
        }
    }

    /**
     * US-03.5 : Consulter les soldes d'un utilisateur
     */
    public List<LeaveBalanceDTO> getUserBalances(Long userId, Integer year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        // Si aucun solde n'existe pour cette année, les initialiser
        List<LeaveBalance> balances = leaveBalanceRepository.findByUserIdAndYear(userId, year);
        if (balances.isEmpty()) {
            initializeUserBalances(userId, year);
            balances = leaveBalanceRepository.findByUserIdAndYear(userId, year);
        }

        return balances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * US-03.3 : Définir manuellement le solde d'un utilisateur
     */
    @Transactional
    public LeaveBalanceDTO setUserBalance(Long userId, Long leaveTypeId, Integer year, Double allowance) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Type de congé non trouvé"));

        LeaveBalance balance = leaveBalanceRepository
                .findByUserAndLeaveTypeAndYear(user, leaveType, year)
                .orElseGet(() -> {
                    LeaveBalance newBalance = new LeaveBalance();
                    newBalance.setUser(user);
                    newBalance.setLeaveType(leaveType);
                    newBalance.setYear(year);
                    newBalance.setUsedDays(0.0);
                    newBalance.setPendingDays(0.0);
                    newBalance.setCarriedOverDays(0.0);
                    return newBalance;
                });

        balance.setTotalAllowance(allowance);
        balance.recalculateRemaining();

        LeaveBalance savedBalance = leaveBalanceRepository.save(balance);
        return convertToDTO(savedBalance);
    }

    /**
     * US-03.4 : Reporter les soldes de l'année précédente
     */
    @Transactional
    public void carryOverBalances(Integer fromYear, Integer toYear) {
        List<LeaveBalance> balancesToCarryOver = leaveBalanceRepository.findBalancesToCarryOver(fromYear);

        for (LeaveBalance oldBalance : balancesToCarryOver) {
            LeaveType leaveType = oldBalance.getLeaveType();

            // Calculer les jours à reporter
            Double daysToCarryOver = oldBalance.getRemainingDays();
            if (leaveType.getMaxCarryOverDays() != null) {
                daysToCarryOver = Math.min(daysToCarryOver, leaveType.getMaxCarryOverDays());
            }

            if (daysToCarryOver > 0) {
                // Créer ou mettre à jour le solde de l'année suivante
                LeaveBalance newBalance = leaveBalanceRepository
                        .findByUserAndLeaveTypeAndYear(oldBalance.getUser(), leaveType, toYear)
                        .orElseGet(() -> {
                            LeaveBalance nb = new LeaveBalance();
                            nb.setUser(oldBalance.getUser());
                            nb.setLeaveType(leaveType);
                            nb.setYear(toYear);
                            nb.setUsedDays(0.0);
                            nb.setPendingDays(0.0);
                            return nb;
                        });

                newBalance.setCarriedOverDays(daysToCarryOver);
                newBalance.setTotalAllowance(leaveType.getDefaultAnnualAllowance() + daysToCarryOver);
                newBalance.recalculateRemaining();

                leaveBalanceRepository.save(newBalance);
            }
        }
    }

    /**
     * Initialiser tous les utilisateurs pour une année
     */
    @Transactional
    public void initializeAllUsersForYear(Integer year) {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            initializeUserBalances(user.getId(), year);
        }
    }

    /**
     * Obtenir le solde d'un utilisateur pour un type spécifique
     */
    public LeaveBalanceDTO getUserBalanceForType(Long userId, Long leaveTypeId, Integer year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Type de congé non trouvé"));

        LeaveBalance balance = leaveBalanceRepository
                .findByUserAndLeaveTypeAndYear(user, leaveType, year)
                .orElseThrow(() -> new EntityNotFoundException("Solde non trouvé"));

        return convertToDTO(balance);
    }

    /**
     * Mettre à jour un solde après validation/refus d'une demande
     * Actions possibles : PENDING, APPROVED, REJECTED, CANCELLED
     */
    @Transactional
    public void updateBalanceAfterRequest(Long userId, Long leaveTypeId, Integer year,
                                          Double days, String action) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Type de congé non trouvé"));

        LeaveBalance balance = leaveBalanceRepository
                .findByUserAndLeaveTypeAndYear(user, leaveType, year)
                .orElseThrow(() -> new EntityNotFoundException("Solde non trouvé"));

        switch (action) {
            case "PENDING":
                // Demande soumise → Bloquer les jours
                balance.setPendingDays(balance.getPendingDays() + days);
                break;
            case "APPROVED":
                // Demande validée → Débloquer et déduire
                balance.setPendingDays(balance.getPendingDays() - days);
                balance.setUsedDays(balance.getUsedDays() + days);
                break;
            case "REJECTED":
                // Demande refusée → Débloquer sans déduire
                balance.setPendingDays(balance.getPendingDays() - days);
                break;
            case "CANCELLED":
                // Demande annulée → Rendre les jours
                balance.setUsedDays(balance.getUsedDays() - days);
                break;
            default:
                throw new IllegalArgumentException("Action invalide : " + action);
        }

        balance.recalculateRemaining();
        leaveBalanceRepository.save(balance);
    }

    /**
     * Vérifier si un utilisateur a assez de solde
     */
    public boolean hasSufficientBalance(Long userId, Long leaveTypeId, Integer year, Double requestedDays) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

            LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                    .orElseThrow(() -> new EntityNotFoundException("Type de congé non trouvé"));

            LeaveBalance balance = leaveBalanceRepository
                    .findByUserAndLeaveTypeAndYear(user, leaveType, year)
                    .orElse(null);

            if (balance == null) {
                return false;
            }

            return balance.hasSufficientBalance(requestedDays);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtenir les utilisateurs avec un solde faible
     */
    public List<LeaveBalanceDTO> getUsersWithLowBalance(Double threshold, Integer year) {
        List<LeaveBalance> lowBalances = leaveBalanceRepository.findUsersWithLowBalance(threshold, year);
        return lowBalances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertir LeaveBalance en LeaveBalanceDTO
     */
    private LeaveBalanceDTO convertToDTO(LeaveBalance balance) {
        LeaveBalanceDTO dto = new LeaveBalanceDTO();
        dto.setId(balance.getId());
        dto.setUserId(balance.getUser().getId());
        dto.setUserName(balance.getUser().getFirstName() + " " + balance.getUser().getLastName());
        dto.setLeaveTypeId(balance.getLeaveType().getId());
        dto.setLeaveTypeName(balance.getLeaveType().getName());
        dto.setLeaveTypeColor(balance.getLeaveType().getColor());
        dto.setYear(balance.getYear());
        dto.setTotalAllowance(balance.getTotalAllowance());
        dto.setUsedDays(balance.getUsedDays());
        dto.setPendingDays(balance.getPendingDays());
        dto.setRemainingDays(balance.getRemainingDays());
        dto.setCarriedOverDays(balance.getCarriedOverDays());
        dto.setAvailableDays(balance.getRemainingDays());

        // Calculer le pourcentage utilisé
        if (balance.getTotalAllowance() > 0) {
            dto.setPercentageUsed((int) ((balance.getUsedDays() / balance.getTotalAllowance()) * 100));
        } else {
            dto.setPercentageUsed(0);
        }

        return dto;
    }
}




