package se.iths.martin.productserviceprojekt2.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Säkerhetskonfiguration för Product Service. Tjänsten agerar som en
 * OAuth2 Resource Server och verifierar inkommande JWT mot en publik
 * nyckel som exponeras av auth-service. Ingen egen session hanteras,
 * tjänsten förlitar sig på att varje anrop bär en giltig token.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Konfigurerar säkerhetsfiltret för samtliga inkommande requests.
     * Swagger-dokumentationen är publikt tillgänglig. Alla övriga endpoints
     * kräver en giltig JWT.
     *
     * @param http Spring Securitys konfigurationsobjekt
     * Den publika statussidan är tillgänglig utan JWT; API-anrop behåller sina åtkomstkrav.
     *
     * @return den byggda filterkedjan
     * @throws Exception om konfigurationen misslyckas att byggas
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/", "/index.html").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    /**
     * Definierar vilka origins, metoder och headers som tillåts i cors anrop mot tjänsten.
     * Tillåtna origins styrs via miljövariabeln {@code ALLOWED_ORIGINS} (kommaseparerad om flera).
     *
     * @return CORS konfigurationen
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Extraherar roller från JWT och gör dem tillgängliga som Spring Security behörigheter, utan prefix.
     * Rollerna förväntas ligga i JWT claimet {@code roles}.
     *
     * @return konverteraren som kopplas in i OAuth2 Resource Server filtret
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return jwtConverter;
    }

    /**
     * Skapar avkodaren som verifierar inkommande JWT:er mot auth-services publika nyckel.
     *
     * @param publicKeyUrl URL till auth-services JWK-set, satt via miljövariabeln {@code JWT_PUBLIC_URL}
     * @return avkodaren som används av OAuth2 Resource Server för att verifiera tokens signatur och giltighet
     */
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${jwt-public-key}")
            String publicKeyUrl
    ) {
        return NimbusJwtDecoder.withJwkSetUri(publicKeyUrl).build();
    }
}
