package com.otec.primavera.controller;

import com.otec.primavera.model.Evaluacion;
import com.otec.primavera.model.Nota;
import com.otec.primavera.service.CursoService;
import com.otec.primavera.service.EstudianteService;
import com.otec.primavera.service.EvaluacionService;

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
@RequestMapping("/evaluaciones")
public class EvaluacionController {

    @Autowired
    private EvaluacionService evaluacionService;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private EstudianteService estudianteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("evaluaciones", evaluacionService.listarTodas());
        return "evaluaciones/lista";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioEvaluacion(Model model) {
        model.addAttribute("evaluacion", new Evaluacion());
        model.addAttribute("cursos", cursoService.listarTodos());
        return "evaluaciones/formulario";
    }

    @PostMapping("/guardar")
    public String guardarEvaluacion(@RequestParam Long cursoId,
                                    @Valid Evaluacion evaluacion, BindingResult result,
                                    RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("cursos", cursoService.listarTodos());
            return "evaluaciones/formulario";
        }
        try {
            evaluacionService.guardar(cursoId, evaluacion);
            redirectAttributes.addFlashAttribute("msgExito", "Evaluación creada exitosamente.");
        } catch (Exception e) {
            model.addAttribute("cursos", cursoService.listarTodos());
            model.addAttribute("msgError", e.getMessage());
            return "evaluaciones/formulario";
        }
        return "redirect:/evaluaciones";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            evaluacionService.eliminar(id);
            redirectAttributes.addFlashAttribute("msgExito", "Evaluación eliminada exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msgError", e.getMessage());
        }
        return "redirect:/evaluaciones";
    }

    @GetMapping("/notas/nueva")
    public String mostrarFormularioNota(Model model) {
        model.addAttribute("nota", new Nota());
        model.addAttribute("estudiantes", estudianteService.listarSoloEstudiantes());
        model.addAttribute("evaluaciones", evaluacionService.listarTodas());
        return "evaluaciones/nota-formulario";
    }

    @PostMapping("/notas/guardar")
    public String guardarNota(@RequestParam Long estudianteId,
                              @RequestParam Long evaluacionId,
                              @Valid Nota nota, BindingResult result,
                              RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("estudiantes", estudianteService.listarSoloEstudiantes());
            model.addAttribute("evaluaciones", evaluacionService.listarTodas());
            return "evaluaciones/nota-formulario";
        }
        try {
            estudianteService.agregarNota(estudianteId, evaluacionId, nota);
            redirectAttributes.addFlashAttribute("msgExito", "Nota registrada exitosamente.");
        } catch (Exception e) {
            model.addAttribute("estudiantes", estudianteService.listarSoloEstudiantes());
            model.addAttribute("evaluaciones", evaluacionService.listarTodas());
            model.addAttribute("msgError", e.getMessage());
            return "evaluaciones/nota-formulario";
        }
        return "redirect:/evaluaciones/notas/nueva";
    }
}
