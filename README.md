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

Si ves el error "Falta db.properties: copia db.properties.example y configúralo", revisa que:

- el archivo `src/main/resources/db.properties` exista;
- `db.url` apunte a tu servidor (`jdbc:mysql://localhost:3306/macrotrack`);
- `db.usuario` y `db.password` sean los de tu MySQL;
- el servidor MySQL esté encendido.

## Ejecutar

```bash
mvn javafx:run
```

## Probar

```bash
mvn -q test
```

## Estado

Pantallas listas: inicio de sesión, registro, dashboard, registrar comida, historial y admin de alimentos.
