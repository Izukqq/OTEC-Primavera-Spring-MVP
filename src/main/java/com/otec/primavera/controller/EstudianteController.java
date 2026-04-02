package com.otec.primavera.controller;

import com.otec.primavera.model.Estudiante;
import com.otec.primavera.service.CursoService;
import com.otec.primavera.service.EstudianteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/estudiantes")
public class EstudianteController {

    @Autowired
    private EstudianteService estudianteService;

    @Autowired
    private CursoService cursoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("estudiantes", estudianteService.listarSoloEstudiantes());
        model.addAttribute("cursos", cursoService.listarTodos());
        return "estudiantes/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("estudiante", new Estudiante());
        model.addAttribute("cursos", cursoService.listarTodos());
        return "estudiantes/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Estudiante estudiante, BindingResult result,
                          RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("cursos", cursoService.listarTodos());
            return "estudiantes/formulario";
        }
        estudianteService.guardar(estudiante);
        redirectAttributes.addFlashAttribute("msgExito", "Estudiante registrado exitosamente.");
        return "redirect:/estudiantes";
    }

    @PostMapping("/matricular")
    public String matricular(@RequestParam Long estudianteId,
                             @RequestParam Long cursoId,
                             RedirectAttributes redirectAttributes) {
        try {
            estudianteService.matricularEnCurso(estudianteId, cursoId);
            redirectAttributes.addFlashAttribute("msgExito", "Estudiante matriculado en el curso exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msgError", e.getMessage());
        }
        return "redirect:/estudiantes";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            estudianteService.eliminar(id);
            redirectAttributes.addFlashAttribute("msgExito", "Estudiante eliminado exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msgError", e.getMessage());
        }
        return "redirect:/estudiantes";
    }
}
