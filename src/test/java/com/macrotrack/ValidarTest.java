package com.macrotrack;

import com.macrotrack.util.Validar;
import com.macrotrack.util.excepciones.ValidacionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidarTest {

    @Test
    void emailInvalidoLanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> Validar.email("sin-arroba"));
        assertThrows(ValidacionException.class, () -> Validar.email("a@b"));
        assertThrows(ValidacionException.class, () -> Validar.email(null));
    }

    @Test
    void emailValidoPasa() {
        assertDoesNotThrow(() -> Validar.email("mario@correo.com"));
    }

    @Test
    void emailConSubdominiosPasa() {
        assertDoesNotThrow(() -> Validar.email("mario@mail.correo.com"));
    }

    @Test
    void valorFueraDeRangoLanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> Validar.rango(0, 0.1, 5000, "Los gramos"));
        assertThrows(ValidacionException.class, () -> Validar.rango(5001, 0.1, 5000, "Los gramos"));
    }

    @Test
    void valorDentroDelRangoPasa() {
        assertDoesNotThrow(() -> Validar.rango(0.1, 0.1, 5000, "Los gramos"));
        assertDoesNotThrow(() -> Validar.rango(5000, 0.1, 5000, "Los gramos"));
    }

    @Test
    void textoVacioLanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> Validar.texto("   ", "El nombre"));
        assertThrows(ValidacionException.class, () -> Validar.texto(null, "El nombre"));
    }

    @Test
    void positivoRechazaCeroYNegativos() {
        assertThrows(ValidacionException.class, () -> Validar.positivo(0, "Las calorías"));
        assertThrows(ValidacionException.class, () -> Validar.positivo(-5, "Las calorías"));
    }
}
