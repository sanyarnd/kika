package kika.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(openApiInformation());
    }

    private Info openApiInformation() {
        return new Info().title("Kika API").contact(new Contact().name("Ekaterina Biryukova").url("https://google.com"));
    }
}
