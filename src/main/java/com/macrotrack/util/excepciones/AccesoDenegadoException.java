package com.macrotrack.util.excepciones;

/**
 * No hay sesión iniciada, no hay permisos o el registro pertenece a otro usuario.
 */
public class AccesoDenegadoException extends MacroTrackException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
