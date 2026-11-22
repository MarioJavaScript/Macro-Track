package com.macrotrack.dao;

import com.macrotrack.db.Conexion;
import com.macrotrack.model.Alimento;
import com.macrotrack.model.Macros;
import com.macrotrack.model.Registro;
import com.macrotrack.model.TipoComida;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acceso a la tabla registro mediante procedimientos almacenados.
 */
public class RegistroDAO {

    private static final String SP_INSERTAR = "{call sp_registro_insertar(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
    private static final String SP_POR_FECHA = "{call sp_registro_por_fecha(?, ?)}";
    private static final String SP_BUSCAR_POR_ID = "{call sp_registro_buscar_por_id(?)}";
    private static final String SP_ACTUALIZAR = "{call sp_registro_actualizar(?, ?, ?, ?, ?, ?)}";
    private static final String SP_ELIMINAR = "{call sp_registro_eliminar(?)}";
    private static final String SP_TOTALES_RANGO = "{call sp_registro_totales_rango(?, ?, ?)}";

    /**
     * Guarda un registro nuevo y devuelve el id generado.
     * Usa: sp_registro_insertar
     */
    public int insertar(Registro registro) throws SQLException {
        Macros m = registro.getMacros();
        try (Connection con = Conexion.obtener();
             CallableStatement cs = con.prepareCall(SP_INSERTAR)) {
            cs.setInt(1, registro.getUsuarioId());
            cs.setInt(2, registro.getAlimento().getId());
            cs.setDate(3, Date.valueOf(registro.getFecha()));
            cs.setString(4, registro.getTipoComida().name());
            cs.setDouble(5, registro.getGramos());
            cs.setDouble(6, m.getKcal());
            cs.setDouble(7, m.getProteina());
            cs.setDouble(8, m.getCarbohidratos());
            cs.setDouble(9, m.getGrasas());
            cs.registerOutParameter(10, Types.INTEGER);
            cs.execute();
            return cs.getInt(10);
        }
    }

    /**
     * Lista los registros de un usuario en una fecha (con su alimento).
     * Usa: sp_registro_por_fecha
     */
    public List<Registro> listarPorFecha(int usuarioId, LocalDate fecha) throws SQLException {
        List<Registro> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             CallableStatement cs = con.prepareCall(SP_POR_FECHA)) {
            cs.setInt(1, usuarioId);
            cs.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Busca un registro por id (con su alimento); vacío si no existe.
     * Usa: sp_registro_buscar_por_id
     */
    public Optional<Registro> buscarPorId(int id) throws SQLException {
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
     * Actualiza los gramos y los macros calculados de un registro.
     * Usa: sp_registro_actualizar
     */
    public void actualizar(Registro registro) throws SQLException {
        Macros m = registro.getMacros();
        try (Connection con = Conexion.obtener();
             CallableStatement cs = con.prepareCall(SP_ACTUALIZAR)) {
            cs.setInt(1, registro.getId());
            cs.setDouble(2, registro.getGramos());
            cs.setDouble(3, m.getKcal());
            cs.setDouble(4, m.getProteina());
            cs.setDouble(5, m.getCarbohidratos());
            cs.setDouble(6, m.getGrasas());
            cs.execute();
        }
    }

    /**
     * Elimina un registro.
     * Usa: sp_registro_eliminar
     */
    public void eliminar(int id) throws SQLException {
        try (Connection con = Conexion.obtener();
             CallableStatement cs = con.prepareCall(SP_ELIMINAR)) {
            cs.setInt(1, id);
            cs.execute();
        }
    }

    /**
     * Totales de macros por día en un rango de fechas, ordenados por fecha.
     * Solo aparecen los días que tienen registros.
     * Usa: sp_registro_totales_rango
     */
    public Map<LocalDate, Macros> totalesPorRango(int usuarioId, LocalDate inicio, LocalDate fin)
            throws SQLException {
        // LinkedHashMap conserva el orden en que llegan las filas (ya vienen por fecha)
        Map<LocalDate, Macros> totales = new LinkedHashMap<>();
        try (Connection con = Conexion.obtener();
             CallableStatement cs = con.prepareCall(SP_TOTALES_RANGO)) {
            cs.setInt(1, usuarioId);
            cs.setDate(2, Date.valueOf(inicio));
            cs.setDate(3, Date.valueOf(fin));
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    totales.put(rs.getDate("fecha").toLocalDate(), new Macros(
                            rs.getDouble("kcal"),
                            rs.getDouble("proteina_g"),
                            rs.getDouble("carbohidratos_g"),
                            rs.getDouble("grasas_g")));
                }
            }
        }
        return totales;
    }

    /** Convierte la fila actual (registro + alimento) en un Registro con su Alimento. */
    private Registro mapear(ResultSet rs) throws SQLException {
        // Alimento con sus datos por 100 g
        Alimento alimento = new Alimento();
        alimento.setId(rs.getInt("alimento_id"));
        alimento.setNombre(rs.getString("alimento_nombre"));
        alimento.setPor100g(new Macros(
                rs.getDouble("kcal_100g"),
                rs.getDouble("proteina_100g"),
                rs.getDouble("carbohidratos_100g"),
                rs.getDouble("grasas_100g")));
        alimento.setIdExterno(rs.getString("alimento_id_externo"));
        alimento.setActivo(rs.getBoolean("alimento_activo"));

        // Registro con los macros de la porción guardados (snapshot)
        Registro r = new Registro();
        r.setId(rs.getInt("id"));
        r.setUsuarioId(rs.getInt("usuario_id"));
        r.setAlimento(alimento);
        r.setFecha(rs.getDate("fecha").toLocalDate());
        r.setTipoComida(TipoComida.valueOf(rs.getString("tipo_comida")));
        r.setGramos(rs.getDouble("gramos"));
        r.setMacros(new Macros(
                rs.getDouble("kcal"),
                rs.getDouble("proteina_g"),
                rs.getDouble("carbohidratos_g"),
                rs.getDouble("grasas_g")));
        return r;
    }
}
