# Sistema de Facturación

Este proyecto es un **Sistema de Facturación** de escritorio desarrollado en JavaFX. Administra clientes y productos, emite facturas con cálculo automático de IVA, genera reportes (PDF/Excel) y transmite los comprobantes al **SRI (Ecuador)** como facturación electrónica.

> Si buscas el manual de uso para el personal que opera el sistema día a día (cajeros, administración), o el checklist de información que hay que pedirle al cliente antes de entregar la aplicación, ve a **[MANUAL_USUARIO.md](MANUAL_USUARIO.md)**. Este README es documentación técnica para quien desarrolla o despliega el proyecto.

## Características Principales

* **Autenticación de usuarios** con contraseñas hasheadas (PBKDF2WithHmacSHA256).
* **Administración de Clientes**: alta, edición, búsqueda y baja lógica.
* **Gestión de Productos**: catálogo con precio de compra, PVP menor/mayor, stock, IVA por producto e imagen.
* **Módulo de Facturación**: carrito de productos con autocompletado de cliente por cédula, cálculo automático de subtotal/IVA/total, métodos de pago (efectivo con cálculo de cambio, tarjeta, transferencia), validación de stock e impresión/exportación del RIDE en PDF.
* **Facturación electrónica SRI**: firma XAdES-BES y envío en tiempo real al SRI con contingencia (ver sección dedicada más abajo).
* **Reportes** (JasperReports): listado de facturas, resumen de ventas, ranking de productos, listado de clientes y de productos — todos exportables a PDF y Excel.
* **Multiidioma (i18n)**: Español/Inglés en caliente, sin reiniciar la app.
* **Empaquetado**: jar ejecutable con dependencias e instalador `.msi` para Windows (ver sección de empaquetado).

## Tecnologías y Herramientas

* **Java 23** (`maven.compiler.release = 23` en `pom.xml`).
* **JavaFX 23** (`javafx-controls`, `javafx-fxml`, classifier `win`) + **FXML** + **CSS** para la interfaz.
* **Maven** como build tool, con **JPMS** (`module-info.java`) para el modo desarrollo (`javafx:run`).
* **JDBC**: `mssql-jdbc` (SQL Server, backend por defecto) y `mariadb-java-client` (alterno).
* **JasperReports 6.21.3** para reportes y el RIDE de factura (plantillas `.jrxml` en `src/main/resources/upse/calculacion/reportes/`).
* **`java.xml.crypto` (JDK estándar)** para la firma XAdES-BES de los comprobantes SRI — sin librerías externas de firma.
* **`java.net.http.HttpClient`** para el cliente SOAP del SRI.
* **`java.util.logging`** centralizado a archivo (`Mod_log`).
* **maven-shade-plugin** + **jpackage** + **WiX Toolset** para el empaquetado del instalador Windows.

## Diseño Arquitectónico

Patrón en capas, tres paquetes principales bajo `upse.calculacion` (ver también `CLAUDE.md`):

1. **`controlador/`** — Controladores JavaFX/FXML. `App.java` es el entry point real solo en modo desarrollo (`javafx:run`); en el jar/instalador empaquetado el entry point es `Launcher.java` (ver *Empaquetado*).
2. **`Mad/`** (Model Access Data) — DAOs con `PreparedStatement`. Cada uno crea su propio `Mod_DB`. Nueva regla de la capa SRI: `Mad_factura` orquesta la venta (transaccional) y, aparte, la emisión electrónica (no transaccional, con reintentos).
3. **`modelo/`** — Beans de dominio.
4. **`sri/`** — Todo lo específico de facturación electrónica: `ClaveAccesoGenerador`, `FacturaXmlBuilder`, `FirmaXadesBes`, `SriSoapClient` y los DTOs de resultado.
5. **`general/`** — Utilidades transversales: `Mod_DB`, `Mod_general`, `Mod_VariablesGlobales`, `Mod_configuracion` (config externa), `Mod_hash` (PBKDF2), `Mod_log` (logging a archivo), `Mod_jasperReporte` (reportes).

## Requisitos del entorno de desarrollo

