# MacroTrack

Aplicación de escritorio (JavaFX + MySQL) para registrar y dar seguimiento a macronutrientes.
Proyecto universitario de Desarrollo en Sistemas III.

## Requisitos

- JDK 21
- Maven
- MySQL 8

## Crear la base de datos

```bash
mysql -u root -p < sql/schema.sql
```

## Configurar la conexión

1. Copia `src/main/resources/db.properties.example` como `src/main/resources/db.properties`.
2. Edita `db.url`, `db.usuario` y `db.password` con tus datos.

`db.properties` no se sube a Git.

## Ejecutar

```bash
mvn javafx:run
```
