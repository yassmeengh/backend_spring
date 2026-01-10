package art.org.example.gestion_des_conges.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeamDTO {
    private Long id;
    private String nom;          // name
    private String description;
    private Long validateurId;
    private String validateurNom;
    private List<UserDTO> membres;
    private Integer nombreMembres;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}