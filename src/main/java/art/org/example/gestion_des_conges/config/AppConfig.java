package art.org.example.gestion_des_conges.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private int defaultPasswordLength = 8;
    private String frontendUrl = "http://localhost:3000";

    // Validation après initialisation
    @PostConstruct
    public void validate() {
        if (defaultPasswordLength < 6) {
            throw new IllegalArgumentException("defaultPasswordLength doit être au moins 6");
        }

        if (!StringUtils.hasText(frontendUrl)) {
            throw new IllegalArgumentException("frontendUrl ne peut pas être vide");
        }

        System.out.println("✅ AppConfig chargé avec succès: " + this);
    }

    // Getters et Setters
    public int getDefaultPasswordLength() {
        return defaultPasswordLength;
    }

    public void setDefaultPasswordLength(int defaultPasswordLength) {
        this.defaultPasswordLength = defaultPasswordLength;
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                "defaultPasswordLength=" + defaultPasswordLength +
                ", frontendUrl='" + frontendUrl + '\'' +
                '}';
    }
}