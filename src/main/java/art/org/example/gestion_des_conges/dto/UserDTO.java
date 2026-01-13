package art.org.example.gestion_des_conges.dto;

import art.org.example.gestion_des_conges.entity.User;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String nom;        // lastName
    private String prenom;     // firstName
    private String username;
    private String email;
    private User.Role role;
    private Boolean actif;     // active
    private Long teamId;
    private String teamNom;

}