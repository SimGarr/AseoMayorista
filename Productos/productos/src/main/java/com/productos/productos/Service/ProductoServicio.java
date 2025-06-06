package com.productos.productos.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productos.productos.Repository.ProductoRepository;
import com.productos.productos.model.Producto;

@Service
public class ProductoServicio {

    private static final Logger logger = LoggerFactory.getLogger(ProductoServicio.class);

    private final ProductoRepository repository;

    public ProductoServicio(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<Producto> listarTodos() {
        return repository.findAll();
    }

    @Transactional
    public Producto guardarConStockInicial(Producto p) {
        
        if (p.getStock() == null || p.getStock() < 0) {
            p.setStock(0);
        }
        Producto productoGuardado = repository.save(p);
        logger.info("Producto guardado con stock inicial, id={}, stock={}", productoGuardado.getId(), productoGuardado.getStock());
        return productoGuardado;
    }

    public Producto guardar(Producto p) {
        return repository.save(p);
    }

    public Producto obtenerPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Producto actualizar(Long id, Producto p) {
        if (repository.existsById(id)) {
            p.setId(id);
            return repository.save(p);
        } else {
            return null;
        }
    }

    public boolean eliminar(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public Producto actualizarStock(Long id, int nuevoStock) {
        Producto producto = obtenerPorId(id);
        if (producto != null) {
            if (nuevoStock < 0) {
                throw new IllegalArgumentException("El stock no puede ser negativo");
            }
            producto.setStock(nuevoStock);
            Producto actualizado = repository.save(producto);
            logger.info("Stock actualizado para producto id={}, stock={}", id, nuevoStock);
            return actualizado;
        } else {
            return null;
        }
    }

    @Transactional
    public Producto descontarStock(Long id, int cantidad) {
        Producto producto = obtenerPorId(id);
        if (producto == null) {
            logger.warn("Producto no encontrado para descontar stock, id={}", id);
            return null;
        }
        int stockActual = producto.getStock() != null ? producto.getStock() : 0;
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor que cero");
        }
        if (stockActual < cantidad) {
            logger.warn("Stock insuficiente para producto id={}, stock actual={}, cantidad solicitada={}", id, stockActual, cantidad);
            throw new IllegalStateException("Stock insuficiente");
        }
        producto.setStock(stockActual - cantidad);
        Producto actualizado = repository.save(producto);
        logger.info("Stock descontado para producto id={}, cantidad={}, stock restante={}", id, cantidad, producto.getStock());
        return actualizado;
    }
}
