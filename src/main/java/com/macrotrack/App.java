package com.macrotrack;

import com.macrotrack.controller.Navegacion;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Clase principal de MacroTrack.
 * Extiende Application, que es el punto de entrada de toda app JavaFX.
 */
public class App extends Application {

    /**
     * JavaFX llama a este método al iniciar; recibe la ventana principal (Stage).
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("MacroTrack");

        // Ícono de la ventana y de la barra de tareas
        var logo = App.class.getResourceAsStream("/com/macrotrack/logo.png");
        if (logo != null) {
            stage.getIcons().add(new Image(logo));
        }

        // Navegacion se encarga de cargar los .fxml en esta ventana
        Navegacion.iniciar(stage);
        Navegacion.ir(Navegacion.LOGIN);

        stage.show();
    }

    /**
     * Método main: solo lanza la aplicación JavaFX.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
