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

    // Cargar productos
    loadProductos();

    // Configurar modal
    const productoModal = new bootstrap.Modal(document.getElementById('productoModal'));
    const productoForm = document.getElementById('productoForm');
    const saveButton = document.getElementById('saveProducto');

    // Configurar preview de imagen
    document.getElementById('imagen').addEventListener('change', function(e) {
        const [file] = e.target.files;
        if (file) {
            // Validar tipo de archivo
            if (!file.type.startsWith('image/')) {
                alert('Por favor, selecciona un archivo de imagen válido');
                this.value = '';
                return;
            }
            
            // Validar tamaño (máximo 5MB)
            if (file.size > 5 * 1024 * 1024) {
                alert('La imagen no debe exceder los 5MB');
                this.value = '';
                return;
            }

            const previewContainer = document.getElementById('imagenPreviewContainer');
            const preview = document.getElementById('imagenPreview');
            
            preview.src = URL.createObjectURL(file);
            preview.classList.add('loading');
            preview.onload = () => preview.classList.remove('loading');
            previewContainer.style.display = 'block';
        }
    });

    // Nuevo producto
    document.querySelector('[data-bs-target="#productoModal"]').addEventListener('click', () => {
        document.getElementById('modalTitle').textContent = 'Nuevo Producto';
        productoForm.reset();
        document.getElementById('productoId').value = '';
        document.getElementById('imagenPreviewContainer').style.display = 'none';
        document.getElementById('imagen').required = true;
    });

    // Guardar producto
    saveButton.addEventListener('click', async () => {
        const nombre = document.getElementById('nombre').value.trim();
        const precio = document.getElementById('precio').value;
        const stock = document.getElementById('stock').value;
        const imagenInput = document.getElementById('imagen');
        const id = document.getElementById('productoId').value;

        // Validaciones básicas
        if (!nombre) {
            alert('El nombre del producto es obligatorio');
            return;
        }
        if (!precio || precio <= 0) {
            alert('El precio debe ser mayor que cero');
            return;
        }
        if (!id && !imagenInput.files[0]) {
            alert('La imagen es obligatoria para nuevos productos');
            return;
        }

        const formData = new FormData();
        formData.append('nombre', nombre);
        formData.append('precio', precio);
        formData.append('stock', stock);
        
        if (imagenInput.files[0]) {
            formData.append('archivo', imagenInput.files[0]); // Cambiado a 'archivo' para coincidir con el backend
        }

        try {
            saveButton.disabled = true;
            saveButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Guardando...';

            if (id) {
                // Primero actualizar datos básicos
                await API.updateProducto(id, {
                    nombre: nombre,
                    precio: precio,
                    stock: stock
                });
                
                // Luego subir imagen si hay una nueva
                if (imagenInput.files[0]) {
                    const imagenFormData = new FormData();
                    imagenFormData.append('archivo', imagenInput.files[0]);
                    await API.subirImagenProducto(id, imagenFormData);
                }
            } else {
                await API.createProducto(formData);
            }
            
            productoModal.hide();
            await loadProductos();
        } catch (error) {
            console.error('Error al guardar producto:', error);
            alert('Error al guardar el producto: ' + (error.message || 'Por favor intente nuevamente'));
        } finally {
            saveButton.disabled = false;
            saveButton.textContent = 'Guardar';
        }
    });
});

async function loadProductos() {
    try {
        const productosGrid = document.getElementById('productosGrid');
        productosGrid.innerHTML = '<div class="col-12 text-center"><div class="spinner-border text-primary" role="status"></div></div>';
        
        const productos = await API.getProductos();
        renderProductos(productos);
    } catch (error) {
        console.error('Error al cargar productos:', error);
        alert('Error al cargar productos: ' + (error.message || 'Por favor intente nuevamente'));
    }
}

