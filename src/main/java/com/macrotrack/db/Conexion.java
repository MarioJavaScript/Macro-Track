package com.macrotrack.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Entrega conexiones a MySQL usando los datos de db.properties (en el classpath).
 */
public final class Conexion {

    private static final String ARCHIVO = "db.properties";
    private static final Properties config = new Properties();

    /** Mensaje de error si no se pudo leer la configuración; null si todo salió bien. */
    private static String errorConfig = null;

    // Se lee el archivo una sola vez, al cargar la clase
    static {
        try (InputStream entrada = Conexion.class.getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (entrada == null) {
                errorConfig = "Falta db.properties: copia db.properties.example y configúralo";
            } else {
                config.load(entrada);
            }
        } catch (IOException e) {
            errorConfig = "No se pudo leer db.properties: " + e.getMessage();
        }
    }

    /** Clase de utilidad: no se instancia. */
    private Conexion() {
    }

    /**
     * Abre una conexión nueva a la base de datos.
     * Quien la use debe cerrarla con try-with-resources.
     *
     * @throws SQLException si falta la configuración o falla la conexión
     */
    public static Connection obtener() throws SQLException {
        if (errorConfig != null) {
            throw new SQLException(errorConfig);
        }
        return DriverManager.getConnection(
                config.getProperty("db.url"),
                config.getProperty("db.usuario"),
                config.getProperty("db.password"));
    }
}
