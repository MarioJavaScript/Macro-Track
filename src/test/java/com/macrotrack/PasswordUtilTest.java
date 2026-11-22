package com.macrotrack;

import com.macrotrack.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void verificarAceptaLaContrasenaCorrecta() {
        String guardado = PasswordUtil.hash("secreta123");

        assertTrue(PasswordUtil.verificar("secreta123", guardado));
    }

    @Test
    void verificarRechazaLaContrasenaIncorrecta() {
        String guardado = PasswordUtil.hash("secreta123");

        assertFalse(PasswordUtil.verificar("otra-cosa", guardado));
    }

    @Test
    void dosHashDeLaMismaContrasenaSonDistintos() {
        assertNotEquals(PasswordUtil.hash("secreta123"), PasswordUtil.hash("secreta123"));
    }

    @Test
    void verificarRechazaUnHashMalFormado() {
        assertFalse(PasswordUtil.verificar("secreta123", "esto no es un hash"));
        assertFalse(PasswordUtil.verificar("secreta123", null));
    }
}
