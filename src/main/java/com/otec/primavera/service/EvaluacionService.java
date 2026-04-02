package com.otec.primavera.service;

import com.otec.primavera.model.Curso;
import com.otec.primavera.model.Evaluacion;
import com.otec.primavera.repository.CursoRepository;
import com.otec.primavera.repository.EvaluacionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("null")
public class EvaluacionService {

    @Autowired
    private EvaluacionRepository evaluacionRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    @Lazy
    private EstudianteService estudianteService;

    @Transactional(readOnly = true)
    public List<Evaluacion> listarTodas() {
        return evaluacionRepository.findAllConCurso();
    }

    @Transactional(readOnly = true)
    public List<Evaluacion> listarPorCurso(Long cursoId) {
        return evaluacionRepository.findByCursoId(cursoId);
    }

    @Transactional(readOnly = true)
    public Evaluacion buscarPorId(Long id) {
        return evaluacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluación no encontrada con ID: " + id));
    }

    @Transactional
    public Evaluacion guardar(Long cursoId, Evaluacion evaluacion) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado con ID: " + cursoId));
        evaluacion.setCurso(curso);
        Evaluacion guardada = evaluacionRepository.save(evaluacion);
        estudianteService.recalcularProgresoPorCurso(cursoId);
        return guardada;
    }

    @Transactional
    public void eliminar(Long id) {
        Evaluacion evaluacion = evaluacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede eliminar. Evaluación no encontrada con ID: " + id));
        Long cursoId = evaluacion.getCurso().getId();
        evaluacionRepository.delete(evaluacion);
        estudianteService.recalcularProgresoPorCurso(cursoId);
    }
}
