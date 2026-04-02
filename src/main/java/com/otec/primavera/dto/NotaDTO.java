package com.otec.primavera.dto;

import java.time.LocalDate;

public record NotaDTO(
        Long id,
        Double calificacion,
        LocalDate fecha,
        String evaluacionNombre,
        String cursoNombre,
        Long estudianteId,
        String estudianteNombre
) {}
