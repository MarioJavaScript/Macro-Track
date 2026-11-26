package com.macrotrack.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {
    private static final int BYTES_SALT = 16;
    private static final int ITERACIONES = 210_000;
    private static final int LARGO_CLAVE = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private PasswordUtil() {
    }
    public static String hash(String password) {
        byte[] salt = new byte[BYTES_SALT];
        ALEATORIO.nextBytes(salt);
        byte[] hash = calcular(salt, password);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }
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
            // comparo en tiempo constante por si acaso
            return MessageDigest.isEqual(esperado, calcular(salt, password));
        } catch (IllegalArgumentException e) {
            // no era base64 valido
            return false;
        }
    }
    private static byte[] calcular(byte[] salt, String password) {
        try {
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERACIONES, LARGO_CLAVE);
            return fabrica.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // esto no deberia pasar en java
            throw new IllegalStateException("PBKDF2 no disponible", e);
        }
    }
}
