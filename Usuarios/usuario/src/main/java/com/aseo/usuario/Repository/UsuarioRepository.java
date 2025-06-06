package com.aseo.usuario.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aseo.usuario.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
    boolean existsByEmail(String email);
}
