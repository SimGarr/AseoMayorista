SISTEMA DE CARRITO DE COMPRAS - MICROSERVICIOS + FRONTEND
--------------------------------------------------------

[ARQUITECTURA]

* Microservicios (Backend):
----------------------------
| Servicio   | Puerto | Descripción                      |
|------------|--------|----------------------------------|
| usuarios   | 8081   | Gestión de usuarios              |
| productos  | 8082   | Catálogo de productos            |
| carrito    | 8080   | Carritos, pagos y notificaciones |

Tecnologías backend:
- Spring Boot 2.7+
- MySQL 8.0+
- RestTemplate para comunicación
- Webpay (Transbank) para pagos
- MailJet para emails

* Frontend:
------------
- HTML5 + JavaScript
- Páginas:
  * index.html (Inicio/login)
  * tienda.html (Catálogo)
  * pagoexitoso.html
  * pagorechazado.html

[INSTALACIÓN Y CONFIGURACIÓN]

1. PRERREQUISITOS:
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- VSCode con extensión Live Server

2. CONFIGURACIÓN BD:
--------------------
a) Crear bases de datos:
   CREATE DATABASE usuarios;
   CREATE DATABASE productos;
   CREATE DATABASE carrito;

b) Ejecutar script productos.sql para datos de prueba

3. CONFIGURAR CREDENCIALES:
--------------------------
Editar en cada microservicio (src/main/resources/application.properties):

spring.datasource.url=jdbc:mysql://localhost:3306/nombre_bd
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update

4. CONFIGURAR WEBPAY:
--------------------
En CarritoController.java actualizar:
response.sendRedirect("http://127.0.0.1:5500/pagoexitoso.html");
(response.sendRedirect("http://127.0.0.1:5500/pagorechazado.html");

Con la ip dinámica entregada por liveserver

[EJECUCIÓN]

1. Iniciar microservicios (en orden):
   cd usuarios && mvn spring-boot:run
   cd productos && mvn spring-boot:run
   cd carrito && mvn spring-boot:run

2. Iniciar frontend:
   - Abrir VSCode en carpeta frontend
   - Con extensión Live Server instalada
   - Click derecho en index.html -> "Open with Live Server"

[PRUEBAS]

1. Acceder a http://127.0.0.1:5500/index.html
2. Registrar usuario (usar email real para MailJet)
3. Agregar productos al carrito
4. Probar flujo de pago con Webpay
usar tarjetas de prueba:

4051 8856 0044 6623
CVV 123
cualquier fecha de expiración

5. Verificar redirección y emails

[SOLUCIÓN DE PROBLEMAS]

* Error de redirección post-pago:
  - Verificar puerto de Live Server
  - Actualizar URLs en CarritoController

* Error conexión MySQL:
  - Verificar credenciales en application.properties
  - Confirmar que MySQL está corriendo

* Problemas CORS:
  - Asegurarse de usar Live Server
  - No abrir HTML directamente en navegador

[NOTAS IMPORTANTES]

- Siempre usar Live Server para frontend
- Script productos.sql incluye 50 productos de aseo
- Configurar credenciales MailJet en carrito/application.properties
- Para Webpay usar tarjetas de prueba
