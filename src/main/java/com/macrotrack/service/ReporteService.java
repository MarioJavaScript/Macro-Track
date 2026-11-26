package com.macrotrack.service;

import com.macrotrack.dao.RegistroDAO;
import com.macrotrack.model.Macros;
import com.macrotrack.util.Sesion;
import com.macrotrack.util.Validar;
import com.macrotrack.util.excepciones.ErrorBDException;
import com.macrotrack.util.excepciones.ValidacionException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reportes semanales de macros del usuario en sesión.
 */
public class ReporteService {

    private static final int DIAS_SEMANA = 7;

    private final RegistroDAO registroDAO;

    public ReporteService() {
        this.registroDAO = new RegistroDAO();
    }

    /**
     * Totales de macros de los 7 días que empiezan en la fecha dada.
     *
     * @return mapa ordenado por fecha con los 7 días; los días sin registros traen Macros.CERO
     * @throws ValidacionException si la fecha es nula
     */
    public Map<LocalDate, Macros> semana(LocalDate inicio) {
        int usuarioId = Sesion.actual().getId();
        if (inicio == null) {
            throw new ValidacionException("Elige una fecha de inicio");
        }
        return rellenarSemana(inicio, totalesConRegistros(usuarioId, inicio));
    }

    /**
     * Promedio diario de los 7 días que empiezan en la fecha dada,
     * contando solo los días que tienen registros (aunque sumen 0 kcal).
     *
     * @return el promedio; Macros.CERO si ningún día tiene registros
     * @throws ValidacionException si la fecha es nula
     */
    public Macros promedioDiario(LocalDate inicio) {
        int usuarioId = Sesion.actual().getId();
        if (inicio == null) {
            throw new ValidacionException("Elige una fecha de inicio");
        }
        // Se promedia lo que trae la consulta, antes de rellenar con ceros
        return promediar(totalesConRegistros(usuarioId, inicio));
    }

    /** Totales tal como vienen de la consulta: solo los días que tienen registros. */
    private Map<LocalDate, Macros> totalesConRegistros(int usuarioId, LocalDate inicio) {
        try {
            return registroDAO.totalesPorRango(usuarioId, inicio, inicio.plusDays(DIAS_SEMANA - 1));
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /**
     * Completa los 7 días desde inicio: toma los totales que existan y pone Macros.CERO en el resto.
     * Es static para poder probarla sin base de datos.
     */
    public static Map<LocalDate, Macros> rellenarSemana(LocalDate inicio, Map<LocalDate, Macros> totales) {
        // LinkedHashMap conserva el orden cronológico
        Map<LocalDate, Macros> semana = new LinkedHashMap<>();
        for (int i = 0; i < DIAS_SEMANA; i++) {
            LocalDate dia = inicio.plusDays(i);
            semana.put(dia, totales.getOrDefault(dia, Macros.CERO));
        }
        return semana;
    }

    /**
     * Promedio de los días que vienen de la consulta SQL (los que tienen registros).
     * Acepta el mapa de la consulta o la semana ya rellenada: los días de relleno son la
     * misma instancia Macros.CERO que pone rellenarSemana y no cuentan. Un día con registros
     * que suma 0 kcal sí cuenta, porque la consulta lo trae como un Macros nuevo.
     * Es static para poder probarla sin base de datos.
     *
     * @return el promedio redondeado a 1 decimal; Macros.CERO si no hay ningún día con registros
     */
    public static Macros promediar(Map<LocalDate, Macros> dias) {
        Macros suma = Macros.CERO;
        int diasConRegistros = 0;
        for (Macros dia : dias.values()) {
            // Comparación por referencia a propósito: solo descarta el relleno de rellenarSemana
            if (dia != Macros.CERO) {
                suma = suma.sumar(dia);
                diasConRegistros++;
            }
        }
        if (diasConRegistros == 0) {
            return Macros.CERO;
        }
        return suma.escalar(1.0 / diasConRegistros);
    }
}
