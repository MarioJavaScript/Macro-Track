package com.macrotrack.model;

/**
 * Representa un usuario de la aplicación (tabla usuario).
 */
public class Usuario {

    private int id;
    private String nombre;
    private String email;
    private String passwordHash;
    private Rol rol;
    private boolean activo;

    /** Constructor vacío, usado al cargar desde la BD. */
    public Usuario() {
    }

    /** Usuario nuevo: por defecto con rol USUARIO y activo. */
    public Usuario(String nombre, String email, String passwordHash) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = Rol.USUARIO;
        this.activo = true;
    }

    /** Indica si el usuario tiene rol de administrador. */
    public boolean esAdmin() {
        return rol == Rol.ADMIN;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
