package com.macrotrack.controller;

import com.macrotrack.service.AuthService;
import com.macrotrack.util.excepciones.MacroTrackException;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    private final AuthService authService = new AuthService();

    @FXML
    private void entrar() {
        try {
            authService.login(txtEmail.getText(), txtPassword.getText());
            Navegacion.ir(Navegacion.DASHBOARD);
        } catch (MacroTrackException e) {
            Navegacion.error(e.getMessage());
            txtPassword.clear();
            txtPassword.requestFocus();
        }
    }

    @FXML
    private void irRegistro() {
        Navegacion.ir(Navegacion.REGISTRO);
    }
}
