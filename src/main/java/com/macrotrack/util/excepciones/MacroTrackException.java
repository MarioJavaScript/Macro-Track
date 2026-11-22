package com.macrotrack.util.excepciones;

/**
 * Excepción base de MacroTrack. Su mensaje está en español y se puede mostrar tal cual en un Alert.
 */
public class MacroTrackException extends RuntimeException {

    public MacroTrackException(String mensaje) {
        super(mensaje);
    }

    public MacroTrackException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
