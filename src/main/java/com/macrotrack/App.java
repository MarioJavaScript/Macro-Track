package com.macrotrack;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
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
        // Contenedor raíz vacío (aquí se cargarán las pantallas .fxml más adelante)
        StackPane raiz = new StackPane();

        // La escena es el contenido de la ventana: raíz + tamaño 400x700
        Scene escena = new Scene(raiz, 400, 700);

        // Configuramos la ventana y la mostramos
        stage.setTitle("MacroTrack");
        stage.setScene(escena);
        stage.show();
    }

    /**
     * Método main: solo lanza la aplicación JavaFX.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
