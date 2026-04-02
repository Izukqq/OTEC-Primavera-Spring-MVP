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
class EstudianteServiceTest {

    @Autowired
    private EstudianteService estudianteService;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private EvaluacionRepository evaluacionRepository;

    @Autowired
    private NotaRepository notaRepository;

    private Curso curso;
    private Estudiante estudiante;

    @BeforeEach
    void setUp() {
        notaRepository.deleteAll();
        evaluacionRepository.deleteAll();
        estudianteRepository.deleteAll();
        cursoRepository.deleteAll();

        curso = new Curso("Java Avanzado", "Curso de Java", LocalDate.of(2026, 3, 1));
        curso = cursoRepository.save(curso);

        estudiante = new Estudiante("Test User", "test@otec.cl", 0, curso);
        estudiante.setPassword("encoded");
        estudiante.setRole(Role.ROLE_USER);
        estudiante = estudianteRepository.save(estudiante);
    }

    // --- CRUD ---

    @Test
    void listarTodos_retornaTodosLosEstudiantes() {
        List<Estudiante> lista = estudianteService.listarTodos();
        assertFalse(lista.isEmpty());
    }

    @Test
    void listarSoloEstudiantes_excluyeAdmin() {
        Estudiante admin = new Estudiante("Admin", "admin@test.cl", 0, null);
        admin.setPassword("pass");
        admin.setRole(Role.ROLE_ADMIN);
        estudianteRepository.save(admin);

        List<Estudiante> lista = estudianteService.listarSoloEstudiantes();
        assertTrue(lista.stream().noneMatch(e -> e.getRole() == Role.ROLE_ADMIN));
    }

    @Test
    void buscarPorId_existente_retornaEstudiante() {
        Estudiante encontrado = estudianteService.buscarPorId(estudiante.getId());
        assertEquals("test@otec.cl", encontrado.getEmail());
    }

