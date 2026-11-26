package com.macrotrack.controller;

import com.macrotrack.service.AuthService;
import com.macrotrack.util.excepciones.MacroTrackException;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistroController {
    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtRepetir;
    private final AuthService authService = new AuthService();
    @FXML
    private void crear() {
        // la repeticion la reviso aca porque es cosa de la pantalla nomas
        if (!txtPassword.getText().equals(txtRepetir.getText())) {
            Navegacion.error("Las contraseñas no coinciden");
            return;
        }
        try {
            authService.registrar(txtNombre.getText(), txtEmail.getText(), txtPassword.getText());
            Navegacion.info("Cuenta creada. Ya puedes iniciar sesión");
            Navegacion.ir(Navegacion.LOGIN);
        } catch (MacroTrackException e) {
            Navegacion.error(e.getMessage());
        }
    }
    @FXML
    private void volver() {
        Navegacion.ir(Navegacion.LOGIN);
    }
}
