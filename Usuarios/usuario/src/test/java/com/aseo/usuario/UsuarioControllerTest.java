package com.aseo.usuario;


import com.aseo.usuario.Controller.UsuarioController;
import com.aseo.usuario.Service.UsuarioService;
import com.aseo.usuario.model.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setEmail("juan@mail.com");
        usuario.setContrasena("1234");
    }

    @Test
    void testRegistrarUsuarioExitoso() throws Exception {
        when(usuarioService.registrar(any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(usuario)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void testRegistrarUsuarioConEmailDuplicado() throws Exception {
        when(usuarioService.registrar(any(Usuario.class)))
            .thenThrow(new IllegalArgumentException("El email ya está en uso"));

        mockMvc.perform(post("/api/usuarios/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(usuario)))
            .andExpect(status().isConflict())
            .andExpect(content().string("El email ya está en uso"));
    }

    @Test
    void testLoginUsuarioExistente() throws Exception {
        when(usuarioService.buscarPorEmail("juan@mail.com")).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(usuario)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("juan@mail.com"));
    }

    @Test
    void testLoginUsuarioNoExistente() throws Exception {
        when(usuarioService.buscarPorEmail("juan@mail.com")).thenReturn(null);

        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(usuario)))
            .andExpect(status().isNotFound());
    }

    @Test
    void testObtenerUsuarioPorIdExistente() throws Exception {
        when(usuarioService.obtenerPorId(1L)).thenReturn(usuario);

        mockMvc.perform(get("/api/usuarios/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void testObtenerUsuarioPorIdNoExistente() throws Exception {
        when(usuarioService.obtenerPorId(2L)).thenReturn(null);

        mockMvc.perform(get("/api/usuarios/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testListarUsuarios() throws Exception {
        when(usuarioService.listarTodos()).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].email").value("juan@mail.com"));
    }
}
