package com.otec.primavera.controller;

import com.otec.primavera.model.Estudiante;
import com.otec.primavera.service.EstudianteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
public class RegistroController {

    @Autowired
    private EstudianteService estudianteService;

    @GetMapping("/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("estudiante", new Estudiante());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid Estudiante estudiante, BindingResult result,
                            RedirectAttributes redirectAttributes, Model model) {
        if (estudiante.getPassword() == null || estudiante.getPassword().isBlank()) {
            result.rejectValue("password", "NotBlank", "La contraseña es obligatoria.");
        }

        if (result.hasErrors()) {
            return "registro";
        }

        try {
            estudianteService.registrarEstudiante(estudiante);
            redirectAttributes.addFlashAttribute("msgExito", "Registro exitoso. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("msgError", e.getMessage());
            return "registro";
        }
    }
}
