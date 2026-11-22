package com.macrotrack.service;

import com.macrotrack.dao.UsuarioDAO;
import com.macrotrack.model.Usuario;
import com.macrotrack.util.PasswordUtil;
import com.macrotrack.util.Sesion;
import com.macrotrack.util.Validar;
import com.macrotrack.util.excepciones.ErrorBDException;
import com.macrotrack.util.excepciones.ValidacionException;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Registro de usuarios, inicio y cierre de sesión.
 */
public class AuthService {

    private static final int MIN_PASSWORD = 8;
    private static final String SQLSTATE_DUPLICADO = "23000";

    /** Mismo mensaje para cualquier fallo de login, para no revelar si el correo existe. */
    private static final String MSG_LOGIN = "Correo o contraseña incorrectos";

    private final UsuarioDAO usuarioDAO;

    public AuthService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Crea una cuenta nueva. No inicia sesión.
     *
     * @return el usuario creado, con su id (sin el hash, que no se expone)
     * @throws ValidacionException si faltan datos, el correo es inválido o ya existe,
     *                             o la contraseña tiene menos de 8 caracteres
     */
    public Usuario registrar(String nombre, String email, String password) {
        // 1. Validar los datos
        String nombreLimpio = Validar.texto(nombre, "El nombre", 80);
        String emailLimpio = Validar.texto(email, "El correo", 120).toLowerCase();
        Validar.email(emailLimpio);
        if (password == null || password.length() < MIN_PASSWORD) {
            throw new ValidacionException(
                    "La contraseña debe tener al menos " + MIN_PASSWORD + " caracteres");
        }

        try {
            // 2. El correo no debe estar repetido
            if (usuarioDAO.buscarPorEmail(emailLimpio).isPresent()) {
                throw new ValidacionException("Ese correo ya está registrado");
            }

            // 3. Guardar solo el hash, nunca la contraseña
            Usuario usuario = new Usuario(nombreLimpio, emailLimpio, PasswordUtil.hash(password));
            usuario.setId(usuarioDAO.insertar(usuario));

            usuario.setPasswordHash(null);
            return usuario;
        } catch (SQLException e) {
            // Dos registros simultáneos con el mismo correo chocan con el UNIQUE de la BD
            if (SQLSTATE_DUPLICADO.equals(e.getSQLState())) {
                throw new ValidacionException("Ese correo ya está registrado");
            }
            throw new ErrorBDException(e);
        }
    }

    /**
     * Inicia sesión si el correo y la contraseña son correctos.
     *
     * @throws ValidacionException con un mensaje genérico si algo no coincide
     */
    public Usuario login(String email, String password) {
        if (email == null || password == null) {
            throw new ValidacionException(MSG_LOGIN);
        }

        try {
            Optional<Usuario> encontrado = usuarioDAO.buscarPorEmail(email.trim().toLowerCase());

            // No existe, está inactivo o la contraseña no coincide: mismo mensaje en los tres casos
            if (encontrado.isEmpty()
                    || !encontrado.get().isActivo()
                    || !PasswordUtil.verificar(password, encontrado.get().getPasswordHash())) {
                throw new ValidacionException(MSG_LOGIN);
            }

            // El hash ya no se necesita: no se deja en la sesión
            Usuario usuario = encontrado.get();
            usuario.setPasswordHash(null);
            Sesion.iniciar(usuario);
            return usuario;
        } catch (SQLException e) {
            throw new ErrorBDException(e);
        }
    }

    /** Cierra la sesión actual. */
    public void logout() {
        Sesion.cerrar();
    }
}
