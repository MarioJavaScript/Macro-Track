package com.macrotrack.model;

/**
 * Alimento del catálogo (tabla alimento). Sus macros están expresados por cada 100 g.
 */
public class Alimento {

    private int id;
    private String nombre;
    private Macros por100g;
    private boolean activo;
    private String idExterno;

    public Alimento() {
    }

    /**
     * Calcula los macros de una porción de este alimento.
     *
     * @param gramos cantidad en gramos, debe ser mayor que 0
     * @throws IllegalArgumentException si gramos es menor o igual a 0
     */
    public Macros calcularPara(double gramos) {
        if (gramos <= 0) {
            throw new IllegalArgumentException("Los gramos deben ser mayores que cero");
        }
        return por100g.escalar(gramos / 100);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Macros getPor100g() {
        return por100g;
    }

    public void setPor100g(Macros por100g) {
        this.por100g = por100g;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getIdExterno() {
        return idExterno;
    }

    public void setIdExterno(String idExterno) {
        this.idExterno = idExterno;
    }

    /** Devuelve el nombre, para mostrarlo directo en ComboBox y ListView. */
    @Override
    public String toString() {
        return nombre;
    }
}
