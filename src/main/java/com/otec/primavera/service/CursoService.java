package com.otec.primavera.service;

import com.otec.primavera.model.Curso;
import com.otec.primavera.repository.CursoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("null")
public class CursoService {

    @Autowired
    private CursoRepository cursoRepository;

    @Transactional(readOnly = true)
    public List<Curso> listarTodos() {
        return cursoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Curso buscarPorId(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado con ID: " + id));
    }

    @Transactional
    public Curso guardar(Curso curso) {
        Curso guardado = cursoRepository.save(curso);
        return guardado;
    }

    @Transactional
    public void eliminar(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede eliminar. Curso no encontrado con ID: " + id));
        cursoRepository.delete(curso);
    }
}
