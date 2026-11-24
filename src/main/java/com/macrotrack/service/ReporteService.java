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
        try {
            // La consulta solo trae los días que tienen registros
            Map<LocalDate, Macros> totales =
                    registroDAO.totalesPorRango(usuarioId, inicio, inicio.plusDays(DIAS_SEMANA - 1));
            return rellenarSemana(inicio, totales);
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /**
     * Promedio diario de los 7 días que empiezan en la fecha dada,
     * contando solo los días que tienen registros.
     *
     * @return el promedio; Macros.CERO si ningún día tiene registros
     */
    public Macros promedioDiario(LocalDate inicio) {
        return promediar(semana(inicio));
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
     * Promedio de los días que tienen datos. Un día con todos los macros en cero se considera vacío
     * y no cuenta. Es static para poder probarla sin base de datos.
     *
     * @return el promedio redondeado a 1 decimal; Macros.CERO si no hay ningún día con datos
     */
    public static Macros promediar(Map<LocalDate, Macros> dias) {
        Macros suma = Macros.CERO;
        int diasConDatos = 0;
        for (Macros dia : dias.values()) {
            if (!estaVacio(dia)) {
                suma = suma.sumar(dia);
                diasConDatos++;
            }
        }
        if (diasConDatos == 0) {
            return Macros.CERO;
        }
        return suma.escalar(1.0 / diasConDatos);
    }

    /** Un día sin registros llega con todos los valores en cero. */
    private static boolean estaVacio(Macros m) {
        return m.getKcal() == 0 && m.getProteina() == 0
                && m.getCarbohidratos() == 0 && m.getGrasas() == 0;
    }
}
