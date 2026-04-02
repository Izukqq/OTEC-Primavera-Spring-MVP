package com.otec.primavera.controller;

import com.otec.primavera.model.Curso;
import com.otec.primavera.service.CursoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/cursos")
public class CursoController {

    @Autowired
    private CursoService cursoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("cursos", cursoService.listarTodos());
        return "cursos/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("curso", new Curso());
        return "cursos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Curso curso, BindingResult result,
                          RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            return "cursos/formulario";
        }
        try {
            cursoService.guardar(curso);
            redirectAttributes.addFlashAttribute("msgExito", "Curso guardado exitosamente.");
        } catch (Exception e) {
            model.addAttribute("msgError", "Error al guardar el curso: " + e.getMessage());
            return "cursos/formulario";
        }
        return "redirect:/cursos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            cursoService.eliminar(id);
            redirectAttributes.addFlashAttribute("msgExito", "Curso eliminado exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msgError", e.getMessage());
        }
        return "redirect:/cursos";
    }
}
