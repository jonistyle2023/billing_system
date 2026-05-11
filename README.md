# Sistema de Facturación

Este proyecto, originalmente nombrado "Calculación", es un **Sistema de Facturación** desarrollado con tecnologías modernas de Java de escritorio. Está diseñado para administrar clientes, gestionar el inventario de productos y emitir facturas mediante una interfaz gráfica limpia y estructurada.

## 🚀 Características Principales

*   **Autenticación de Usuarios**: Pantalla de login para restringir el acceso al sistema.
*   **Administración de Clientes**: Creación, validación, actualización y búsqueda de clientes. (Soporte para ventanas modales).
*   **Gestión de Productos**: Control del catálogo de productos y precios.
*   **Módulo de Facturación**: Interfaz dedicada para agregar productos al carrito, calcular subtotales, IVA y el total final de manera automática. Incluye **autocompletado de datos del cliente** en tiempo real al ingresar su cédula.
*   **Soporte Multiidioma (i18n)**: El sistema cuenta con internacionalización completa usando `ResourceBundle`. Actualmente permite cambiar dinámicamente entre **Español** e **Inglés** directamente desde la barra de estado de la ventana principal.
*   **Diseño Visual (CSS)**: Toda la interfaz gráfica se encuentra estilizada a través de una hoja de estilos externa, proveyendo a los botones, tablas y formularios de un diseño moderno y minimalista en una paleta de colores corporativa.

## 🛠️ Tecnologías y Herramientas Utilizadas

*   **Java (JDK 11+)**: Lenguaje de programación principal del backend y la lógica de controladores.
*   **JavaFX**: Framework utilizado para el diseño y construcción de toda la Interfaz Gráfica de Usuario (GUI).
*   **FXML**: Lenguaje declarativo basado en XML empleado para estructurar visualmente las vistas, separando el diseño del código Java.
*   **CSS (JavaFX CSS)**: Estilos en cascada aplicados a nodos JavaFX para un diseño responsivo, estético y moderno.
*   **Maven / Módulos de Java (Jigsaw)**: Arquitectura modular implementada a través de `module-info.java` para exponer correctamente las vistas y modelos.
*   **Java ResourceBundle (.properties)**: Manejo nativo de internacionalización (i18n) para soportar la función multiidioma sin librerías de terceros.

## 🏗️ Diseño Arquitectónico

El proyecto sigue un patrón arquitectónico similar al **MVC (Modelo-Vista-Controlador)**:

1.  **Vistas (`src/main/resources/upse/calculacion/vistas`)**: Archivos FXML (`Principal.fxml`, `Factura.fxml`, etc.) que definen las ventanas, botones, tablas y formularios, respaldados globalmente por `styles.css`.
2.  **Controladores (`src/main/java/upse/calculacion/controlador`)**: Clases Java (ej. `PrincipalController.java`, `FacturaController.java`) que manejan los eventos del usuario y controlan el flujo de datos.
3.  **Modelo (`src/main/java/upse/calculacion/modelo`)**: Clases de dominio (ej. `Cliente.java`, `CabeceraFactura.java`, `DetFactura.java`) encargadas de estructurar y retener los datos dentro del sistema.
4.  **Recursos / i18n (`src/main/resources/upse/calculacion/idiomas`)**: Archivos `mensajes_es.properties` y `mensajes_en.properties` encargados de alimentar la interfaz gráfica según la localización escogida por el usuario.

## 🌐 Internacionalización (Multiidioma)

Una de las funciones más destacables del proyecto es su soporte multiidioma. Al iniciar la aplicación, todos los textos de la interfaz son proveídos por el archivo de propiedades en español (`mensajes_es.properties`). 
En la parte inferior de la ventana principal, los usuarios encontrarán un `ComboBox` que les permite seleccionar **"English"**. Al seleccionarlo, la aplicación recarga la vista instantáneamente consultando el archivo `mensajes_en.properties`, sin necesidad de reiniciar el programa.

## ⚙️ Cómo Ejecutar

1. Clona el repositorio.
2. Asegúrate de tener configurado tu IDE con soporte para **JavaFX** (agregando los módulos de `javafx.controls` y `javafx.fxml`).
3. Ejecuta la clase principal ubicada en `upse.calculacion.controlador.App`.
4. Ingresa usando las credenciales predeterminadas:
   *   **Usuario:** `admin`
   *   **Contraseña:** `123`

---
*Desarrollado para propósitos académicos y de gestión empresarial básica.*