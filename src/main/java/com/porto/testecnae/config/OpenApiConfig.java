package com.porto.testecnae.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Microservico CNAE",
                version = "1.0",
                description = "API para consulta de atividades economicas (CNAE) e cadastro de cadastros secundarios vinculados a um CNAE."
        )
)
public class OpenApiConfig {
}
