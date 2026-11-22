package com.macrotrack.util.excepciones;

/**
 * Los datos que mandó el usuario no son válidos.
 */
public class ValidacionException extends MacroTrackException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
