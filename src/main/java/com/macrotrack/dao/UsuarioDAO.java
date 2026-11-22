package com.macrotrack.dao;

import com.macrotrack.db.Conexion;
import com.macrotrack.model.Rol;
import com.macrotrack.model.Usuario;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

/**
 * Acceso a la tabla usuario mediante procedimientos almacenados.
 */
public class UsuarioDAO {

    private static final String SP_INSERTAR = "{call sp_usuario_insertar(?, ?, ?, ?)}";
    private static final String SP_BUSCAR_POR_EMAIL = "{call sp_usuario_buscar_por_email(?)}";

    /**
     * Guarda un usuario nuevo y devuelve el id generado.
     * Usa: sp_usuario_insertar
     */
    public int insertar(Usuario usuario) throws SQLException {
        try (Connection con = Conexion.obtener();
             CallableStatement cs = con.prepareCall(SP_INSERTAR)) {
            cs.setString(1, usuario.getNombre());
            cs.setString(2, usuario.getEmail());
            cs.setString(3, usuario.getPasswordHash());
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute();
            return cs.getInt(4);
        }
    }

    /**
     * Busca un usuario por su email; vacío si no existe.
     * Usa: sp_usuario_buscar_por_email
     */
    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        try (Connection con = Conexion.obtener();
             CallableStatement cs = con.prepareCall(SP_BUSCAR_POR_EMAIL)) {
            cs.setString(1, email);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
                return Optional.empty();
            }
        }
    }

    /** Convierte la fila actual del ResultSet en un Usuario. */
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRol(Rol.valueOf(rs.getString("rol")));
        u.setActivo(rs.getBoolean("activo"));
        return u;
    }
}
