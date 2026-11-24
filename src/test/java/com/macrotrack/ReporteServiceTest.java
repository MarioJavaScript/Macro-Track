package com.macrotrack;

import com.macrotrack.model.Macros;
import com.macrotrack.service.ReporteService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ReporteServiceTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 1, 5);
    private static final double DELTA = 0.001;

    @Test
    void rellenarSemanaCompleta7DiasConCerosDondeFalta() {
        Map<LocalDate, Macros> totales = new HashMap<>();
        Macros miercoles = new Macros(1800, 120, 200, 60);
        totales.put(LUNES.plusDays(2), miercoles);

        Map<LocalDate, Macros> semana = ReporteService.rellenarSemana(LUNES, totales);

        assertEquals(7, semana.size());
        assertSame(miercoles, semana.get(LUNES.plusDays(2)));
        assertSame(Macros.CERO, semana.get(LUNES));
        assertSame(Macros.CERO, semana.get(LUNES.plusDays(6)));
    }

    @Test
    void rellenarSemanaMantieneElOrdenCronologico() {
        // Se entregan los datos desordenados para comprobar que el resultado sale en orden
        Map<LocalDate, Macros> totales = new HashMap<>();
        totales.put(LUNES.plusDays(5), new Macros(1, 1, 1, 1));
        totales.put(LUNES.plusDays(1), new Macros(1, 1, 1, 1));

        List<LocalDate> fechas = new ArrayList<>(ReporteService.rellenarSemana(LUNES, totales).keySet());

        for (int i = 0; i < 7; i++) {
            assertEquals(LUNES.plusDays(i), fechas.get(i));
        }
    }

    @Test
    void promediarIgnoraLosDiasVacios() {
        Map<LocalDate, Macros> totales = new HashMap<>();
        totales.put(LUNES, new Macros(2000, 100, 200, 60));
        totales.put(LUNES.plusDays(3), new Macros(1000, 50, 100, 30));

        Macros promedio = ReporteService.promediar(ReporteService.rellenarSemana(LUNES, totales));

        // Se divide entre 2 días con datos, no entre 7
        assertEquals(1500, promedio.getKcal(), DELTA);
        assertEquals(75, promedio.getProteina(), DELTA);
        assertEquals(150, promedio.getCarbohidratos(), DELTA);
        assertEquals(45, promedio.getGrasas(), DELTA);
    }

    @Test
    void promediarSinDatosDevuelveCero() {
        Macros promedio = ReporteService.promediar(ReporteService.rellenarSemana(LUNES, new HashMap<>()));

        assertSame(Macros.CERO, promedio);
    }
}
