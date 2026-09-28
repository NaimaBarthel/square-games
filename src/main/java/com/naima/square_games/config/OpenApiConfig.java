package com.naima.square_games.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI squareGamesOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Square Games API")
                        .description("API REST pour la gestion, la persistance et le déroulement au tour par tour des jeux de plateau (TicTacToe, Taquin, ConnectFour).")
                        .version("1.0.0")
                        .contact(new Contact().name("Équipe de développement Square Games")));

    }
}
