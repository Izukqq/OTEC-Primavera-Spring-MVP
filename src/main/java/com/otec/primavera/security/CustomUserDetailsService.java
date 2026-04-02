package com.otec.primavera.security;

import com.otec.primavera.model.Estudiante;
import com.otec.primavera.repository.EstudianteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Estudiante estudiante = estudianteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró usuario con correo: " + email));

        return new User(
                estudiante.getEmail(),
                estudiante.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(estudiante.getRole().name()))
        );
    }
}
