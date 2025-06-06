class AuthService {
    static isAuthenticated() {
        const user = localStorage.getItem('user');
        return user !== null && user !== undefined;
    }

    static getCurrentUser() {
        const user = localStorage.getItem('user');
        return user ? JSON.parse(user) : null;
    }

    static async login(email, password) {
        // Validación básica en frontend
        if (!email || !password) {
            throw new Error('Por favor ingresa email y contraseña');
        }

        try {
            const response = await fetch('http://localhost:8081/api/usuarios/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Credenciales incorrectas');
            }

            const user = await response.json();
            
            // Validar que el usuario tenga los campos mínimos requeridos
            if (!user || !user.id || !user.email) {
                throw new Error('Datos de usuario inválidos recibidos del servidor');
            }

            localStorage.setItem('user', JSON.stringify(user));
            return user;
            
        } catch (error) {
            console.error('Error en AuthService.login:', error);
            throw error; // Re-lanzamos el error para que lo capture el llamador
        }
    }

    static async register(name, email, password) {
        // Validación básica en frontend
        if (!name || !email || !password) {
            throw new Error('Por favor completa todos los campos');
        }

        try {
            const response = await fetch('http://localhost:8081/api/usuarios/registro', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ 
                    nombre: name, 
                    email, 
                    password 
                })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Error en el registro');
            }

            return await response.json();
            
        } catch (error) {
            console.error('Error en AuthService.register:', error);
            throw error;
        }
    }

    static logout() {
        localStorage.removeItem('user');
        // Opcional: Redirigir a la página de login
        // window.location.href = 'index.html';
    }

    static checkAuth() {
        if (!this.isAuthenticated() && !window.location.pathname.endsWith('index.html')) {
            window.location.href = 'index.html';
        }
    }
}

// Event Listeners para formularios (si están presentes en la página)
document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    // Manejar login
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('loginEmail').value.trim();
            const password = document.getElementById('loginPassword').value;

            try {
                // Mostrar loading
                const submitBtn = loginForm.querySelector('button[type="submit"]');
                const originalText = submitBtn.textContent;
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Validando...';

                await AuthService.login(email, password);
                window.location.href = 'tienda.html';
                
            } catch (error) {
                // Mostrar error de forma más amigable
                const errorElement = document.getElementById('loginError') || document.createElement('div');
                errorElement.id = 'loginError';
                errorElement.className = 'alert alert-danger mt-3';
                errorElement.textContent = error.message;
                
                if (!document.getElementById('loginError')) {
                    loginForm.appendChild(errorElement);
                }
                
                // Restaurar botón
                const submitBtn = loginForm.querySelector('button[type="submit"]');
                submitBtn.disabled = false;
                submitBtn.textContent = originalText;
            }
        });
    }

    // Manejar registro
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('registerName').value.trim();
            const email = document.getElementById('registerEmail').value.trim();
            const password = document.getElementById('registerPassword').value;

            try {
                // Mostrar loading
                const submitBtn = registerForm.querySelector('button[type="submit"]');
                const originalText = submitBtn.textContent;
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Registrando...';

                await AuthService.register(name, email, password);
                
                // Limpiar formulario y mostrar éxito
                registerForm.reset();
                const successElement = document.createElement('div');
                successElement.className = 'alert alert-success mt-3';
                successElement.textContent = '¡Registro exitoso! Por favor inicia sesión.';
                registerForm.appendChild(successElement);
                
                // Cambiar a pestaña de login
                document.getElementById('login-tab').click();
                
            } catch (error) {
                // Mostrar error
                const errorElement = document.getElementById('registerError') || document.createElement('div');
                errorElement.id = 'registerError';
                errorElement.className = 'alert alert-danger mt-3';
                errorElement.textContent = error.message;
                
                if (!document.getElementById('registerError')) {
                    registerForm.appendChild(errorElement);
                }
                
            } finally {
                // Restaurar botón
                const submitBtn = registerForm.querySelector('button[type="submit"]');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = originalText;
                }
            }
        });
    }

    // Verificar autenticación al cargar la página
    if (AuthService.isAuthenticated() && window.location.pathname.endsWith('index.html')) {
        window.location.href = 'tienda.html';
    } else if (!AuthService.isAuthenticated() && !window.location.pathname.endsWith('index.html')) {
        window.location.href = 'index.html';
    }
});

// Exportar para usar en otros archivos si es necesario
if (typeof module !== 'undefined' && module.exports) {
    module.exports = AuthService;
}