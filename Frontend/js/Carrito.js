document.addEventListener('DOMContentLoaded', () => {
    // Verificar autenticación
    if (!AuthService.isAuthenticated()) {
        window.location.href = 'index.html';
        return;
    }

    // Mostrar email del usuario
    const user = AuthService.getCurrentUser();
    document.getElementById('userEmail').textContent = user.email;

    // Manejar logout
    document.getElementById('logoutBtn').addEventListener('click', () => {
        AuthService.logout();
        window.location.href = 'index.html';
    });

    // Cargar productos y carrito
    loadProductos();
    loadCarrito();

    // Configurar modal de Webpay
    const webpayModal = new bootstrap.Modal(document.getElementById('webpayModal'));

    // Proceso de pago con Webpay
    document.getElementById('checkoutBtn').addEventListener('click', async () => {
        try {
            const user = AuthService.getCurrentUser();
            
            // Mostrar modal de carga
            webpayModal.show();
            
            // Iniciar pago con Webpay
            const response = await API.iniciarPagoWebpay(user.id);
            
            // Redirigir a Webpay
            window.location.href = response.data.url;;
        } catch (error) {
            webpayModal.hide();
            alert('Error al iniciar el pago: ' + error.message);
        }
    });
});

async function loadProductos() {
    try {
        const productos = await API.getProductos();
        renderProductos(productos);
    } catch (error) {
        alert('Error al cargar productos: ' + error.message);
    }
}

function renderProductos(productos) {
    const grid = document.getElementById('productosGrid');
    grid.innerHTML = '';

    productos.forEach(producto => {
        const card = document.createElement('div');
        card.className = 'col';
        card.innerHTML = `
            <div class="card h-100">
                <div class="card-body">
                    <h5 class="card-title">${producto.nombre}</h5>
                    <p class="card-text">$${producto.precio.toFixed(2)}</p>
                    <p class="card-text"><small class="text-muted">Stock: ${producto.stock}</small></p>
                </div>
                <div class="card-footer bg-transparent">
                    <div class="input-group">
                        <input type="number" class="form-control" id="qty-${producto.id}" min="1" max="${producto.stock}" value="1">
                        <button class="btn btn-primary add-to-cart" data-id="${producto.id}">
                            <i class="bi bi-cart-plus"></i> Agregar
                        </button>
                    </div>
                </div>
            </div>
        `;
        grid.appendChild(card);
    });

    // Agregar eventos a los botones
    document.querySelectorAll('.add-to-cart').forEach(btn => {
        btn.addEventListener('click', async () => {
            const productId = btn.getAttribute('data-id');
            const quantity = parseInt(document.getElementById(`qty-${productId}`).value);
            
            try {
                const user = AuthService.getCurrentUser();
                await API.agregarAlCarrito(user.id, productId, quantity);
                await loadCarrito();
                
                // Mostrar feedback
                const toast = document.createElement('div');
                toast.className = 'toast show position-fixed bottom-0 end-0 m-3';
                toast.innerHTML = `
                    <div class="toast-header bg-success text-white">
                        <strong class="me-auto">Carrito</strong>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast"></button>
                    </div>
                    <div class="toast-body">
                        Producto agregado al carrito
                    </div>
                `;
                document.body.appendChild(toast);
                
                setTimeout(() => toast.remove(), 3000);
            } catch (error) {
                alert('Error al agregar al carrito: ' + error.message);
            }
        });
    });
}

async function loadCarrito() {
    try {
        const user = AuthService.getCurrentUser();
        const carrito = await API.obtenerCarrito(user.id);
        renderCarrito(carrito);
    } catch (error) {
        alert('Error al cargar carrito: ' + error.message);
    }
}

function renderCarrito(carrito) {
    const cartItems = document.getElementById('cartItems');
    const cartCount = document.getElementById('cartCount');
    const cartTotal = document.getElementById('cartTotal');
    
    if (carrito.length === 0) {
        cartItems.innerHTML = '<p class="text-muted">Tu carrito está vacío</p>';
        cartCount.textContent = '0';
        cartTotal.textContent = '$0.00';
        document.getElementById('checkoutBtn').disabled = true;
        return;
    }

    let total = 0;
    let count = 0;
    cartItems.innerHTML = '';
    
    carrito.forEach(item => {
        const subtotal = item.producto.precio * item.cantidad;
        total += subtotal;
        count += item.cantidad;
        
        const itemElement = document.createElement('div');
        itemElement.className = 'd-flex justify-content-between mb-2 border-bottom pb-2';
        itemElement.innerHTML = `
            <div>
                <h6 class="mb-0">${item.producto.nombre}</h6>
                <small class="text-muted">${item.cantidad} x $${item.producto.precio.toFixed(2)}</small>
            </div>
            <div class="d-flex align-items-center">
                <span class="fw-bold">$${subtotal.toFixed(2)}</span>
                <button class="btn btn-sm btn-outline-danger ms-2 remove-item" data-id="${item.id}">
                    <i class="bi bi-trash"></i>
                </button>
            </div>
        `;
        cartItems.appendChild(itemElement);
    });

    // Actualizar totales
    cartCount.textContent = count;
    cartTotal.textContent = `$${total.toFixed(2)}`;
    document.getElementById('checkoutBtn').disabled = false;

    // Agregar eventos a los botones de eliminar
    document.querySelectorAll('.remove-item').forEach(btn => {
        btn.addEventListener('click', async () => {
            const itemId = btn.getAttribute('data-id');
            if (confirm('¿Eliminar este producto del carrito?')) {
                try {
                    await API.eliminarDelCarrito(itemId);
                    await loadCarrito();
                } catch (error) {
                    alert('Error al eliminar del carrito: ' + error.message);
                }
            }
        });
    });
}