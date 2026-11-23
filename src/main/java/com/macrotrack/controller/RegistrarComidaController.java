package com.macrotrack.controller;

import com.macrotrack.model.Alimento;
import com.macrotrack.model.Macros;
import com.macrotrack.model.TipoComida;
import com.macrotrack.service.AlimentoService;
import com.macrotrack.service.RegistroService;
import com.macrotrack.util.excepciones.MacroTrackException;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.macrotrack.controller.DashboardController.formato;

/**
 * Pantalla para registrar una porción de comida.
 * Usa AlimentoService.buscar() para el buscador y RegistroService.registrar() para guardar.
 */
public class RegistrarComidaController {

    @FXML private TextField txtBuscar;
    @FXML private ListView<Alimento> lstResultados;
    @FXML private Label lblAlimento;
    @FXML private Label lblPor100g;
    @FXML private TextField txtGramos;
    @FXML private ComboBox<TipoComida> cmbTipo;
    @FXML private DatePicker dpFecha;
    @FXML private Label lblVistaPrevia;
    @FXML private Button btnGuardar;

    private final AlimentoService alimentoService = new AlimentoService();
    private final RegistroService registroService = new RegistroService();

    /** Espera 300 ms después de la última tecla antes de buscar, para no consultar la BD en cada letra. */
    private final PauseTransition esperaBusqueda = new PauseTransition(Duration.millis(300));

    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(TipoComida.values());
        cmbTipo.setValue(tipoSegunHora());
        dpFecha.setValue(LocalDate.now());

        lstResultados.setPlaceholder(new Label("Escribe para buscar alimentos"));

        // Buscador: reinicia la espera con cada tecla
        esperaBusqueda.setOnFinished(e -> buscar());
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> esperaBusqueda.playFromStart());

        // Al elegir un alimento o cambiar los gramos se actualiza la vista previa
        lstResultados.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, nuevo) -> mostrarAlimento(nuevo));
        txtGramos.textProperty().addListener((obs, anterior, nuevo) -> actualizarVistaPrevia());

        btnGuardar.setDisable(true);
    }

    /** El dashboard le pasa la fecha que estaba viendo. */
    public void setFecha(LocalDate fecha) {
        if (fecha != null) {
            dpFecha.setValue(fecha);
        }
    }

    private void buscar() {
        String texto = txtBuscar.getText();
        try {
            lstResultados.getItems().setAll(alimentoService.buscar(texto));
            if (texto != null && texto.trim().length() >= 2 && lstResultados.getItems().isEmpty()) {
                lstResultados.setPlaceholder(new Label("No se encontró \"" + texto.trim() + "\""));
            } else {
                lstResultados.setPlaceholder(new Label("Escribe para buscar alimentos"));
            }
        } catch (MacroTrackException e) {
            Navegacion.error(e.getMessage());
        }
    }

    private void mostrarAlimento(Alimento alimento) {
        if (alimento == null) {
            lblAlimento.setText("Ningún alimento seleccionado");
            lblPor100g.setText("Elige uno de la lista");
        } else {
            lblAlimento.setText(alimento.getNombre());
            lblPor100g.setText("Por 100 g: " + alimento.getPor100g());
            txtGramos.requestFocus();
        }
        actualizarVistaPrevia();
    }

    /** Calcula los macros de la porción con Alimento.calcularPara() sin tocar la BD. */
    private void actualizarVistaPrevia() {
        Alimento alimento = lstResultados.getSelectionModel().getSelectedItem();
        Double gramos = gramosEscritos();

        if (alimento == null || gramos == null || gramos <= 0) {
            lblVistaPrevia.setText("—");
            btnGuardar.setDisable(true);
            return;
        }
        Macros porcion = alimento.calcularPara(gramos);
        lblVistaPrevia.setText(formato(porcion.getKcal()) + " kcal  |  P " + formato(porcion.getProteina())
                + " g  C " + formato(porcion.getCarbohidratos())
                + " g  G " + formato(porcion.getGrasas()) + " g");
        btnGuardar.setDisable(false);
    }

    /** Devuelve los gramos escritos o null si todavía no es un número. */
    private Double gramosEscritos() {
        try {
            return Navegacion.numero(txtGramos.getText(), "Los gramos");
        } catch (MacroTrackException e) {
            return null;
        }
    }

    @FXML
    private void guardar() {
        Alimento alimento = lstResultados.getSelectionModel().getSelectedItem();
        if (alimento == null) {
            Navegacion.error("Selecciona un alimento");
            return;
        }
        try {
            double gramos = Navegacion.numero(txtGramos.getText(), "Los gramos");
            registroService.registrar(alimento.getId(), gramos, cmbTipo.getValue(), dpFecha.getValue());

            // Volver al dashboard mostrando el día en que se registró
            DashboardController dashboard = Navegacion.ir(Navegacion.DASHBOARD);
            if (dashboard != null) {
                dashboard.setFecha(dpFecha.getValue());
            }
        } catch (MacroTrackException e) {
            // Gramos fuera de rango, alimento dado de baja, error de BD, etc.
            Navegacion.error(e.getMessage());
        }
    }

    @FXML
    private void volver() {
        esperaBusqueda.stop();
        Navegacion.ir(Navegacion.DASHBOARD);
    }

    /** Sugiere el tipo de comida según la hora actual. */
    private static TipoComida tipoSegunHora() {
        int hora = LocalTime.now().getHour();
        if (hora < 11) {
            return TipoComida.DESAYUNO;
        } else if (hora < 17) {
            return TipoComida.COMIDA;
        } else if (hora < 22) {
            return TipoComida.CENA;
        }
        return TipoComida.SNACK;
    }
}
