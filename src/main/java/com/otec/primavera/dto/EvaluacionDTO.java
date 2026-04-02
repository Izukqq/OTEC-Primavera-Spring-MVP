package com.otec.primavera.dto;

public record EvaluacionDTO(
        Long id,
        String nombre,
        String descripcion,
        Long cursoId,
        String cursoNombre
) {}
