package com.macrotrack;

import com.macrotrack.model.ResumenDiario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResumenDiarioTest {

    @Test
    void porcentajeConMetaCeroDevuelveCero() {
        assertEquals(0, ResumenDiario.porcentaje(500, 0), 0.001);
    }

    @Test
    void porcentajeNormal() {
        assertEquals(25, ResumenDiario.porcentaje(500, 2000), 0.001);
    }

    @Test
    void porcentajeNoPasaDeCien() {
        assertEquals(100, ResumenDiario.porcentaje(3000, 2000), 0.001);
    }

    @Test
    void porcentajeNoBajaDeCero() {
        assertEquals(0, ResumenDiario.porcentaje(-100, 2000), 0.001);
    }
}
