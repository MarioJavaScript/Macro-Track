package com.macrotrack.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Resumen de un día para la pantalla principal: lo registrado, la meta y lo que falta.
 */
public class ResumenDiario {

    private final LocalDate fecha;
    private final List<Registro> registros;
    private final Macros consumido;
    private final Macros meta;
    private final Macros restante;

    public ResumenDiario(LocalDate fecha, List<Registro> registros,
                         Macros consumido, Macros meta, Macros restante) {
        this.fecha = fecha;
        this.registros = registros;
        this.consumido = consumido;
        this.meta = meta;
        this.restante = restante;
    }

    /**
     * Porcentaje de la meta que se ha consumido, limitado a 0..100 (para barras de progreso).
     * Si la meta es 0 (o menor) devuelve 0.
     */
    public static double porcentaje(double consumido, double meta) {
        if (meta <= 0) {
            return 0;
        }
        double porcentaje = consumido / meta * 100;
        return Math.max(0, Math.min(100, porcentaje));
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public List<Registro> getRegistros() {
        return registros;
    }

    public Macros getConsumido() {
        return consumido;
    }

    public Macros getMeta() {
        return meta;
    }

    public Macros getRestante() {
        return restante;
    }
}
