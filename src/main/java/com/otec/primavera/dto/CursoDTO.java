package com.otec.primavera.dto;

import java.time.LocalDate;

public record CursoDTO(
        Long id,
        String nombre,
        String descripcion,
        LocalDate fechaInicio
) {}
