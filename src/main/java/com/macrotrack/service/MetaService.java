package com.macrotrack.service;

import com.macrotrack.dao.MetaDAO;
import com.macrotrack.model.Macros;
import com.macrotrack.model.Meta;
import com.macrotrack.util.Sesion;
import com.macrotrack.util.Validar;
import com.macrotrack.util.excepciones.ErrorBDException;
import com.macrotrack.util.excepciones.ValidacionException;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Meta diaria de macros del usuario en sesión.
 */
public class MetaService {

    // Topes razonables; evitan desbordar las columnas DECIMAL de la BD
    private static final double MAX_KCAL = 20000;
    private static final double MAX_GRAMOS = 2000;

    private final MetaDAO metaDAO;

    public MetaService() {
        this.metaDAO = new MetaDAO();
    }

    /** Devuelve la meta del usuario en sesión; vacío si aún no la ha definido. */
    public Optional<Meta> obtener() {
        int usuarioId = Sesion.actual().getId();
        try {
            return metaDAO.buscarPorUsuario(usuarioId);
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /**
     * Crea o actualiza la meta del usuario en sesión.
     *
     * @throws com.macrotrack.util.excepciones.ValidacionException si las kcal no son mayores que 0
     *                                                             o algún macro es negativo
     */
    public void guardar(Macros objetivo) {
        int usuarioId = Sesion.actual().getId();
        if (objetivo == null) {
            throw new ValidacionException("Los macros son obligatorios");
        }

        Validar.positivo(objetivo.getKcal(), "Las calorías");
        Validar.rango(objetivo.getKcal(), 0, MAX_KCAL, "Las calorías");
        validarGramos(objetivo.getProteina(), "La proteína");
        validarGramos(objetivo.getCarbohidratos(), "Los carbohidratos");
        validarGramos(objetivo.getGrasas(), "Las grasas");

        Meta meta = new Meta();
        meta.setUsuarioId(usuarioId);
        meta.setObjetivo(objetivo);
        try {
            metaDAO.guardar(meta);
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /** Un macro debe ser >= 0 (y no exagerado). */
    private void validarGramos(double valor, String campo) {
        Validar.noNegativo(valor, campo);
        Validar.rango(valor, 0, MAX_GRAMOS, campo);
    }
}
