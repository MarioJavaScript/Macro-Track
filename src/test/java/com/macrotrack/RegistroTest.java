package com.macrotrack;

import com.macrotrack.model.Alimento;
import com.macrotrack.model.Macros;
import com.macrotrack.model.Registro;
import com.macrotrack.model.TipoComida;
import com.macrotrack.service.RegistroService;
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

    @Test
    void cambiarGramosEscalaElSnapshotAunqueCambieElAlimento() {
        Alimento alimento = new Alimento();
        alimento.setPor100g(new Macros(200, 10, 20, 5));
        Registro registro = new Registro(1, alimento, LocalDate.of(2026, 1, 15), TipoComida.COMIDA, 100);

        // El admin cambia el alimento en el catálogo después de registrarlo
        alimento.setPor100g(new Macros(250, 10, 20, 5));

        registro.cambiarGramos(100);
        assertEquals(200, registro.getMacros().getKcal(), 0.001);

        registro.cambiarGramos(50);
        assertEquals(100, registro.getMacros().getKcal(), 0.001);
    }

    @Test
    void gramosSeRedondeanAUnDecimalComoLaBD() {
        double gramos = RegistroService.redondearGramos(10.15);
        assertEquals(10.2, gramos, 0.0001);

        // Los macros se calculan con los mismos 10.2 g que se guardan
        Alimento alimento = new Alimento();
        alimento.setPor100g(new Macros(100, 10, 10, 10));
        Registro registro = new Registro(1, alimento, LocalDate.of(2026, 1, 15), TipoComida.COMIDA, gramos);
        assertEquals(10.2, registro.getGramos(), 0.0001);
        assertEquals(10.2, registro.getMacros().getKcal(), 0.0001);
    }
}
