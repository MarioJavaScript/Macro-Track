package com.macrotrack.util;

import com.macrotrack.model.Usuario;
import com.macrotrack.util.excepciones.AccesoDenegadoException;

/**
 * Guarda el usuario que inició sesión mientras la app está abierta.
 */
public final class Sesion {

    private static Usuario usuario;

    /** Clase de utilidad: no se instancia. */
    private Sesion() {
    }

    /** Guarda al usuario como el que tiene la sesión activa. */
    public static void iniciar(Usuario nuevo) {
        usuario = nuevo;
    }

    /** Cierra la sesión. */
    public static void cerrar() {
        usuario = null;
    }

    /**
     * Devuelve el usuario en sesión.
     *
     * @throws AccesoDenegadoException si nadie ha iniciado sesión
     */
    public static Usuario actual() {
        if (usuario == null) {
            throw new AccesoDenegadoException("Debes iniciar sesión para continuar");
        }
        return usuario;
    }

    /** Indica si hay una sesión activa. */
    public static boolean hayUsuario() {
        return usuario != null;
    }
}
