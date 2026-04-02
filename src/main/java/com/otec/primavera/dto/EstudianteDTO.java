package com.otec.primavera.dto;

public record EstudianteDTO(
        Long id,
        String nombre,
        String email,
        Integer progreso,
        Long cursoId,
        String cursoNombre
) {}
