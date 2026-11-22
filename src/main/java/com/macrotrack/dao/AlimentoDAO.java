package com.macrotrack.dao;

import com.macrotrack.db.Conexion;
import com.macrotrack.model.Alimento;
import com.macrotrack.model.Macros;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a la tabla alimento mediante procedimientos almacenados.
 */
public class AlimentoDAO {

    private static final String SP_BUSCAR = "{call sp_alimento_buscar(?)}";
    private static final String SP_BUSCAR_POR_ID = "{call sp_alimento_buscar_por_id(?)}";
    private static final String SP_INSERTAR = "{call sp_alimento_insertar(?, ?, ?, ?, ?, ?, ?)}";
    private static final String SP_ACTUALIZAR = "{call sp_alimento_actualizar(?, ?, ?, ?, ?, ?)}";
    private static final String SP_DESACTIVAR = "{call sp_alimento_desactivar(?)}";

    /**
     * Busca alimentos activos cuyo nombre contenga el texto (máximo 50).
     * Usa: sp_alimento_buscar
     */
    public List<Alimento> buscar(String texto) throws SQLException {
        List<Alimento> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             CallableStatement cs = con.prepareCall(SP_BUSCAR)) {
            cs.setString(1, texto);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Busca un alimento por id (activo o no); vacío si no existe.
     * Usa: sp_alimento_buscar_por_id
     */
    public Optional<Alimento> buscarPorId(int id) throws SQLException {
        try (Connection con = Conexion.obtener();
             CallableStatement cs = con.prepareCall(SP_BUSCAR_POR_ID)) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Guarda un alimento nuevo y devuelve el id generado.
     * Usa: sp_alimento_insertar
     */
    public int insertar(Alimento alimento, int creadoPor) throws SQLException {
        Macros m = alimento.getPor100g();
        try (Connection con = Conexion.obtener();
             CallableStatement cs = con.prepareCall(SP_INSERTAR)) {
            cs.setString(1, alimento.getNombre());
            cs.setDouble(2, m.getKcal());
            cs.setDouble(3, m.getProteina());
            cs.setDouble(4, m.getCarbohidratos());
            cs.setDouble(5, m.getGrasas());
            cs.setInt(6, creadoPor);
            cs.registerOutParameter(7, Types.INTEGER);
            cs.execute();
            return cs.getInt(7);
        }
    }

    /**
     * Actualiza nombre y macros (por 100 g) de un alimento existente.
     * Usa: sp_alimento_actualizar
     */
    public void actualizar(Alimento alimento) throws SQLException {
        Macros m = alimento.getPor100g();
        try (Connection con = Conexion.obtener();
             CallableStatement cs = con.prepareCall(SP_ACTUALIZAR)) {
            cs.setInt(1, alimento.getId());
            cs.setString(2, alimento.getNombre());
            cs.setDouble(3, m.getKcal());
            cs.setDouble(4, m.getProteina());
            cs.setDouble(5, m.getCarbohidratos());
            cs.setDouble(6, m.getGrasas());
            cs.execute();
        }
    }

    /**
     * Baja lógica: marca el alimento como no activo.
     * Usa: sp_alimento_desactivar
     */
    public void desactivar(int id) throws SQLException {
        try (Connection con = Conexion.obtener();
             CallableStatement cs = con.prepareCall(SP_DESACTIVAR)) {
            cs.setInt(1, id);
            cs.execute();
        }
    }

    /** Convierte la fila actual del ResultSet en un Alimento. */
    private Alimento mapear(ResultSet rs) throws SQLException {
        Alimento a = new Alimento();
        a.setId(rs.getInt("id"));
        a.setNombre(rs.getString("nombre"));
        a.setPor100g(new Macros(
                rs.getDouble("kcal_100g"),
                rs.getDouble("proteina_100g"),
                rs.getDouble("carbohidratos_100g"),
                rs.getDouble("grasas_100g")));
        a.setIdExterno(rs.getString("id_externo"));
        a.setActivo(rs.getBoolean("activo"));
        return a;
    }
}
