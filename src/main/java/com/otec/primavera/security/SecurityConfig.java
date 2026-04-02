package com.otec.primavera.security;

import com.otec.primavera.model.Estudiante;
import com.otec.primavera.model.Role;
import com.otec.primavera.repository.EstudianteRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner initAdmin(EstudianteRepository estudianteRepository, PasswordEncoder encoder) {
        return args -> {
            if (estudianteRepository.findByEmail("admin@otec.cl").isEmpty()) {
                Estudiante admin = new Estudiante();
                admin.setNombre("Administrador");
                admin.setEmail("admin@otec.cl");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole(Role.ROLE_ADMIN);
                admin.setProgreso(0);
                estudianteRepository.save(admin);
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/registro", "/css/**", "/js/**").permitAll()

                .requestMatchers("/api/**").hasRole("ADMIN")

                .requestMatchers("/cursos/nuevo", "/cursos/guardar", "/cursos/eliminar/**").hasRole("ADMIN")
                .requestMatchers("/estudiantes/nuevo", "/estudiantes/guardar",
                                 "/estudiantes/matricular", "/estudiantes/eliminar/**").hasRole("ADMIN")

                .requestMatchers("/evaluaciones/**").hasRole("ADMIN")

                .requestMatchers("/mi-progreso").hasRole("USER")

                .requestMatchers("/cursos", "/estudiantes").hasAnyRole("ADMIN", "USER")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    if (isAdmin) {
                        response.sendRedirect("/cursos");
                    } else {
                        response.sendRedirect("/mi-progreso");
                    }
                })
                .permitAll()
            )
            .httpBasic(basic -> {})
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/login?denied")
            );

        return http.build();
    }
}
