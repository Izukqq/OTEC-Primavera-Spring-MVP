package com.otec.primavera.controller;

import com.otec.primavera.model.Estudiante;
import com.otec.primavera.service.EstudianteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
public class ProgresoController {

    @Autowired
    private EstudianteService estudianteService;

    @GetMapping("/mi-progreso")
    public String miProgreso(Principal principal, Model model) {
        String username = principal.getName();
        Optional<Estudiante> estudiante = estudianteService.buscarPorEmail(username);

        if (estudiante.isPresent()) {
            Estudiante est = estudiante.get();
            model.addAttribute("estudiante", est);
            if (est.getCurso() != null) {
                model.addAttribute("notas",
                        estudianteService.listarNotasPorCurso(est.getId(), est.getCurso().getId()));
            } else {
                model.addAttribute("notas", estudianteService.listarNotas(est.getId()));
            }
        } else {
            model.addAttribute("mensaje", "No se encontró un perfil de estudiante asociado al usuario: " + username);
        }

        return "estudiantes/mi-progreso";
    }
}
