class API {
    static BASE_URL = 'http://localhost:8082/api/productos';
    static USUARIOS_URL = 'http://localhost:8081/api/usuarios';
    static CARRITO_URL = 'http://localhost:8080/api/carrito';

    static async getProductos() {
        const response = await fetch(this.BASE_URL);
        if (!response.ok) throw new Error('Error al obtener productos');
        return await response.json();
    }

    static async getProducto(id) {
        const response = await fetch(`${this.BASE_URL}/${id}`);
        if (!response.ok) throw new Error('Error al obtener producto');
        return await response.json();
    }

    static async createProducto(productoData) {
        const response = await fetch(this.BASE_URL, {
            method: 'POST',
            body: productoData // FormData no necesita headers
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Error al crear producto');
        }
        return await response.json();
    }

    static async updateProducto(id, productoData) {
        const response = await fetch(`${this.BASE_URL}/${id}`, {
            method: 'PUT',
            body: productoData
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Error al actualizar producto');
        }
        return await response.json();
    }

    static async agregarAlCarrito(usuarioId, productoId, cantidad) {
        const response = await fetch(`${this.CARRITO_URL}/items/agregar`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                usuarioId,
                productoId,
                cantidad
            })
        });
        if (!response.ok) throw new Error('Error al agregar al carrito');
        return await response.json();
    }

    static async obtenerCarrito(usuarioId) {
        const response = await fetch(`${this.CARRITO_URL}/items/usuario/${usuarioId}`);
        if (!response.ok) throw new Error('Error al obtener carrito');
        
        const items = await response.json();
        
        const itemsConProductos = await Promise.all(items.map(async item => {
            try {
                const producto = await this.getProducto(item.productoId);
                return {
                    ...item,
                    producto: {
                        ...producto,
                        imagenUrl: optimizarImagenCloudinary(producto.imagenUrl)
                    }
                };
            } catch (error) {
                console.error('Error al obtener producto:', error);
                return null;
            }
        }));
        
        return itemsConProductos.filter(item => item !== null);
    }

    static async eliminarDelCarrito(itemId) {
        const response = await fetch(`${this.CARRITO_URL}/items/eliminar/${itemId}`, {
            method: 'DELETE'
        });
        if (!response.ok) throw new Error('Error al eliminar del carrito');
    }

    static async iniciarPagoWebpay(usuarioId) {
        const response = await fetch(`${this.CARRITO_URL}/pagar`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ usuarioId })
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Error al iniciar pago');
        }
        
        return await response.json();
    }
}

function optimizarImagenCloudinary(url) {
    if (!url || !url.includes('res.cloudinary.com')) return url || 'img/default-product.png';
    return url.replace('/upload/', '/upload/w_500,h_500,c_fill,q_auto,f_auto/');
}