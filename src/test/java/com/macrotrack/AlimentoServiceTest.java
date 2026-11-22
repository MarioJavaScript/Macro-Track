package com.macrotrack;

import com.macrotrack.model.Macros;
import com.macrotrack.service.AlimentoService;
import com.macrotrack.util.excepciones.ValidacionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlimentoServiceTest {

    @Test
    void datosCorrectosPasanYDevuelvenElNombreSinEspacios() {
        Macros pechuga = new Macros(165, 31, 0, 3.6);

        assertEquals("Pechuga", AlimentoService.validarDatos("  Pechuga ", pechuga));
    }

    @Test
    void sumaDeMacrosMayorA100LanzaValidacionException() {
        Macros imposible = new Macros(400, 50, 40, 20); // suma 110 g en 100 g

        assertThrows(ValidacionException.class, () -> AlimentoService.validarDatos("Raro", imposible));
    }

    @Test
    void sumaExactaDe100EsValida() {
        assertDoesNotThrow(() -> AlimentoService.validarDatos("Mezcla", new Macros(400, 50, 30, 20)));
    }

    @Test
    void kcalFueraDeRangoLanzaValidacionException() {
        assertThrows(ValidacionException.class,
                () -> AlimentoService.validarDatos("Raro", new Macros(901, 10, 10, 10)));
        assertThrows(ValidacionException.class,
                () -> AlimentoService.validarDatos("Raro", new Macros(-1, 10, 10, 10)));
    }

    @Test
    void macroNegativoOMayorA100LanzaValidacionException() {
        assertThrows(ValidacionException.class,
                () -> AlimentoService.validarDatos("Raro", new Macros(100, -1, 10, 10)));
        assertThrows(ValidacionException.class,
                () -> AlimentoService.validarDatos("Raro", new Macros(100, 101, 0, 0)));
    }

    @Test
    void nombreVacioOMuyLargoLanzaValidacionException() {
        Macros ok = new Macros(100, 10, 10, 10);

        assertThrows(ValidacionException.class, () -> AlimentoService.validarDatos("  ", ok));
        assertThrows(ValidacionException.class, () -> AlimentoService.validarDatos("x".repeat(121), ok));
        assertDoesNotThrow(() -> AlimentoService.validarDatos("x".repeat(120), ok));
    }

    @Test
    void macrosNulosLanzanValidacionException() {
        assertThrows(ValidacionException.class, () -> AlimentoService.validarDatos("Raro", null));
    }
}
