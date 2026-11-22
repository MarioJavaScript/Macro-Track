package com.macrotrack;

import com.macrotrack.model.Alimento;
import com.macrotrack.model.Macros;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlimentoTest {

    /** Pechuga: 165 kcal, 31 g de proteína, 0 g carbohidratos y 3.6 g grasas por 100 g. */
    private Alimento pechuga() {
        Alimento a = new Alimento();
        a.setNombre("Pechuga");
        a.setPor100g(new Macros(165, 31, 0, 3.6));
        return a;
    }

    @Test
    void calcularPara150GramosDa46Punto5DeProteina() {
        Macros porcion = pechuga().calcularPara(150);

        assertEquals(46.5, porcion.getProteina(), 0.001);
        assertEquals(247.5, porcion.getKcal(), 0.001);
    }

    @Test
    void gramosCeroLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> pechuga().calcularPara(0));
    }

    @Test
    void gramosNegativosLanzanExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> pechuga().calcularPara(-10));
    }
}
