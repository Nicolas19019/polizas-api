package com.prueba.polizas.configuracion;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionOpenApi {
  @Bean
  public OpenAPI documentacionPolizas() {
    return new OpenAPI()
        .info(
            new Info()
                .title("API de Gestion de Polizas")
                .version("1.0")
                .description("Consulta, renovacion y cancelacion de polizas y riesgos."))
        .components(
            new Components()
                .addSecuritySchemes(
                    "ClaveApi",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("x-api-key")))
        .addSecurityItem(new SecurityRequirement().addList("ClaveApi"));
  }
}
