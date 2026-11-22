package com.macrotrack.util;

import com.macrotrack.util.excepciones.ValidacionException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validaciones pequeñas y reutilizables. Todas lanzan ValidacionException
 * con un mensaje listo para mostrar al usuario.
 */
public final class Validar {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    /** Clase de utilidad: no se instancia. */
    private Validar() {
    }

    /** Verifica que el texto no sea nulo ni esté en blanco; lo devuelve sin espacios sobrantes. */
    public static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException(campo + " es obligatorio");
        }
        return valor.trim();
    }

    /** Igual que texto(valor, campo) y además limita la longitud máxima. */
    public static String texto(String valor, String campo, int maxLongitud) {
        String limpio = texto(valor, campo);
        if (limpio.length() > maxLongitud) {
            throw new ValidacionException(campo + " no puede tener más de " + maxLongitud + " caracteres");
        }
        return limpio;
    }

    /** Verifica que el correo tenga formato válido (algo@dominio.ext). */
    public static void email(String valor) {
        if (valor == null || !EMAIL.matcher(valor.trim()).matches()) {
            throw new ValidacionException("El correo no tiene un formato válido");
        }
    }

    /** Verifica que el valor sea mayor que cero. */
    public static void positivo(double valor, String campo) {
        // Se escribe así para que NaN también falle
        if (!(valor > 0)) {
            throw new ValidacionException(campo + " debe ser mayor que cero");
        }
    }

    /** Verifica que el valor sea mayor o igual que cero. */
    public static void noNegativo(double valor, String campo) {
        if (!(valor >= 0)) {
            throw new ValidacionException(campo + " no puede ser negativo");
        }
    }

    /** Verifica que el valor esté entre min y max (ambos incluidos). */
    public static void rango(double valor, double min, double max, String campo) {
        if (!(valor >= min && valor <= max)) {
            throw new ValidacionException(campo + " debe estar entre " + numero(min) + " y " + numero(max));
        }
    }

    /** Formato corto para el mensaje: 5000 en vez de 5000.0, 0.1 tal cual. */
    private static String numero(double n) {
        return n == Math.rint(n) ? String.format(Locale.US, "%.0f", n) : String.valueOf(n);
    }
}
