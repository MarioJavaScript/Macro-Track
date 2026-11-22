package com.macrotrack.service;

import com.macrotrack.dao.AlimentoDAO;
import com.macrotrack.dao.MetaDAO;
import com.macrotrack.dao.RegistroDAO;
import com.macrotrack.model.Alimento;
import com.macrotrack.model.Macros;
import com.macrotrack.model.Meta;
import com.macrotrack.model.Registro;
import com.macrotrack.model.ResumenDiario;
import com.macrotrack.model.TipoComida;
import com.macrotrack.util.Sesion;
import com.macrotrack.util.Validar;
import com.macrotrack.util.excepciones.AccesoDenegadoException;
import com.macrotrack.util.excepciones.ErrorBDException;
import com.macrotrack.util.excepciones.NoEncontradoException;
import com.macrotrack.util.excepciones.ValidacionException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Registro de comidas del usuario en sesión y resumen diario.
 */
public class RegistroService {

    private static final double MIN_GRAMOS = 0.1;
    private static final double MAX_GRAMOS = 5000;

    private final RegistroDAO registroDAO;
    private final AlimentoDAO alimentoDAO;
    private final MetaDAO metaDAO;

    public RegistroService() {
        this.registroDAO = new RegistroDAO();
        this.alimentoDAO = new AlimentoDAO();
        this.metaDAO = new MetaDAO();
    }

    /**
     * Registra una porción de un alimento para el usuario en sesión.
     *
     * @return el registro guardado, con su id
     */
    public Registro registrar(int alimentoId, double gramos, TipoComida tipo, LocalDate fecha) {
        int usuarioId = Sesion.actual().getId();

        // 1. Validar lo que mandó el usuario
        validarGramos(gramos);
        if (tipo == null) {
            throw new ValidacionException("Elige el tipo de comida");
        }
        if (fecha == null) {
            throw new ValidacionException("Elige una fecha");
        }

        try {
            // 2. El alimento debe existir y estar activo
            Alimento alimento = alimentoDAO.buscarPorId(alimentoId)
                    .orElseThrow(() -> new NoEncontradoException("El alimento no existe"));
            if (!alimento.isActivo()) {
                throw new ValidacionException("Ese alimento ya no está disponible");
            }

            // 3. El constructor de Registro calcula los macros con Alimento.calcularPara
            Registro registro = new Registro(usuarioId, alimento, fecha, tipo, gramos);
            registro.setId(registroDAO.insertar(registro));
            return registro;
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /**
     * Cambia los gramos de un registro propio y recalcula sus macros.
     *
     * @return el registro actualizado
     */
    public Registro editarGramos(int registroId, double gramos) {
        validarGramos(gramos);
        try {
            Registro registro = buscarPropio(registroId);
            registro.cambiarGramos(gramos);
            registroDAO.actualizar(registro);
            return registro;
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /** Elimina un registro propio. */
    public void eliminar(int registroId) {
        try {
            buscarPropio(registroId);
            registroDAO.eliminar(registroId);
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /**
     * Arma el resumen del día: registros, total consumido, meta y lo que falta.
     * Si el usuario no tiene meta, se usa Macros.CERO.
     */
    public ResumenDiario resumenDelDia(LocalDate fecha) {
        int usuarioId = Sesion.actual().getId();
        if (fecha == null) {
            throw new ValidacionException("Elige una fecha");
        }

        try {
            List<Registro> registros = registroDAO.listarPorFecha(usuarioId, fecha);

            // Sumar los macros de cada registro del día
            Macros consumido = Macros.CERO;
            for (Registro r : registros) {
                consumido = consumido.sumar(r.getMacros());
            }

            Macros meta = metaDAO.buscarPorUsuario(usuarioId)
                    .map(Meta::getObjetivo)
                    .orElse(Macros.CERO);

            return new ResumenDiario(fecha, registros, consumido, meta, meta.restar(consumido));
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /** Gramos entre 0.1 y 5000. */
    private void validarGramos(double gramos) {
        Validar.rango(gramos, MIN_GRAMOS, MAX_GRAMOS, "Los gramos");
    }

    /**
     * Busca un registro y comprueba que sea del usuario en sesión.
     *
     * @throws NoEncontradoException   si no existe
     * @throws AccesoDenegadoException si es de otro usuario
     */
    private Registro buscarPropio(int registroId) throws SQLException {
        int usuarioId = Sesion.actual().getId();
        Registro registro = registroDAO.buscarPorId(registroId)
                .orElseThrow(() -> new NoEncontradoException("El registro no existe"));
        if (registro.getUsuarioId() != usuarioId) {
            throw new AccesoDenegadoException("Ese registro no te pertenece");
        }
        return registro;
    }
}
