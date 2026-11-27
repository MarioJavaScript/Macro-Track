package com.macrotrack.service;

import com.macrotrack.dao.AlimentoDAO;
import com.macrotrack.model.Alimento;
import com.macrotrack.model.Macros;
import com.macrotrack.util.Sesion;
import com.macrotrack.util.Validar;
import com.macrotrack.util.excepciones.AccesoDenegadoException;
import com.macrotrack.util.excepciones.ErrorBDException;
import com.macrotrack.util.excepciones.NoEncontradoException;
import com.macrotrack.util.excepciones.ValidacionException;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Catálogo de alimentos. Buscar y consultar es para cualquier usuario con sesión;
 * crear, editar y dar de baja es solo para administradores.
 */
public class AlimentoService {

    private static final int MIN_CARACTERES_BUSQUEDA = 2;
    private static final int MAX_NOMBRE = 120;
    private static final double MAX_KCAL_100G = 900;
    private static final double MAX_GRAMOS_100G = 100;

    private final AlimentoDAO alimentoDAO;

    public AlimentoService() {
        this.alimentoDAO = new AlimentoDAO();
    }

    /**
     * Devuelve el catálogo de alimentos activos, ordenado por nombre (máximo 50).
     * Se usa para mostrar la lista completa antes de que el usuario escriba.
     */
    public List<Alimento> listarTodos() {
        Sesion.actual();
        try {
            // El procedimiento usa LIKE '%texto%': con texto vacío trae todos los activos
            return alimentoDAO.buscar("");
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /**
     * Busca alimentos activos cuyo nombre contenga el texto (máximo 50 resultados).
     *
     * @return la lista encontrada; vacía si el texto tiene menos de 2 caracteres
     */
    public List<Alimento> buscar(String texto) {
        Sesion.actual();
        if (texto == null || texto.trim().length() < MIN_CARACTERES_BUSQUEDA) {
            return Collections.emptyList();
        }
        try {
            return alimentoDAO.buscar(texto.trim());
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /**
     * Devuelve un alimento por su id.
     *
     * @throws NoEncontradoException si no existe
     */
    public Alimento obtener(int id) {
        Sesion.actual();
        try {
            return alimentoDAO.buscarPorId(id)
                    .orElseThrow(() -> new NoEncontradoException("El alimento no existe"));
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /**
     * Crea un alimento nuevo en el catálogo (solo ADMIN).
     *
     * @param por100g calorías y macros por cada 100 g
     * @return el alimento creado, con su id
     * @throws AccesoDenegadoException si el usuario no es administrador
     * @throws ValidacionException     si el nombre o los macros no son válidos
     */
    public Alimento crear(String nombre, Macros por100g) {
        int adminId = exigirAdmin();
        String nombreLimpio = validarDatos(nombre, por100g);

        Alimento alimento = new Alimento();
        alimento.setNombre(nombreLimpio);
        alimento.setPor100g(por100g);
        alimento.setActivo(true);
        try {
            alimento.setId(alimentoDAO.insertar(alimento, adminId));
            return alimento;
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /**
     * Cambia el nombre y los macros de un alimento existente (solo ADMIN).
     *
     * @return el alimento actualizado
     * @throws AccesoDenegadoException si el usuario no es administrador
     * @throws ValidacionException     si el nombre o los macros no son válidos
     * @throws NoEncontradoException   si el alimento no existe
     */
    public Alimento actualizar(int id, String nombre, Macros por100g) {
        exigirAdmin();
        String nombreLimpio = validarDatos(nombre, por100g);

        try {
            Alimento alimento = alimentoDAO.buscarPorId(id)
                    .orElseThrow(() -> new NoEncontradoException("El alimento no existe"));
            alimento.setNombre(nombreLimpio);
            alimento.setPor100g(por100g);
            alimentoDAO.actualizar(alimento);
            return alimento;
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /**
     * Da de baja un alimento (baja lógica: deja de aparecer en las búsquedas, solo ADMIN).
     * Los registros que ya lo usan se conservan.
     *
     * @throws AccesoDenegadoException si el usuario no es administrador
     * @throws NoEncontradoException   si el alimento no existe
     */
    public void darDeBaja(int id) {
        exigirAdmin();
        try {
            alimentoDAO.buscarPorId(id)
                    .orElseThrow(() -> new NoEncontradoException("El alimento no existe"));
            alimentoDAO.desactivar(id);
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /**
     * Valida el nombre y los macros (por 100 g) de un alimento.
     * Es static para poder probarla sin base de datos.
     *
     * @return el nombre sin espacios sobrantes
     * @throws ValidacionException si el nombre pasa de 120 caracteres, kcal no está entre 0 y 900,
     *                             algún macro no está entre 0 y 100, o proteína + carbohidratos + grasas pasa de 100
     */
    public static String validarDatos(String nombre, Macros por100g) {
        String nombreLimpio = Validar.texto(nombre, "El nombre", MAX_NOMBRE);
        if (por100g == null) {
            throw new ValidacionException("Los macros son obligatorios");
        }

        Validar.rango(por100g.getKcal(), 0, MAX_KCAL_100G, "Las calorías");
        Validar.rango(por100g.getProteina(), 0, MAX_GRAMOS_100G, "La proteína");
        Validar.rango(por100g.getCarbohidratos(), 0, MAX_GRAMOS_100G, "Los carbohidratos");
        Validar.rango(por100g.getGrasas(), 0, MAX_GRAMOS_100G, "Las grasas");

        // Son gramos por cada 100 g de alimento: no pueden sumar más de 100
        double suma = por100g.getProteina() + por100g.getCarbohidratos() + por100g.getGrasas();
        if (suma > MAX_GRAMOS_100G) {
            throw new ValidacionException(
                    "Proteína, carbohidratos y grasas no pueden sumar más de 100 g por cada 100 g");
        }
        return nombreLimpio;
    }

    /**
     * Comprueba que haya sesión y que el usuario sea administrador.
     *
     * @return el id del administrador
     */
    private int exigirAdmin() {
        if (!Sesion.actual().esAdmin()) {
            throw new AccesoDenegadoException("Solo un administrador puede hacer esto");
        }
        return Sesion.actual().getId();
    }
}
