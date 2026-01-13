package art.org.example.gestion_des_conges.security;

import art.org.example.gestion_des_conges.config.JwtConfig;
import io.jsonwebtoken.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    /**
     * Génère un token JWT avec les informations de l'utilisateur
     * Inclut : username, rôle, et autres claims utiles
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        // Récupérer les rôles/authorities
        String authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Récupérer le rôle principal (le premier)
        String mainRole = userPrincipal.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_EMPLOYE");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles", authorities) // Stocke tous les rôles
                .claim("role", mainRole)     // Rôle principal pour accès rapide
                .claim("type", "ACCESS_TOKEN") // Type de token
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecret())
                .compact();
    }

    /**
     * Récupère le username depuis le token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * Récupère le rôle principal depuis le token
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Récupère tous les rôles depuis le token
     */
    public String[] getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String roles = claims.get("roles", String.class);
        return roles != null ? roles.split(",") : new String[0];
    }

    /**
     * Récupère la date d'expiration depuis le token
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * Vérifie si le token est valide (non expiré et bien signé)
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (SignatureException ex) {
            // Signature JWT invalide
            System.err.println("Signature JWT invalide");
        } catch (MalformedJwtException ex) {
            // Token JWT malformé
            System.err.println("Token JWT malformé");
        } catch (ExpiredJwtException ex) {
            // Token JWT expiré
            System.err.println("Token JWT expiré");
        } catch (UnsupportedJwtException ex) {
            // Token JWT non supporté
            System.err.println("Token JWT non supporté");
        } catch (IllegalArgumentException ex) {
            // Claims JWT vides
            System.err.println("Claims JWT vides");
        }
        return false;
    }

    /**
     * Vérifie si le token a expiré
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Méthode utilitaire pour extraire les claims d'un token
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtConfig.getSecret())
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Génère un token avec des claims personnalisés
     * Utile pour créer des tokens avec des informations spécifiques
     */
    public String generateTokenWithCustomClaims(String username, String role, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("roles", role)
                .claim("type", "ACCESS_TOKEN")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecret())
                .compact();
    }

    /**
     * Récupère l'ID utilisateur depuis le token (si stocké)
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Récupère le type de token
     */
    public String getTokenType(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("type", String.class);
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * Vérifie si le token contient un rôle spécifique
     */
    public boolean hasRole(String token, String role) {
        try {
            String[] roles = getRolesFromToken(token);
            for (String r : roles) {
                if (r.equals(role)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}