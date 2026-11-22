package com.macrotrack.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash de contraseñas con SHA-256 y un salt aleatorio por contraseña.
 * Formato guardado: "salt:hash", ambos en Base64.
 */
public final class PasswordUtil {

    private static final int BYTES_SALT = 16;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    /** Clase de utilidad: no se instancia. */
    private PasswordUtil() {
    }

    /** Genera un salt nuevo y devuelve "salt:hash" para guardar en la BD. */
    public static String hash(String password) {
        byte[] salt = new byte[BYTES_SALT];
        ALEATORIO.nextBytes(salt);
        byte[] hash = calcular(salt, password);
        return Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    /** Indica si la contraseña corresponde al "salt:hash" guardado. */
    public static boolean verificar(String password, String guardado) {
        if (password == null || guardado == null) {
            return false;
        }
        String[] partes = guardado.split(":");
        if (partes.length != 2) {
            return false;
        }
        try {
            byte[] salt = Base64.getDecoder().decode(partes[0]);
            byte[] esperado = Base64.getDecoder().decode(partes[1]);
            // isEqual compara en tiempo constante
            return MessageDigest.isEqual(esperado, calcular(salt, password));
        } catch (IllegalArgumentException e) {
            // El texto guardado no era Base64 válido
            return false;
        }
    }

    /** SHA-256 de (salt + password). */
    private static byte[] calcular(byte[] salt, String password) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(salt);
            return sha.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 siempre existe en Java; esto no debería ocurrir
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
