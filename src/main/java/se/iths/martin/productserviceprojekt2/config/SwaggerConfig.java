package se.iths.martin.productserviceprojekt2.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfigurerar API-dokumentation och JWT Bearer-autentisering i Swagger.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Skapar OpenAPI-dokumentation med JWT Bearer-autentisering.
     * @return OpenAPI-konfigurationen
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Swagger Auth Token Example API")
                        .description("Demo for centralized token authentication using Swagger @SecurityRequirement.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("authorization"))
                .components(new Components()
                        .addSecuritySchemes("authorization",
                                new SecurityScheme()
                                        .name("authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .description("JWT auth description")));
    }

    /**
     * Väljer vilka API-sökvägar som visas i Swagger.
     * @return API-gruppen som visas i Swagger
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .build();
    }
}
