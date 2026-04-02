package com.otec.primavera.controller.rest;

import com.otec.primavera.dto.CursoDTO;
import com.otec.primavera.model.Curso;
import com.otec.primavera.service.CursoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoRestController {

    @Autowired
    private CursoService cursoService;

    @GetMapping
    public ResponseEntity<List<CursoDTO>> listarTodos() {
        List<CursoDTO> cursos = cursoService.listarTodos().stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoDTO> buscarPorId(@PathVariable Long id) {
        Curso curso = cursoService.buscarPorId(id);
        return ResponseEntity.ok(toDTO(curso));
    }

    @PostMapping
    public ResponseEntity<CursoDTO> crear(@Valid @RequestBody Curso curso) {
        Curso guardado = cursoService.guardar(curso);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody Curso cursoActualizado) {
        Curso existente = cursoService.buscarPorId(id);
        existente.setNombre(cursoActualizado.getNombre());
        existente.setDescripcion(cursoActualizado.getDescripcion());
        existente.setFechaInicio(cursoActualizado.getFechaInicio());
        Curso guardado = cursoService.guardar(existente);
        return ResponseEntity.ok(toDTO(guardado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private CursoDTO toDTO(Curso curso) {
        return new CursoDTO(
                curso.getId(),
                curso.getNombre(),
                curso.getDescripcion(),
                curso.getFechaInicio()
        );
    }
}
