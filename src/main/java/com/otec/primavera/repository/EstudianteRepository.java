package com.otec.primavera.repository;

import com.otec.primavera.model.Estudiante;
import com.otec.primavera.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    Optional<Estudiante> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Estudiante> findByRoleNot(Role role);

    List<Estudiante> findByCursoId(Long cursoId);
}
