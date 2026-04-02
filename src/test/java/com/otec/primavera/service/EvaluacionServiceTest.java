package com.otec.primavera.service;

import com.otec.primavera.model.*;
import com.otec.primavera.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@SuppressWarnings("null")
class EvaluacionServiceTest {

    @Autowired
    private EvaluacionService evaluacionService;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private EvaluacionRepository evaluacionRepository;

    @Autowired
    private NotaRepository notaRepository;

    private Curso curso;

    @BeforeEach
    void setUp() {
        notaRepository.deleteAll();
        evaluacionRepository.deleteAll();
        estudianteRepository.deleteAll();
        cursoRepository.deleteAll();

        curso = cursoRepository.save(new Curso("Java Avanzado", "Curso de Java", LocalDate.of(2026, 3, 1)));
    }

    @Test
    void listarTodas_retornaEvaluacionesConCurso() {
        evaluacionRepository.save(new Evaluacion("Eval 1", "Desc", curso));
        evaluacionRepository.save(new Evaluacion("Eval 2", "Desc", curso));

        List<Evaluacion> lista = evaluacionService.listarTodas();
        assertEquals(2, lista.size());
        assertNotNull(lista.get(0).getCurso().getNombre());
    }

    @Test
    void listarPorCurso_filtraPorCursoId() {
        Curso otroCurso = cursoRepository.save(new Curso("Python", "Otro", LocalDate.of(2026, 4, 1)));
        evaluacionRepository.save(new Evaluacion("Eval Java", "Desc", curso));
        evaluacionRepository.save(new Evaluacion("Eval Python", "Desc", otroCurso));

        List<Evaluacion> lista = evaluacionService.listarPorCurso(curso.getId());
        assertEquals(1, lista.size());
        assertEquals("Eval Java", lista.get(0).getNombre());
    }

    @Test
    void buscarPorId_existente_retornaEvaluacion() {
        Evaluacion guardada = evaluacionRepository.save(new Evaluacion("Eval", "Desc", curso));
        Evaluacion encontrada = evaluacionService.buscarPorId(guardada.getId());
        assertEquals("Eval", encontrada.getNombre());
    }

    @Test
    void buscarPorId_noExistente_lanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> evaluacionService.buscarPorId(99999L));
    }

    @Test
    void guardar_creaEvaluacionYRecalculaProgreso() {
        Estudiante est = new Estudiante("Test", "test@otec.cl", 0, curso);
        est.setPassword("pass");
        est.setRole(Role.ROLE_USER);
        est = estudianteRepository.save(est);

        // Crear primera evaluación y agregar nota → 100%
        Evaluacion ev1 = evaluacionRepository.save(new Evaluacion("Eval 1", "Desc", curso));
        notaRepository.save(new Nota(6.0, LocalDate.now(), est, ev1));
        est.setProgreso(100);
        est = estudianteRepository.save(est);

        // Crear segunda evaluación via service → debería recalcular a 50%
        Evaluacion nuevaEval = new Evaluacion();
        nuevaEval.setNombre("Eval 2");
        nuevaEval.setDescripcion("Nueva");
        evaluacionService.guardar(curso.getId(), nuevaEval);

        Estudiante actualizado = estudianteRepository.findById(est.getId()).orElseThrow();
        assertEquals(50, actualizado.getProgreso()); // 1 nota / 2 evaluaciones
    }

    @Test
    void guardar_cursoNoExistente_lanzaExcepcion() {
        Evaluacion ev = new Evaluacion();
        ev.setNombre("Eval");
        assertThrows(RuntimeException.class, () -> evaluacionService.guardar(99999L, ev));
    }

    @Test
    void eliminar_recalculaProgresoAlAlza() {
        Estudiante est = new Estudiante("Test", "test2@otec.cl", 0, curso);
        est.setPassword("pass");
        est.setRole(Role.ROLE_USER);
        est = estudianteRepository.save(est);

        Evaluacion ev1 = evaluacionRepository.save(new Evaluacion("Eval 1", "Desc", curso));
        Evaluacion ev2 = evaluacionRepository.save(new Evaluacion("Eval 2", "Desc", curso));

        // 1 nota de 2 evaluaciones = 50%
        notaRepository.save(new Nota(6.0, LocalDate.now(), est, ev1));
        est.setProgreso(50);
        estudianteRepository.save(est);

        // Eliminar ev2 → 1 nota / 1 evaluación = 100%
        evaluacionService.eliminar(ev2.getId());

        Estudiante actualizado = estudianteRepository.findById(est.getId()).orElseThrow();
        assertEquals(100, actualizado.getProgreso());
    }

    @Test
    void eliminar_noExistente_lanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> evaluacionService.eliminar(99999L));
    }
}
