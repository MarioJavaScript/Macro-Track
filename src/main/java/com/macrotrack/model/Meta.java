package com.macrotrack.model;

/**
 * Meta diaria de macros de un usuario (tabla meta).
 */
public class Meta {

    private int id;
    private int usuarioId;
    private Macros objetivo;

    public Meta() {
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

    public Macros getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Macros objetivo) {
        this.objetivo = objetivo;
    }
}
