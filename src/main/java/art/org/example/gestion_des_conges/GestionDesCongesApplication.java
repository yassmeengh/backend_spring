package art.org.example.gestion_des_conges;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("art.org.example.gestion_des_conges.entity") // ✅ Change to lowercase "entity"
@EnableJpaRepositories("art.org.example.gestion_des_conges.repository")
public class GestionDesCongesApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionDesCongesApplication.class, args);
    }
}