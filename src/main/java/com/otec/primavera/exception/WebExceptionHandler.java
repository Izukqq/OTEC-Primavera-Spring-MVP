package com.otec.primavera.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(basePackages = "com.otec.primavera.controller")
public class WebExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrity(DataIntegrityViolationException ex,
                                      RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("msgError",
                "Error de integridad de datos. Verifique que no existan registros duplicados.");
        return "redirect:/cursos";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntime(RuntimeException ex,
                                RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("msgError", ex.getMessage());
        return "redirect:/cursos";
    }
}
