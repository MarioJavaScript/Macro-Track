package com.macrotrack.util.excepciones;

/**
 * Lo que se buscó no existe.
 */
public class NoEncontradoException extends MacroTrackException {

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
