package com.otec.primavera.controller.rest;

import com.otec.primavera.dto.EstudianteDTO;
import com.otec.primavera.dto.NotaDTO;
import com.otec.primavera.model.Estudiante;
import com.otec.primavera.model.Nota;
import com.otec.primavera.service.EstudianteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteRestController {

    @Autowired
    private EstudianteService estudianteService;

    @GetMapping
    public ResponseEntity<List<EstudianteDTO>> listarTodos() {
        List<EstudianteDTO> estudiantes = estudianteService.listarSoloEstudiantes().stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(estudiantes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstudianteDTO> buscarPorId(@PathVariable Long id) {
        Estudiante estudiante = estudianteService.buscarPorId(id);
        return ResponseEntity.ok(toDTO(estudiante));
    }

    @PostMapping
    public ResponseEntity<EstudianteDTO> crear(@Valid @RequestBody Estudiante estudiante) {
        Estudiante guardado = estudianteService.guardar(estudiante);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstudianteDTO> actualizar(@PathVariable Long id,
                                                     @Valid @RequestBody Estudiante estudianteActualizado) {
        Estudiante existente = estudianteService.buscarPorId(id);
        existente.setNombre(estudianteActualizado.getNombre());
        existente.setEmail(estudianteActualizado.getEmail());
        existente.setProgreso(estudianteActualizado.getProgreso());
        Estudiante guardado = estudianteService.guardar(existente);
        return ResponseEntity.ok(toDTO(guardado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        estudianteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/matricular/{cursoId}")
    public ResponseEntity<EstudianteDTO> matricular(@PathVariable Long id, @PathVariable Long cursoId) {
        Estudiante matriculado = estudianteService.matricularEnCurso(id, cursoId);
        return ResponseEntity.ok(toDTO(matriculado));
    }

    @PatchMapping("/{id}/progreso")
    public ResponseEntity<EstudianteDTO> actualizarProgreso(@PathVariable Long id,
                                                             @RequestParam Integer progreso) {
        Estudiante actualizado = estudianteService.actualizarProgreso(id, progreso);
        return ResponseEntity.ok(toDTO(actualizado));
    }

    @GetMapping("/{id}/evaluaciones")
    public ResponseEntity<List<NotaDTO>> listarEvaluaciones(@PathVariable Long id) {
        List<NotaDTO> notas = estudianteService.listarNotas(id).stream()
                .map(this::toNotaDTO)
                .toList();
        return ResponseEntity.ok(notas);
    }

    private EstudianteDTO toDTO(Estudiante est) {
        return new EstudianteDTO(
                est.getId(),
                est.getNombre(),
                est.getEmail(),
                est.getProgreso(),
                est.getCurso() != null ? est.getCurso().getId() : null,
                est.getCurso() != null ? est.getCurso().getNombre() : null
        );
    }

    private NotaDTO toNotaDTO(Nota nota) {
        return new NotaDTO(
                nota.getId(),
                nota.getCalificacion(),
                nota.getFecha(),
                nota.getEvaluacion().getNombre(),
                nota.getEvaluacion().getCurso().getNombre(),
                nota.getEstudiante().getId(),
                nota.getEstudiante().getNombre()
        );
    }
}
