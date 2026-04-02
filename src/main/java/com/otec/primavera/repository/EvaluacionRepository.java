package com.otec.primavera.repository;

import com.otec.primavera.model.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    @Query("SELECT e FROM Evaluacion e JOIN FETCH e.curso")
    List<Evaluacion> findAllConCurso();

    List<Evaluacion> findByCursoId(Long cursoId);

    long countByCursoId(Long cursoId);
}
