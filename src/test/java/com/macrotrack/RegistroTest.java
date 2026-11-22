package com.macrotrack;

import com.macrotrack.model.Alimento;
import com.macrotrack.model.Macros;
import com.macrotrack.model.Registro;
import com.macrotrack.model.TipoComida;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegistroTest {

    @Test
    void cambiarGramosRecalculaLosMacros() {
        Alimento alimento = new Alimento();
        alimento.setPor100g(new Macros(165, 31, 0, 3.6));
        Registro registro = new Registro(1, alimento, LocalDate.of(2026, 1, 15), TipoComida.COMIDA, 100);
        assertEquals(31, registro.getMacros().getProteina(), 0.001);

        registro.cambiarGramos(200);

        assertEquals(200, registro.getGramos(), 0.001);
        assertEquals(62, registro.getMacros().getProteina(), 0.001);
        assertEquals(330, registro.getMacros().getKcal(), 0.001);
    }
}