    @Test
    void buscarPorId_noExistente_lanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> estudianteService.buscarPorId(99999L));
    }

    @Test
    void guardar_persisteEstudiante() {
        Estudiante nuevo = new Estudiante("Nuevo", "nuevo@otec.cl", 0, null);
        Estudiante guardado = estudianteService.guardar(nuevo);
        assertNotNull(guardado.getId());
    }

    @Test
    void eliminar_existente_eliminaEstudiante() {
        Long id = estudiante.getId();
        estudianteService.eliminar(id);
        assertFalse(estudianteRepository.findById(id).isPresent());
    }

    @Test
    void eliminar_noExistente_lanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> estudianteService.eliminar(99999L));
    }

    // --- Matriculación ---

    @Test
    void matricularEnCurso_asignaCursoYResetProgreso() {
        estudiante.setProgreso(50);
        estudianteRepository.save(estudiante);

        Curso otroCurso = cursoRepository.save(new Curso("Python", "Curso Python", LocalDate.of(2026, 4, 1)));
        Estudiante matriculado = estudianteService.matricularEnCurso(estudiante.getId(), otroCurso.getId());

        assertEquals(otroCurso.getId(), matriculado.getCurso().getId());
        assertEquals(0, matriculado.getProgreso());
    }

    @Test
    void matricularEnCurso_cursoNoExistente_lanzaExcepcion() {
        assertThrows(RuntimeException.class,
                () -> estudianteService.matricularEnCurso(estudiante.getId(), 99999L));
    }

    // --- Registro ---

    @Test
    void registrarEstudiante_creaConRoleUserYProgreso0() {
        Estudiante nuevo = new Estudiante();
        nuevo.setNombre("Registro Test");
        nuevo.setEmail("registro@otec.cl");
        nuevo.setPassword("password123");

        Estudiante registrado = estudianteService.registrarEstudiante(nuevo);

        assertEquals(Role.ROLE_USER, registrado.getRole());
        assertEquals(0, registrado.getProgreso());
        assertNotEquals("password123", registrado.getPassword());
    }

    @Test
    void registrarEstudiante_emailDuplicado_lanzaExcepcion() {
        Estudiante duplicado = new Estudiante();
        duplicado.setNombre("Duplicado");
        duplicado.setEmail("test@otec.cl");
        duplicado.setPassword("pass");

        assertThrows(RuntimeException.class, () -> estudianteService.registrarEstudiante(duplicado));
    }

    // --- Progreso ---

    @Test
    void actualizarProgreso_valorValido_actualizaCorrectamente() {
        Estudiante actualizado = estudianteService.actualizarProgreso(estudiante.getId(), 75);
        assertEquals(75, actualizado.getProgreso());
    }

    @Test
    void actualizarProgreso_valorNegativo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> estudianteService.actualizarProgreso(estudiante.getId(), -1));
    }

    @Test
    void actualizarProgreso_valorMayor100_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> estudianteService.actualizarProgreso(estudiante.getId(), 101));
    }

    // --- Notas y cálculo de progreso ---

    @Test
    void agregarNota_recalculaProgreso() {
        Evaluacion ev1 = evaluacionRepository.save(new Evaluacion("Eval 1", "Desc", curso));
        evaluacionRepository.save(new Evaluacion("Eval 2", "Desc", curso));

        Nota nota = new Nota();
        nota.setCalificacion(6.0);
        nota.setFecha(LocalDate.now());
        estudianteService.agregarNota(estudiante.getId(), ev1.getId(), nota);

        Estudiante actualizado = estudianteRepository.findById(estudiante.getId()).orElseThrow();
        assertEquals(50, actualizado.getProgreso()); // 1 nota / 2 evaluaciones = 50%
    }

    @Test
    void agregarNota_completaTodasLasEvaluaciones_progreso100() {
        Evaluacion ev1 = evaluacionRepository.save(new Evaluacion("Eval 1", "Desc", curso));
        Evaluacion ev2 = evaluacionRepository.save(new Evaluacion("Eval 2", "Desc", curso));

        Nota n1 = new Nota();
        n1.setCalificacion(5.0);
        n1.setFecha(LocalDate.now());
        estudianteService.agregarNota(estudiante.getId(), ev1.getId(), n1);

        Nota n2 = new Nota();
        n2.setCalificacion(6.5);
        n2.setFecha(LocalDate.now());
        estudianteService.agregarNota(estudiante.getId(), ev2.getId(), n2);

        Estudiante actualizado = estudianteRepository.findById(estudiante.getId()).orElseThrow();
        assertEquals(100, actualizado.getProgreso());
    }

    @Test
    void agregarNota_estudianteNoMatriculadoEnCurso_lanzaExcepcion() {
        Curso otroCurso = cursoRepository.save(new Curso("Otro", "Otro curso", LocalDate.of(2026, 5, 1)));
        Evaluacion evOtroCurso = evaluacionRepository.save(new Evaluacion("Eval Otro", "Desc", otroCurso));

        Nota nota = new Nota();
        nota.setCalificacion(5.0);
        nota.setFecha(LocalDate.now());

        assertThrows(RuntimeException.class,
                () -> estudianteService.agregarNota(estudiante.getId(), evOtroCurso.getId(), nota));
    }

    @Test
    void agregarNota_estudianteSinCurso_lanzaExcepcion() {
        Estudiante sinCurso = new Estudiante("Sin Curso", "sincurso@otec.cl", 0, null);
        sinCurso.setPassword("pass");
        sinCurso.setRole(Role.ROLE_USER);
        sinCurso = estudianteRepository.save(sinCurso);

        Evaluacion ev = evaluacionRepository.save(new Evaluacion("Eval", "Desc", curso));

        Nota nota = new Nota();
        nota.setCalificacion(5.0);
        nota.setFecha(LocalDate.now());

        Long sinCursoId = sinCurso.getId();
        Long evId = ev.getId();
        assertThrows(RuntimeException.class, () -> estudianteService.agregarNota(sinCursoId, evId, nota));
    }

    @Test
    void recalcularProgreso_sinCurso_progreso0() {
        estudiante.setCurso(null);
        estudianteRepository.save(estudiante);

        estudianteService.recalcularProgreso(estudiante);

        Estudiante actualizado = estudianteRepository.findById(estudiante.getId()).orElseThrow();
        assertEquals(0, actualizado.getProgreso());
    }

    @Test
    void recalcularProgreso_cursoSinEvaluaciones_progreso0() {
        estudianteService.recalcularProgreso(estudiante);

        Estudiante actualizado = estudianteRepository.findById(estudiante.getId()).orElseThrow();
        assertEquals(0, actualizado.getProgreso());
    }

    @Test
    void recalcularProgresoPorCurso_actualizaTodosLosEstudiantes() {
        Estudiante est2 = new Estudiante("Otro", "otro@otec.cl", 0, curso);
        est2.setPassword("pass");
        est2.setRole(Role.ROLE_USER);
        est2 = estudianteRepository.save(est2);

        Evaluacion ev1 = evaluacionRepository.save(new Evaluacion("Eval 1", "Desc", curso));
        Evaluacion ev2 = evaluacionRepository.save(new Evaluacion("Eval 2", "Desc", curso));
        evaluacionRepository.save(new Evaluacion("Eval 3", "Desc", curso));

        // Estudiante 1: 2 notas de 3 evaluaciones = 66%
        Nota n1 = new Nota(5.0, LocalDate.now(), estudiante, ev1);
        Nota n2 = new Nota(6.0, LocalDate.now(), estudiante, ev2);
        notaRepository.save(n1);
        notaRepository.save(n2);

        // Estudiante 2: 1 nota de 3 evaluaciones = 33%
        Nota n3 = new Nota(4.5, LocalDate.now(), est2, ev1);
        notaRepository.save(n3);

        estudianteService.recalcularProgresoPorCurso(curso.getId());

        Estudiante act1 = estudianteRepository.findById(estudiante.getId()).orElseThrow();
        Estudiante act2 = estudianteRepository.findById(est2.getId()).orElseThrow();

        assertEquals(66, act1.getProgreso());
        assertEquals(33, act2.getProgreso());
    }

    // --- Listar notas ---

    @Test
    void listarNotas_retornaNotasDelEstudiante() {
        Evaluacion ev = evaluacionRepository.save(new Evaluacion("Eval", "Desc", curso));
        notaRepository.save(new Nota(6.0, LocalDate.now(), estudiante, ev));

        List<Nota> notas = estudianteService.listarNotas(estudiante.getId());
        assertEquals(1, notas.size());
    }

    @Test
    void listarNotasPorCurso_filtraPorCurso() {
        Curso otroCurso = cursoRepository.save(new Curso("Otro", "Otro", LocalDate.of(2026, 5, 1)));

        Evaluacion ev1 = evaluacionRepository.save(new Evaluacion("Eval Curso1", "Desc", curso));
        Evaluacion ev2 = evaluacionRepository.save(new Evaluacion("Eval Curso2", "Desc", otroCurso));

        notaRepository.save(new Nota(5.0, LocalDate.now(), estudiante, ev1));

        // Crear otro estudiante matriculado en otroCurso para la 2da nota
        Estudiante est2 = new Estudiante("Otro", "otro2@otec.cl", 0, otroCurso);
        est2.setPassword("pass");
        est2.setRole(Role.ROLE_USER);
        est2 = estudianteRepository.save(est2);
        notaRepository.save(new Nota(6.0, LocalDate.now(), est2, ev2));

        List<Nota> notasCurso1 = estudianteService.listarNotasPorCurso(estudiante.getId(), curso.getId());
        assertEquals(1, notasCurso1.size());
        assertEquals("Eval Curso1", notasCurso1.get(0).getEvaluacion().getNombre());
    }

    // --- buscarPorEmail ---

    @Test
    void buscarPorEmail_existente_retornaEstudiante() {
        assertTrue(estudianteService.buscarPorEmail("test@otec.cl").isPresent());
    }

    @Test
    void buscarPorEmail_noExistente_retornaVacio() {
        assertTrue(estudianteService.buscarPorEmail("noexiste@otec.cl").isEmpty());
    }
}