| Herramienta | Versión usada / mínima | Notas |
|---|---|---|
| JDK | **23** | `jpackage` (incluido en el JDK) se usa para el instalador. |
| Maven | 3.9+ | Sin *wrapper* (`mvnw`) incluido en el repo. |
| SQL Server | 2019+ (probado con SQL Server 2025) | Backend por defecto (`Mod_general.gestorBD = 2`). |
| MariaDB | 10.x/11.x | Backend alterno (`gestorBD = 1`), menos probado. |
| WiX Toolset | 3.14+ (`candle.exe`/`light.exe` en el `PATH`) | Solo si vas a generar el instalador `.msi`/`.exe`. |
| Sistema operativo | **Windows 10/11 (x64)** | Ver limitación de plataforma más abajo. |

**⚠️ Limitación de plataforma:** el proyecto está fijado a Windows en este momento — las dependencias de JavaFX se declaran con `classifier=win` (necesario para que el jar empaquetado funcione con `java -jar`) y `Mod_configuracion`/`Mod_log` usan `%APPDATA%`. Para soportar macOS/Linux habría que parametrizar el classifier de JavaFX por SO (perfiles Maven) y las rutas de configuración.

No hay pruebas automatizadas (`src/test` no existe). La verificación es manual: correr la app y probar el flujo afectado.

## Base de datos

El esquema completo está en **`databaseScript.sql`** (raíz del repo, no versionado en git — ver `.gitignore`). El script:

* Crea la base `BD2026_1` en SQL Server si no existe.
* Crea todas las tablas: `Cliente`, `Producto`, `Usuario`, `Perfil`, `Empresa`, `ConfiguracionSri`, `ParametroGeneral`, `Factura`, `DetalleFactura` (`AutorizacionFac` y `cab_Factura` son tablas heredadas de un esquema anterior, ya no las usa el código).
* Inserta datos semilla: perfil "Administrador", dos usuarios de prueba, dos productos de ejemplo, el parámetro `IVA = 15.00`, una fila de `Empresa` placeholder y la fila única de `ConfiguracionSri` en ambiente de pruebas.
* Trae al final un **bloque de migración comentado** para bases ya existentes que vienen de un esquema anterior (agrega `emp_obligadoContabilidad`, `fe_estado`, `fe_mensaje`, crea `ConfiguracionSri` si falta). Si tu base ya existía antes de estos cambios, corre ese bloque en vez del script completo — es exactamente lo que se usó para reparar la base de este entorno (ver commits de este mismo proyecto).

**Credenciales semilla** (usuario/contraseña en texto plano en el script — se re-hashean solas en el primer login exitoso, ver `Mad_seguridad.migrarAHash`):

| Usuario | Contraseña | Nombre |
|---|---|---|
| `admin` | `123` | JAIME OROZCO |
| `ktorres` | `321` | Karol Torres |

**⚠️ Antes de entregar a un cliente**, hay que reemplazar los datos semilla por datos reales: la fila de `Empresa` (RUC `0000000000001` es un placeholder), los productos de ejemplo (`Televisor LG`, `Cuaderno U`), y considerar rotar/crear las credenciales de usuario. Ver el checklist completo en `MANUAL_USUARIO.md`.

Conexión (por defecto, SQL Server): `localhost:1433`, base `BD2026_1`, usuario `sa`, contraseña `Admin.`, `encrypt=false;trustServerCertificate=true`. Todo esto es configurable sin recompilar — ver siguiente sección.

## Configuración externa (por instalación)

Para que un mismo instalador sirva a distintos clientes sin recompilar, la configuración vive **fuera** del código, en `%APPDATA%\facturacion\`:

* **`config.properties`** — se crea automáticamente con valores por defecto en el primer arranque (`Mod_configuracion.java`). Contiene credenciales de BD (MariaDB y SQL Server) y la ruta/clave del certificado de firma electrónica SRI:
  ```properties
  db.sqlserver.servidor=localhost
  db.sqlserver.baseDatos=BD2026_1
  db.sqlserver.usuario=sa
  db.sqlserver.clave=Admin.
  sri.rutaCertificado=
  sri.claveCertificado=
  ```
  El backend activo (SQL Server vs. MariaDB) sigue seleccionándose en código (`Mod_general.gestorBD`, no en este archivo) — cambiarlo requiere recompilar.
* **`logs\facturacion.log`** — log rotativo (5 archivos × 1 MB) de toda la aplicación, incluidos errores de conexión a BD y fallos de envío al SRI. Es el primer lugar a revisar ante un reporte de error del cliente, incluso si la app corre empaquetada sin consola visible.
* **`imagenes\`** — imágenes de productos subidas desde el formulario de producto.

## ▶️ Cómo ejecutar en desarrollo

```bash
mvn clean javafx:run              # ejecutar
mvn clean compile                 # solo compilar
mvn clean javafx:run@debug        # depurar (espera debugger en el puerto 8000)
```

## Empaquetado (jar ejecutable e instalador Windows)

El build por defecto (`mvn clean package`) genera **dos** jars en `target/`:

* `facturacion-<version>.jar` — jar delgado sin dependencias (no ejecutable por sí solo).
* `facturacion-<version>-jar-with-dependencies.jar` — **jar ejecutable** (`maven-shade-plugin`), con todas las dependencias incluidas. Se ejecuta con `java -jar`. El *entry point* es `upse.calculacion.controlador.Launcher`, no `App`: JavaFX no permite lanzar directamente desde un jar en classpath plano una clase que extiende `Application`, así que `Launcher` es un intermediario mínimo.

Para el **instalador de Windows** (`.msi` por defecto), usa el perfil `installer` (requiere WiX Toolset en el `PATH`):

```bash
mvn -Pinstaller clean verify
#  -> target/dist/Facturacion-1.0.0.msi

mvn -Pinstaller clean verify -Dinstaller.type=exe   # variante .exe
```

El instalador incluye el runtime completo de Java (no usa `jlink`/imagen reducida —ver nota abajo—, por eso pesa ~90 MB), ícono, acceso directo, entrada en el menú de inicio y desinstalador. La versión del instalador y el GUID de actualización (`installer.winUpgradeUuid`) se controlan como propiedades en `pom.xml`; mantener el mismo GUID entre versiones para que un instalador nuevo reemplace al anterior en vez de fallar por "ya instalado".

**Por qué no se usa `jlink`:** `jasperreports-6.21.3.jar` no declara un nombre de módulo JPMS explícito, así que Java lo resuelve como *automatic module* (Maven avisa de esto en cada compilación). Los módulos automáticos no son válidos dentro de una imagen de runtime `jlink`. La solución adoptada fue empaquetar en modo classpath plano (shade + jpackage sin `--runtime-image` reducido) en vez de resolver el módulo — más simple y con el mismo resultado para el usuario final, a cambio de un instalador más pesado.

## Internacionalización

`ResourceBundle` sobre `mensajes_es.properties` / `mensajes_en.properties` (`src/main/resources/upse/calculacion/idiomas/`). El selector de idioma está en la barra superior de la ventana principal y recarga la vista al vuelo, sin reiniciar la app.

## Facturación Electrónica (SRI)

El sistema emite comprobantes electrónicos (factura) conforme al esquema del Servicio de
Rentas Internas del Ecuador. El flujo, al emitir una venta en la pantalla de Facturación:

1. Se registra la venta en la base de datos (esto **nunca** se revierte por un problema del SRI).
2. Se genera la **clave de acceso** de 49 dígitos (fecha + RUC + ambiente + establecimiento +
   punto de emisión + secuencial + código numérico + dígito verificador módulo 11).
3. Se arma el XML de la factura (esquema v1.1.0: `infoTributaria`, `infoFactura`, `detalles`,
   `infoAdicional`) y se firma con **XAdES-BES** usando el certificado `.p12` configurado.
4. Se envía al web service `RecepcionComprobantesOffline` del SRI y se consulta
   `AutorizacionComprobantesOffline` unas cuantas veces (cada 2 segundos, hasta ~10s).
   Todo esto corre en un hilo de fondo (`javafx.concurrent.Task`); la ventana de facturación
   no se congela mientras se procesa.
5. **Síncrono con contingencia**: si el SRI autoriza a tiempo, la factura queda `AUTORIZADO`
   (o `RECHAZADO` si el SRI la rechaza, con el motivo guardado). Si el SRI no responde a
   tiempo, queda `PENDIENTE` — la venta ya está hecha, y el sistema reintenta la autorización
   automáticamente cada 5 minutos en segundo plano, o manualmente desde **Reportes → Listado
   de facturas → Reintentar SRI**.

Esto responde a la normativa vigente desde el **1 de enero de 2026** (Resolución
NAC-DGERCGC25-00000017), que exige transmitir los comprobantes al SRI en tiempo real al
momento de emitirse, en vez del esquema diferido usado antes de esa fecha.

### Configuración necesaria antes de emitir facturas reales

**En la base de datos** (ver `databaseScript.sql`):
- `dbo.Empresa`: RUC real (`emp_ruc`), razón social, dirección y si el negocio está
  `emp_obligadoContabilidad` (aparece impreso en cada factura).
- `dbo.ConfiguracionSri` (fila única): `ambiente` (`1` pruebas / `2` producción),
  `establecimiento` y `puntoEmision` (los códigos de 3 dígitos que asignó el SRI al local).

  No existe todavía una pantalla en la app para editar estos datos — se actualizan con
  `UPDATE` directo. Es una mejora pendiente natural (pantalla de "Configuración de la empresa").

**En `%APPDATA%\facturacion\config.properties`**:
- `sri.rutaCertificado`: ruta al archivo `.p12`/`.pfx` del certificado de firma electrónica
  (emitido por una entidad certificadora acreditada en Ecuador: Security Data, ANF, BCE, etc.).
- `sri.claveCertificado`: contraseña del certificado.

Si `sri.rutaCertificado` está vacío, la app **no intenta contactar al SRI**: la factura se
guarda igual y queda con estado SRI `PENDIENTE` con un mensaje explicando que falta el
certificado. Es el comportamiento esperado en un ambiente de desarrollo/demo.

### Aviso importante sobre el estado de esta funcionalidad

El código de firma XAdES-BES (`upse.calculacion.sri.FirmaXadesBes`) y el envío SOAP
(`upse.calculacion.sri.SriSoapClient`) están implementados siguiendo la estructura pública
documentada por el SRI, pero **no han podido probarse contra el ambiente de pruebas real del
SRI** (no había un certificado ni un RUC de pruebas disponibles al desarrollarlos). Antes de
emitir facturas a clientes reales:

1. Consigue un certificado de firma electrónica (puede ser de pruebas).
2. Configura `ambiente = 1` (pruebas) en `dbo.ConfiguracionSri`.
3. Emite una factura de prueba y confirma en el listado de facturas (Reportes) que el estado
   SRI llega a `AUTORIZADO`. Si el SRI la rechaza, el motivo queda en `fe_mensaje` — es la
   pista para ajustar el XML o la firma si algo no calza con el validador real.
4. Solo después de validar en pruebas, cambia `ambiente` a `2` (producción).

La generación de la clave de acceso (incluido el dígito verificador) y el armado del XML sí
se probaron de forma aislada (sin necesitar un certificado) y no deberían presentar problemas.

## Estado del proyecto / limitaciones conocidas

* **Sin pantalla de configuración**: los datos de `Empresa`, `ConfiguracionSri` y el parámetro
  `IVA` (`dbo.ParametroGeneral`) se editan por SQL directo, no desde la app.
- **Sin cambio de contraseña desde la UI**: para resetear una, hacer
  `UPDATE dbo.Usuario SET usr_clave = 'nueva_clave' WHERE usr_usuario = '...'` (texto plano);
  `Mad_seguridad` la re-hashea sola en el siguiente login exitoso del usuario.
* **Concurrencia de stock**: el descuento de stock al emitir factura es atómico
  (`UPDATE ... WHERE prod_stock >= ?`), así que dos cajas facturando el mismo producto a la vez
  no dejan el stock en negativo — la segunda simplemente falla con "stock insuficiente" y no
  se emite esa factura.
* **SRI no probado contra el ambiente real** — ver aviso arriba.
* **Solo Windows** por ahora (empaquetado y rutas de configuración).

## Documentos relacionados

* **[MANUAL_USUARIO.md](MANUAL_USUARIO.md)** — manual para el personal que usa el sistema, más el checklist de información a pedirle al cliente antes de poner la app en marcha.
* **`CLAUDE.md`** — guía de arquitectura para trabajar en el código con asistencia de IA (no se versiona en git, es local a cada checkout).
* **`databaseScript.sql`** — esquema completo + datos semilla + bloque de migración (tampoco versionado, ver `.gitignore`).

---
*Desarrollado para propósitos académicos y de gestión empresarial básica.*
