package com.aseo.usuario.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.aseo.usuario.Repository.UsuarioRepository;
import com.aseo.usuario.model.Usuario;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario registrar(Usuario usuario) {
        logger.info("Intentando registrar usuario con email: {}", usuario.getEmail());

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            logger.warn("El email {} ya está en uso", usuario.getEmail());
            throw new IllegalArgumentException("El email ya está en uso");
        }

        Usuario guardado = usuarioRepository.save(usuario);
        logger.info("Usuario registrado con ID: {}", guardado.getId());
        return guardado;
    }

    public Usuario buscarPorEmail(String email) {
        logger.info("Buscando usuario por email: {}", email);
        return usuarioRepository.findByEmail(email);
    }

    public Usuario obtenerPorId(Long id) {
        logger.info("Buscando usuario por ID: {}", id);
        return usuarioRepository.findById(id).orElse(null);
    }

    public List<Usuario> listarTodos() {
        logger.info("Listando todos los usuarios");
        return usuarioRepository.findAll();
    }
}