function optimizarImagenCloudinary(url) {
    if (!url) return 'img/default-product.png';
    
    // Si ya es una URL de Cloudinary
    if (url.includes('res.cloudinary.com')) {
        // Añadir parámetros de optimización (ancho 400, formato webp, calidad 80)
        return url.replace(/upload\//, 'upload/w_400,f_webp,q_80/');
    }
    
    return url;
}

function renderProductos(productos) {
    const grid = document.getElementById('productosGrid');
    grid.innerHTML = '';

    if (productos.length === 0) {
        grid.innerHTML = '<div class="col-12 text-center text-muted py-4">No hay productos disponibles</div>';
        return;
    }

    productos.forEach(producto => {
        const imagenUrl = optimizarImagenCloudinary(producto.imagenUrl || producto.imagen_url);

        const card = document.createElement('div');
        card.className = 'col mb-4';
        card.innerHTML = `
            <div class="card h-100">
                <img src="${imagenUrl}" 
                     class="card-img-top loading" 
                     alt="${producto.nombre}"
                     onload="this.classList.remove('loading')"
                     onerror="this.classList.remove('loading'); this.src='img/default-product.png'">
                <div class="card-body">
                    <h5 class="card-title">${producto.nombre}</h5>
                    <p class="card-text">$${(producto.precio / 100).toFixed(2)}</p>
                    <p class="card-text"><small class="text-muted">Stock: ${producto.stock}</small></p>
                </div>
                <div class="card-footer bg-transparent">
                    <div class="d-flex justify-content-between align-items-center">
                        <button class="btn btn-outline-primary btn-sm edit-product" data-id="${producto.id}">
                            <i class="bi bi-pencil"></i> Editar
                        </button>
                        <div class="input-group" style="width: 150px;">
                            <input type="number" class="form-control form-control-sm" id="qty-${producto.id}" min="1" max="${producto.stock}" value="1">
                            <button class="btn btn-primary btn-sm add-to-cart" data-id="${producto.id}">
                                <i class="bi bi-cart-plus"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        grid.appendChild(card);
    });

    // Agregar eventos a los botones de agregar al carrito
    document.querySelectorAll('.add-to-cart').forEach(btn => {
        btn.addEventListener('click', async () => {
            const productId = btn.getAttribute('data-id');
            const quantityInput = document.getElementById(`qty-${productId}`);
            const quantity = parseInt(quantityInput.value);
            const max = parseInt(quantityInput.max);
            
            if (quantity < 1 || quantity > max) {
                alert(`La cantidad debe estar entre 1 y ${max}`);
                return;
            }

            const user = AuthService.getCurrentUser();
            
            try {
                btn.disabled = true;
                btn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span>';
                
                await API.agregarAlCarrito(user.id, productId, quantity);
                
                // Mostrar notificación de éxito
                const toast = new bootstrap.Toast(document.getElementById('liveToast'));
                document.getElementById('toastMessage').textContent = 'Producto agregado al carrito';
                toast.show();
            } catch (error) {
                console.error('Error al agregar al carrito:', error);
                alert('Error al agregar al carrito: ' + (error.message || 'Por favor intente nuevamente'));
            } finally {
                btn.disabled = false;
                btn.innerHTML = '<i class="bi bi-cart-plus"></i>';
            }
        });
    });

    // Agregar eventos a los botones de editar
    document.querySelectorAll('.edit-product').forEach(btn => {
        btn.addEventListener('click', () => {
            loadProductoForEdit(btn.getAttribute('data-id'));
        });
    });
}

async function loadProductoForEdit(id) {
    try {
        const producto = await API.getProducto(id);
        document.getElementById('modalTitle').textContent = 'Editar Producto';
        document.getElementById('productoId').value = producto.id;
        document.getElementById('nombre').value = producto.nombre;
        document.getElementById('precio').value = producto.precio;
        document.getElementById('stock').value = producto.stock;
        document.getElementById('imagen').required = false;

        const previewContainer = document.getElementById('imagenPreviewContainer');
        const preview = document.getElementById('imagenPreview');
        
        if (producto.imagenUrl) {
            preview.src = optimizarImagenCloudinary(producto.imagenUrl);
            preview.classList.add('loading');
            preview.onload = () => preview.classList.remove('loading');
            previewContainer.style.display = 'block';
        } else {
            previewContainer.style.display = 'none';
        }

        const productoModal = new bootstrap.Modal(document.getElementById('productoModal'));
        productoModal.show();
    } catch (error) {
        console.error('Error al cargar producto:', error);
        alert('Error al cargar producto: ' + (error.message || 'Por favor intente nuevamente'));
    }
}