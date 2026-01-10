package art.org.example.gestion_des_conges.dto;

import art.org.example.gestion_des_conges.entity.User;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String nom;
    private String prenom;
    private String username;
    private String email;
    private User.Role role;
    private Boolean actif;
    private Long teamId;
}