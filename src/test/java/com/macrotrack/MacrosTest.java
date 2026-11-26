package com.macrotrack;

import com.macrotrack.model.Macros;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MacrosTest {

    private static final double DELTA = 0.001;

    @Test
    void sumarJuntaCadaValor() {
        Macros r = new Macros(100, 10, 20, 5).sumar(new Macros(50, 2.5, 3, 1));

        assertEquals(150, r.getKcal(), DELTA);
        assertEquals(12.5, r.getProteina(), DELTA);
        assertEquals(23, r.getCarbohidratos(), DELTA);
        assertEquals(6, r.getGrasas(), DELTA);
    }

    @Test
    void restarQuitaCadaValor() {
        Macros r = new Macros(2000, 150, 200, 60).restar(new Macros(500, 40, 50, 10));

        assertEquals(1500, r.getKcal(), DELTA);
        assertEquals(110, r.getProteina(), DELTA);
        assertEquals(150, r.getCarbohidratos(), DELTA);
        assertEquals(50, r.getGrasas(), DELTA);
    }

    @Test
    void escalarMultiplicaCadaValor() {
        Macros r = new Macros(165, 31, 0, 3.6).escalar(1.5);

        assertEquals(247.5, r.getKcal(), DELTA);
        assertEquals(46.5, r.getProteina(), DELTA);
        assertEquals(0, r.getCarbohidratos(), DELTA);
        assertEquals(5.4, r.getGrasas(), DELTA);
    }

    @Test
    void escalarRedondeaAUnDecimal() {
        Macros r = new Macros(10.26, 10.24, 0, 0).escalar(1);

        assertEquals(10.3, r.getKcal(), DELTA);
        assertEquals(10.2, r.getProteina(), DELTA);
    }

    @Test
    void sumarNoModificaElOriginal() {
        Macros original = new Macros(100, 10, 20, 5);
        original.sumar(new Macros(50, 5, 5, 5));

        assertEquals(100, original.getKcal(), DELTA);
    }

    @Test
    void escalarPorCeroDejaTodoEnCero() {
        Macros r = new Macros(165, 31, 10, 3.6).escalar(0);

        assertEquals(0, r.getKcal(), DELTA);
        assertEquals(0, r.getProteina(), DELTA);
        assertEquals(0, r.getCarbohidratos(), DELTA);
        assertEquals(0, r.getGrasas(), DELTA);
    }
}
