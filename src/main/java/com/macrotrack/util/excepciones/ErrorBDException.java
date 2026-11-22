package com.macrotrack.util.excepciones;

import java.sql.SQLException;

/**
 * Envuelve una SQLException para que la capa de pantallas no dependa de java.sql.
 * La causa original queda guardada (getCause) para depurar.
 */
public class ErrorBDException extends MacroTrackException {

    public ErrorBDException(SQLException causa) {
        super("No se pudo conectar con la base de datos", causa);
    }
}
