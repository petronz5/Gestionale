package com.studicommerciali.gestionale;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class GestionaleApplication {
    public static void main(String[] args) {
        SpringApplication.run(GestionaleApplication.class, args);
    }

    // Questo codice verrà eseguito solo all'avvio dell'app per stamparci l'hash corretto
    @Bean
    public CommandLineRunner generaHashCorretto(PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("=========================================================");
            System.out.println("HASH ESATTO PER 'admin123': " + passwordEncoder.encode("admin123"));
            System.out.println("=========================================================");
        };
    }
}