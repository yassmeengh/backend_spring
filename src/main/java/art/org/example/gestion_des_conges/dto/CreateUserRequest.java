package art.org.example.gestion_des_conges.dto;

import art.org.example.gestion_des_conges.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank private String nom;
    @NotBlank private String prenom;
    @NotBlank private String username;
    @Email @NotBlank private String email;
    private User.Role role;
    private Long teamId;
}