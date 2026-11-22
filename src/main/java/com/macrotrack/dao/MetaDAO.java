package com.macrotrack.dao;

import com.macrotrack.db.Conexion;
import com.macrotrack.model.Macros;
import com.macrotrack.model.Meta;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Acceso a la tabla meta mediante procedimientos almacenados.
 */
public class MetaDAO {

    private static final String SP_GUARDAR = "{call sp_meta_guardar(?, ?, ?, ?, ?)}";
    private static final String SP_BUSCAR = "{call sp_meta_buscar(?)}";

    /**
     * Crea la meta del usuario o la actualiza si ya tenía una.
     * Usa: sp_meta_guardar
     */
    public void guardar(Meta meta) throws SQLException {
        Macros objetivo = meta.getObjetivo();
        try (Connection con = Conexion.obtener();
             CallableStatement cs = con.prepareCall(SP_GUARDAR)) {
            cs.setInt(1, meta.getUsuarioId());
            cs.setDouble(2, objetivo.getKcal());
            cs.setDouble(3, objetivo.getProteina());
            cs.setDouble(4, objetivo.getCarbohidratos());
            cs.setDouble(5, objetivo.getGrasas());
            cs.execute();
        }
    }

    /**
     * Busca la meta de un usuario; vacío si aún no tiene.
     * Usa: sp_meta_buscar
     */
    public Optional<Meta> buscarPorUsuario(int usuarioId) throws SQLException {
        try (Connection con = Conexion.obtener();
             CallableStatement cs = con.prepareCall(SP_BUSCAR)) {
            cs.setInt(1, usuarioId);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
                return Optional.empty();
            }
        }
    }

    /** Convierte la fila actual del ResultSet en una Meta. */
    private Meta mapear(ResultSet rs) throws SQLException {
        Meta m = new Meta();
        m.setId(rs.getInt("id"));
        m.setUsuarioId(rs.getInt("usuario_id"));
        m.setObjetivo(new Macros(
                rs.getDouble("kcal"),
                rs.getDouble("proteina_g"),
                rs.getDouble("carbohidratos_g"),
                rs.getDouble("grasas_g")));
        return m;
    }
}
