package com.macrotrack.model;

import java.time.LocalDate;

/**
 * Comida registrada por un usuario (tabla registro).
 * Los macros guardados son los de la porción, no los de 100 g.
 */
public class Registro {

    private int id;
    private int usuarioId;
    private Alimento alimento;
    private LocalDate fecha;
    private TipoComida tipoComida;
    private double gramos;
    private Macros macros;

    /** Constructor vacío, usado al cargar desde la BD. */
    public Registro() {
    }

    /** Registro nuevo: calcula los macros de la porción a partir de los gramos. */
    public Registro(int usuarioId, Alimento alimento, LocalDate fecha,
                    TipoComida tipoComida, double gramos) {
        this.usuarioId = usuarioId;
        this.alimento = alimento;
        this.fecha = fecha;
        this.tipoComida = tipoComida;
        this.gramos = gramos;
        this.macros = alimento.calcularPara(gramos);
    }

    /** Cambia los gramos y recalcula los macros en el mismo paso, para que no queden desfasados. */
    public void cambiarGramos(double nuevosGramos) {
        this.macros = alimento.calcularPara(nuevosGramos);
        this.gramos = nuevosGramos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Alimento getAlimento() {
        return alimento;
    }

    public void setAlimento(Alimento alimento) {
        this.alimento = alimento;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public TipoComida getTipoComida() {
        return tipoComida;
    }

    public void setTipoComida(TipoComida tipoComida) {
        this.tipoComida = tipoComida;
    }

    public double getGramos() {
        return gramos;
    }

    /** Solo para cargar desde la BD. Para modificar gramos en la app usar cambiarGramos(). */
    public void setGramos(double gramos) {
        this.gramos = gramos;
    }

    public Macros getMacros() {
        return macros;
    }

    public void setMacros(Macros macros) {
        this.macros = macros;
    }
}
