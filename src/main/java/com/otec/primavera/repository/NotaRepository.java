package com.otec.primavera.repository;

import com.otec.primavera.model.Nota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotaRepository extends JpaRepository<Nota, Long> {

    List<Nota> findByEstudianteId(Long estudianteId);

    List<Nota> findByEstudianteIdAndEvaluacionCursoId(Long estudianteId, Long cursoId);

    long countByEstudianteIdAndEvaluacionCursoId(Long estudianteId, Long cursoId);
}
