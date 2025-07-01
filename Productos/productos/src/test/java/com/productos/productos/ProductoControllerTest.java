package com.productos.productos;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.productos.productos.Controller.ProductoController;
import com.productos.productos.Service.ProductoServicio;
import com.productos.productos.model.Producto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoServicio productoServicio;

    private Producto producto;

    @BeforeEach
    void setup() {
        producto = new Producto(1L, "desc", 1000, "Producto Test", 10, "url");
    }

    @Test
    void testListarProductos() throws Exception {
        List<Producto> productos = Arrays.asList(producto);
        when(productoServicio.listarTodos()).thenReturn(productos);

        mockMvc.perform(get("/api/productos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombre").value("Producto Test"));
    }

    @Test
    void testGuardarProductoExitosamente() throws Exception {
        Producto nuevo = new Producto(null, "desc", 1000, "Nuevo", 5, null);
        Producto guardado = new Producto(1L, "desc", 1000, "Nuevo", 5, null);
        when(productoServicio.guardarConStockInicial(any(Producto.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(nuevo)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGuardarProductoConNombreInvalido() throws Exception {
        Producto sinNombre = new Producto(null, "desc", 1000, "", 5, null);

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(sinNombre)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testObtenerProductoExistente() throws Exception {
        when(productoServicio.obtenerPorId(1L)).thenReturn(producto);

        mockMvc.perform(get("/api/productos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Producto Test"));
    }

    @Test
    void testObtenerProductoNoExistente() throws Exception {
        when(productoServicio.obtenerPorId(99L)).thenReturn(null);

        mockMvc.perform(get("/api/productos/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarStockCorrectamente() throws Exception {
        when(productoServicio.obtenerPorId(1L)).thenReturn(producto);
        when(productoServicio.guardar(any(Producto.class))).thenReturn(producto);

        mockMvc.perform(put("/api/productos/1/stock")
                .param("cantidad", "15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stock").value(10)); // el mock no cambia stock realmente
    }

    @Test
    void testActualizarStockNegativo() throws Exception {
        mockMvc.perform(put("/api/productos/1/stock")
                .param("cantidad", "-1"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testEliminarProductoExistente() throws Exception {
        when(productoServicio.eliminar(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/productos/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void testEliminarProductoNoExistente() throws Exception {
        when(productoServicio.eliminar(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/productos/99"))
            .andExpect(status().isNotFound());
    }
}
