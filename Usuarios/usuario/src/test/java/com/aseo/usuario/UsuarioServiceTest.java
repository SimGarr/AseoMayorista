package com.aseo.usuario;



import com.aseo.usuario.Repository.UsuarioRepository;
import com.aseo.usuario.Service.UsuarioService;
import com.aseo.usuario.model.Usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setEmail("juan@mail.com");
        usuario.setContrasena("1234");
    }

    @Test
    void testRegistrarUsuarioExitoso() {
        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(false);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario registrado = usuarioService.registrar(usuario);

        assertNotNull(registrado);
        assertEquals("Juan", registrado.getNombre());
    }

    @Test
    void testRegistrarUsuarioConEmailDuplicado() {
        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrar(usuario));
    }

    @Test
    void testBuscarPorEmail() {
        when(usuarioRepository.findByEmail("juan@mail.com")).thenReturn(usuario);

        Usuario encontrado = usuarioService.buscarPorEmail("juan@mail.com");

        assertNotNull(encontrado);
        assertEquals("Juan", encontrado.getNombre());
    }

    @Test
    void testObtenerPorIdExistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario encontrado = usuarioService.obtenerPorId(1L);

        assertNotNull(encontrado);
    }

    @Test
    void testObtenerPorIdNoExistente() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        Usuario encontrado = usuarioService.obtenerPorId(2L);

        assertNull(encontrado);
    }

    @Test
    void testListarTodos() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<Usuario> lista = usuarioService.listarTodos();

        assertEquals(1, lista.size());
    }
}
