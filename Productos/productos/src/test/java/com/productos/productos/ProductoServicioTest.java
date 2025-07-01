package com.productos.productos;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import com.productos.productos.Repository.ProductoRepository;
import com.productos.productos.Service.ProductoServicio;
import com.productos.productos.model.Producto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ProductoServicioTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServicio productoServicio;

    private Producto productoEjemplo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productoEjemplo = new Producto(1L, "Descripción", 1000, "Producto 1", 10, "urlImagen");
    }

    @Test
    void testGuardarConStockInicialCuandoStockEsNulo() {
        Producto productoSinStock = new Producto(null, "Desc", 500, "Prod", null, null);

        when(productoRepository.save(any(Producto.class)))
            .thenAnswer(invoc -> {
                Producto p = invoc.getArgument(0);
                p.setId(1L);
                return p;
            });

        Producto resultado = productoServicio.guardarConStockInicial(productoSinStock);

        assertNotNull(resultado.getId());
        assertEquals(0, resultado.getStock());
    }

    @Test
    void testObtenerPorIdExistente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));

        Producto resultado = productoServicio.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals("Producto 1", resultado.getNombre());
    }

    @Test
    void testObtenerPorIdNoExistente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        Producto resultado = productoServicio.obtenerPorId(1L);

        assertNull(resultado);
    }

    @Test
    void testDescontarStockCorrectamente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoEjemplo);

        Producto resultado = productoServicio.descontarStock(1L, 5);

        assertEquals(5, resultado.getStock());
    }

    @Test
    void testDescontarStockInsuficiente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            productoServicio.descontarStock(1L, 20);
        });

        assertEquals("Stock insuficiente", exception.getMessage());
    }

    @Test
    void testActualizarStockCorrectamente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoEjemplo);

        Producto actualizado = productoServicio.actualizarStock(1L, 15);

        assertEquals(15, actualizado.getStock());
    }

    @Test
    void testActualizarStockNegativo() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));

        assertThrows(IllegalArgumentException.class, () -> {
            productoServicio.actualizarStock(1L, -10);
        });
    }

    @Test
    void testListarTodos() {
        List<Producto> productos = Arrays.asList(
            new Producto(1L, "Desc 1", 100, "Producto A", 5, null),
            new Producto(2L, "Desc 2", 200, "Producto B", 10, null)
        );
        when(productoRepository.findAll()).thenReturn(productos);

        List<Producto> resultado = productoServicio.listarTodos();

        assertEquals(2, resultado.size());
    }
}
