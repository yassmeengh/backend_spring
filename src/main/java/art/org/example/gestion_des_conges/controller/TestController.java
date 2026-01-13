// controller/TestController.java - NOUVEAU FICHIER
package art.org.example.gestion_des_conges.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public String publicAccess() {
        return "Accès public autorisé";
    }

    @GetMapping("/employee")
    @PreAuthorize("hasRole('EMPLOYE')")
    public String employeeAccess() {
        return "Accès employé autorisé";
    }

    @GetMapping("/validator")
    @PreAuthorize("hasRole('VALIDATEUR')")
    public String validatorAccess() {
        return "Accès validateur autorisé";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Accès administrateur autorisé";
    }

    @GetMapping("/admin-or-validator")
    @PreAuthorize("hasAnyRole('ADMIN', 'VALIDATEUR')")
    public String adminOrValidatorAccess() {
        return "Accès admin ou validateur autorisé";
    }

    @GetMapping("/authenticated")
    @PreAuthorize("isAuthenticated()")
    public String authenticatedAccess() {
        return "Accès pour tout utilisateur authentifié";
    }
}