package com.macrotrack.controller;

import com.macrotrack.util.excepciones.ValidacionException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**
 * Cambia entre pantallas y muestra mensajes. La usan todos los controladores.
 * Los .fxml viven en src/main/resources/com/macrotrack/view/.
 */
public final class Navegacion {

    // Nombres de las pantallas (archivo .fxml dentro de /com/macrotrack/view/)
    public static final String LOGIN = "login.fxml";
    public static final String REGISTRO = "registro.fxml";
    public static final String DASHBOARD = "dashboard.fxml";
    public static final String REGISTRAR_COMIDA = "registrar-comida.fxml";
    public static final String HISTORIAL = "historial.fxml";
    public static final String ADMIN_ALIMENTOS = "admin-alimentos.fxml";

    private static final String CARPETA_VISTAS = "/com/macrotrack/view/";
    private static final String CSS = "/com/macrotrack/css/estilos.css";

    private static Stage ventana;

    /** Clase de utilidad: no se instancia. */
    private Navegacion() {
    }

    /** Se llama una vez desde App.start() con la ventana principal. */
    public static void iniciar(Stage stage) {
        ventana = stage;
    }

    /**
     * Carga una pantalla .fxml, la pone en la ventana y devuelve su controlador
     * (por si hay que pasarle datos, por ejemplo una fecha).
     *
     * @return el controlador de la pantalla, o null si no se pudo cargar
     */
    public static <T> T ir(String fxml) {
        URL url = Navegacion.class.getResource(CARPETA_VISTAS + fxml);
        if (url == null) {
            error("La pantalla " + fxml + " todavía no existe");
            return null;
        }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent raiz = loader.load();

            Scene escena = new Scene(raiz);
            URL css = Navegacion.class.getResource(CSS);
            if (css != null) {
                escena.getStylesheets().add(css.toExternalForm());
            }
            ventana.setScene(escena);
            ventana.sizeToScene();
            ventana.centerOnScreen();
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            error("No se pudo abrir la pantalla " + fxml);
            return null;
        }
    }

    /** Muestra un mensaje de error. */
    public static void error(String mensaje) {
        mostrar(Alert.AlertType.ERROR, "Error", mensaje);
    }

    /** Muestra un mensaje informativo. */
    public static void info(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "MacroTrack", mensaje);
    }

    /** Pregunta Sí/No. Devuelve true si el usuario aceptó. */
    public static boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar");
        alerta.setHeaderText(null);
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.YES;
    }

    /**
     * Convierte el texto de un TextField a número. Acepta coma o punto decimal.
     *
     * @throws ValidacionException si está vacío o no es un número
     */
    public static double numero(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new ValidacionException(campo + " es obligatorio");
        }
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new ValidacionException(campo + " debe ser un número");
        }
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
