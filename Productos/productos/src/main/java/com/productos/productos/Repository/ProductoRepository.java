package com.productos.productos.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.productos.productos.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {}