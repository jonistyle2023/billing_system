# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Run the application
mvn clean javafx:run

# Compile only
mvn clean compile

# Debug (suspends until debugger attaches on port 8000)
mvn clean javafx:run@debug
```

There are no automated tests. The app is a desktop GUI that must be run manually to verify behavior.

## Architecture

This is a JavaFX desktop billing system (facturación) following a layered MVC pattern with three distinct Java packages:

**`controlador/`** — JavaFX controllers bound to FXML views. `App.java` is the entry point and manages scene switching, i18n `ResourceBundle` loading, and the current logged-in `Usuario`. Views are loaded via `App.loadFXML(name)` which applies the active locale automatically.

**`Mad/`** (Model Access Data) — Database access objects that use `PreparedStatement` to prevent SQL injection. `Mad_cliente`, `Mad_producto`, `Mad_empresa` each own a `Mod_DB` instance and handle their entity's CRUD. **New DB operations belong here using `PreparedStatement`, not string concatenation.**

**`modelo/`** — Plain Java beans: `Cliente`, `Producto`, `Empresa`, `Usuario`, `CabeceraFactura`, `DetFactura`.

**`general/`** — Cross-cutting utilities:
- `Mod_DB` — wraps `Connection`/`Statement`/`ResultSet`, provides `getListaConsulta()` (generic mapper), `fun_ejecutar()`, and transaction helpers.
- `Mod_general` — holds `gestorBD` flag (1 = MariaDB, 2 = SQL Server), `DIRVISTAS` constant, and shared `Alert`/focus helpers.
- `Mod_VariablesGlobales` — app-wide mutable state (current invoice number, date formats).

## Database

Two supported backends, switched by `Mod_general.gestorBD`:
- `1` → MariaDB at `localhost:3306/base20261` (user: `root`, no password)
- `2` (default) → SQL Server at `localhost:1433/BD2026_1` (user: `sa`, password: `Admin.`)

Connection credentials are hardcoded in `Mod_DB.conectarBD()`. The schema is in `databaseScript.sql` (targets SQL Server `BD2024_1`/`BD2026_1`). Soft deletes are used: `cli_estado`, `pro_estado`, `usr_estado` = `'A'` (active) / `'E'` (inactive).

## FXML & i18n

All views live in `src/main/resources/upse/calculacion/vistas/`. The global stylesheet is `styles.css` in the same directory. Views use `%key` syntax to reference strings from `mensajes_es.properties` or `mensajes_en.properties` in `src/main/resources/upse/calculacion/idiomas/`. Language switching happens at runtime via `App.cambiarIdioma(locale)` followed by reloading the current view — no restart needed.

## Module System

`module-info.java` opens `upse.calculacion.controlador` to `javafx.fxml` for reflection-based controller injection. If you add a new package that needs FXML injection or is exported, update `module-info.java`.

## Login

Default credentials (from `databaseScript.sql`): username `jorozco` / password `123`, or `ktorres` / `321`.