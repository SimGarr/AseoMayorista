package com.productos.productos.Controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.productos.productos.Service.ProductoServicio;
import com.productos.productos.Service.CloudinaryService;
import com.productos.productos.model.ImagenUrlDTO;
import com.productos.productos.model.Producto;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin("*")
public class ProductoController {

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

    private final ProductoServicio servicio;
    private final CloudinaryService cloudinaryService;

    public ProductoController(ProductoServicio servicio, CloudinaryService cloudinaryService) {
        this.servicio = servicio;
        this.cloudinaryService = cloudinaryService;
    }

    @Operation(summary = "Listar todos los productos")
    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        try {
            List<Producto> productos = servicio.listarTodos();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            logger.error("Error al listar productos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Guardar un nuevo producto con stock inicial")
    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Producto p) {
        logger.info("Solicitando guardar producto: {}", p);

        if (p.getNombre() == null || p.getNombre().isBlank()) {
            logger.warn("Nombre de producto inválido");
            return ResponseEntity.badRequest().body("El nombre del producto es obligatorio");
        }
        if (p.getPrecio() == null || p.getPrecio() <= 0) {
            logger.warn("Precio de producto inválido");
            return ResponseEntity.badRequest().body("El precio debe ser mayor que cero");
        }
        if (p.getStock() != null && p.getStock() < 0) {
            logger.warn("Stock de producto inválido");
            return ResponseEntity.badRequest().body("El stock no puede ser negativo");
        }

        try {
            Producto productoGuardado = servicio.guardarConStockInicial(p);
            logger.info("Producto guardado con éxito: id={}", productoGuardado.getId());
            return ResponseEntity.ok(productoGuardado);
        } catch (Exception e) {
            logger.error("Error al guardar producto: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al guardar producto: " + e.getMessage());
        }
    }

    @Operation(summary = "Obtener un producto por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            Producto producto = servicio.obtenerPorId(id);
            if (producto != null) {
                return ResponseEntity.ok(producto);
            } else {
                logger.warn("Producto no encontrado: id={}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error al obtener producto id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al obtener producto");
        }
    }

    @Operation(summary = "Actualizar un producto por ID")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Producto p) {
        logger.info("Solicitando actualizar producto id={}", id);

        if (p.getStock() != null && p.getStock() < 0) {
            logger.warn("Stock de producto inválido");
            return ResponseEntity.badRequest().body("El stock no puede ser negativo");
        }

        try {
            Producto productoActualizado = servicio.actualizar(id, p);
            if (productoActualizado != null) {
                return ResponseEntity.ok(productoActualizado);
            } else {
                logger.warn("Producto a actualizar no encontrado: id={}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error al actualizar producto id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al actualizar producto");
        }
    }

    @Operation(summary = "Eliminar un producto por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        logger.info("Solicitando eliminar producto id={}", id);
        try {
            boolean eliminado = servicio.eliminar(id);
            if (eliminado) {
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("Producto a eliminar no encontrado: id={}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error al eliminar producto id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al eliminar producto");
        }
    }

    @Operation(summary = "Obtener stock de un producto por ID")
    @GetMapping("/{id}/stock")
    public ResponseEntity<?> obtenerStock(@PathVariable Long id) {
        try {
            Producto producto = servicio.obtenerPorId(id);
            if (producto != null) {
                return ResponseEntity.ok(producto.getStock());
            } else {
                logger.warn("Producto no encontrado para obtener stock, id={}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error al obtener stock producto id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body("Error al obtener stock");
        }
    }

    @Operation(summary = "Actualizar stock de un producto por ID")
    @PutMapping("/{id}/stock")
    public ResponseEntity<?> actualizarStock(@PathVariable Long id, @RequestParam int cantidad) {
        if (cantidad < 0) {
            return ResponseEntity.badRequest().body("El stock no puede ser negativo");
        }
        try {
            Producto producto = servicio.obtenerPorId(id);
            if (producto == null) {
                logger.warn("Producto no encontrado para actualizar stock, id={}", id);
                return ResponseEntity.notFound().build();
            }
            producto.setStock(cantidad);
            servicio.guardar(producto);
            logger.info("Stock actualizado para producto id={}, nuevo stock={}", id, cantidad);
            return ResponseEntity.ok(producto);
        } catch (Exception e) {
            logger.error("Error al actualizar stock producto id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body("Error al actualizar stock");
        }
    }

}
