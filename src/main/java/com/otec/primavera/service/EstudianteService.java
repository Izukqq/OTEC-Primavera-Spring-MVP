package com.otec.primavera.service;

import com.otec.primavera.model.Curso;
import com.otec.primavera.model.Estudiante;
import com.otec.primavera.model.Evaluacion;
import com.otec.primavera.model.Nota;
import com.otec.primavera.model.Role;
import com.otec.primavera.repository.CursoRepository;
import com.otec.primavera.repository.EstudianteRepository;
import com.otec.primavera.repository.EvaluacionRepository;
import com.otec.primavera.repository.NotaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class EstudianteService {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private EvaluacionRepository evaluacionRepository;

    @Autowired
    private NotaRepository notaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Estudiante> listarTodos() {
        return estudianteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Estudiante> listarSoloEstudiantes() {
        return estudianteRepository.findByRoleNot(Role.ROLE_ADMIN);
    }

    @Transactional(readOnly = true)
    public Estudiante buscarPorId(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + id));
    }

    @Transactional
    public Estudiante guardar(Estudiante estudiante) {
        return estudianteRepository.save(estudiante);
    }

    @Transactional
    public void eliminar(Long id) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede eliminar. Estudiante no encontrado con ID: " + id));
        estudianteRepository.delete(estudiante);
    }

    @Transactional
    public Estudiante actualizarProgreso(Long estudianteId, Integer nuevoProgreso) {
        if (nuevoProgreso < 0 || nuevoProgreso > 100) {
            throw new IllegalArgumentException("El progreso debe estar entre 0 y 100. Valor recibido: " + nuevoProgreso);
        }

        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + estudianteId));

        estudiante.setProgreso(nuevoProgreso);
        return estudianteRepository.save(estudiante);
    }

    @Transactional
    public Estudiante matricularEnCurso(Long estudianteId, Long cursoId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + estudianteId));

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado con ID: " + cursoId));

        estudiante.setCurso(curso);
        estudiante.setProgreso(0);
        return estudianteRepository.save(estudiante);
    }

    @Transactional(readOnly = true)
    public Optional<Estudiante> buscarPorEmail(String email) {
        return estudianteRepository.findByEmail(email);
    }

    @Transactional
    public Estudiante registrarEstudiante(Estudiante estudiante) {
        if (estudianteRepository.existsByEmail(estudiante.getEmail())) {
            throw new RuntimeException("Ya existe una cuenta con el correo: " + estudiante.getEmail());
        }
        estudiante.setPassword(passwordEncoder.encode(estudiante.getPassword()));
        estudiante.setRole(Role.ROLE_USER);
        estudiante.setProgreso(0);
        return estudianteRepository.save(estudiante);
    }

    @Transactional
    public Nota agregarNota(Long estudianteId, Long evaluacionId, Nota nota) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + estudianteId));

        Evaluacion evaluacion = evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new RuntimeException("Evaluación no encontrada con ID: " + evaluacionId));

        if (estudiante.getCurso() == null || !estudiante.getCurso().getId().equals(evaluacion.getCurso().getId())) {
            throw new RuntimeException("El estudiante no está matriculado en el curso de esta evaluación.");
        }

        nota.setEstudiante(estudiante);
        nota.setEvaluacion(evaluacion);
        Nota guardada = notaRepository.save(nota);

        recalcularProgreso(estudiante);
        return guardada;
    }

    @Transactional(readOnly = true)
    public List<Nota> listarNotas(Long estudianteId) {
        return notaRepository.findByEstudianteId(estudianteId);
    }

    @Transactional(readOnly = true)
    public List<Nota> listarNotasPorCurso(Long estudianteId, Long cursoId) {
        return notaRepository.findByEstudianteIdAndEvaluacionCursoId(estudianteId, cursoId);
    }

    @Transactional
    public void recalcularProgreso(Estudiante estudiante) {
        if (estudiante.getCurso() == null) {
            estudiante.setProgreso(0);
        } else {
            Long cursoId = estudiante.getCurso().getId();
            long totalEvaluaciones = evaluacionRepository.countByCursoId(cursoId);
            if (totalEvaluaciones == 0) {
                estudiante.setProgreso(0);
            } else {
                long notasRegistradas = notaRepository.countByEstudianteIdAndEvaluacionCursoId(
                        estudiante.getId(), cursoId);
                int progreso = (int) Math.min((notasRegistradas * 100) / totalEvaluaciones, 100);
                estudiante.setProgreso(progreso);
            }
        }
        estudianteRepository.save(estudiante);
    }

    @Transactional
    public void recalcularProgresoPorCurso(Long cursoId) {
        List<Estudiante> estudiantes = estudianteRepository.findByCursoId(cursoId);
        long totalEvaluaciones = evaluacionRepository.countByCursoId(cursoId);
        for (Estudiante est : estudiantes) {
            if (totalEvaluaciones == 0) {
                est.setProgreso(0);
            } else {
                long notasRegistradas = notaRepository.countByEstudianteIdAndEvaluacionCursoId(
                        est.getId(), cursoId);
                int progreso = (int) Math.min((notasRegistradas * 100) / totalEvaluaciones, 100);
                est.setProgreso(progreso);
            }
            estudianteRepository.save(est);
        }
    }
}
