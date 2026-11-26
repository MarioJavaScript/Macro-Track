package com.macrotrack.controller;

import com.macrotrack.model.Alimento;
import com.macrotrack.model.Macros;
import com.macrotrack.service.AlimentoService;
import com.macrotrack.util.Sesion;
import com.macrotrack.util.excepciones.MacroTrackException;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.function.ToDoubleFunction;

import static com.macrotrack.controller.DashboardController.formato;

/**
 * Panel de administrador para el catálogo de alimentos.
 * Usa AlimentoService: buscar, crear, actualizar y darDeBaja (estos tres solo para ADMIN).
 */
public class AdminAlimentosController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Alimento> tblAlimentos;
    @FXML private TableColumn<Alimento, String> colNombre;
    @FXML private TableColumn<Alimento, String> colKcal;
    @FXML private TableColumn<Alimento, String> colProteina;
    @FXML private TableColumn<Alimento, String> colCarbos;
    @FXML private TableColumn<Alimento, String> colGrasas;

    @FXML private Label lblModo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtKcal;
    @FXML private TextField txtProteina;
    @FXML private TextField txtCarbos;
    @FXML private TextField txtGrasas;
    @FXML private Label lblSumaMacros;
    @FXML private Button btnBaja;

    private final AlimentoService alimentoService = new AlimentoService();
    private final PauseTransition esperaBusqueda = new PauseTransition(Duration.millis(300));

    /** Alimento que se está editando; null cuando el formulario es para uno nuevo. */
    private Alimento editando;

    @FXML
    private void initialize() {
        // Por si alguien llega aquí sin ser admin
        if (!Sesion.actual().esAdmin()) {
            Navegacion.error("Solo un administrador puede entrar a esta pantalla");
            javafx.application.Platform.runLater(() -> Navegacion.ir(Navegacion.DASHBOARD));
            return;
        }

        // Qué muestra cada columna
        colNombre.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getNombre()));
        enlazar(colKcal, Macros::getKcal);
        enlazar(colProteina, Macros::getProteina);
        enlazar(colCarbos, Macros::getCarbohidratos);
        enlazar(colGrasas, Macros::getGrasas);

        tblAlimentos.setPlaceholder(new Label("Busca un alimento para verlo aquí"));

        // Al seleccionar una fila se carga en el formulario para editarla
        tblAlimentos.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, nuevo) -> {
                    if (nuevo != null) {
                        cargarEnFormulario(nuevo);
                    }
                });

        esperaBusqueda.setOnFinished(e -> buscar());
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> esperaBusqueda.playFromStart());

        tblAlimentos.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE) {
                darDeBaja();
            }
        });

        txtProteina.textProperty().addListener((obs, anterior, nuevo) -> actualizarSumaMacros());
        txtCarbos.textProperty().addListener((obs, anterior, nuevo) -> actualizarSumaMacros());
        txtGrasas.textProperty().addListener((obs, anterior, nuevo) -> actualizarSumaMacros());

        nuevo();
    }

    /** Columna numérica: toma un valor de los macros por 100 g y lo muestra con formato corto. */
    private void enlazar(TableColumn<Alimento, String> columna, ToDoubleFunction<Macros> valor) {
        columna.setCellValueFactory(fila -> new SimpleStringProperty(
                formato(valor.applyAsDouble(fila.getValue().getPor100g()))));
        columna.setStyle("-fx-alignment: CENTER-RIGHT;");
    }

    private void buscar() {
        try {
            tblAlimentos.getItems().setAll(alimentoService.buscar(txtBuscar.getText()));
        } catch (MacroTrackException e) {
            Navegacion.error(e.getMessage());
        }
    }

    // ---------------------------------------------------------------- formulario

    private void cargarEnFormulario(Alimento a) {
        editando = a;
        lblModo.setText("Editando: " + a.getNombre());
        txtNombre.setText(a.getNombre());
        txtKcal.setText(formato(a.getPor100g().getKcal()));
        txtProteina.setText(formato(a.getPor100g().getProteina()));
        txtCarbos.setText(formato(a.getPor100g().getCarbohidratos()));
        txtGrasas.setText(formato(a.getPor100g().getGrasas()));
        btnBaja.setDisable(false);
    }

    /** Limpia el formulario para dar de alta un alimento nuevo. */
    @FXML
    private void nuevo() {
        editando = null;
        tblAlimentos.getSelectionModel().clearSelection();
        lblModo.setText("Nuevo alimento");
        txtNombre.clear();
        txtKcal.clear();
        txtProteina.clear();
        txtCarbos.clear();
        txtGrasas.clear();
        btnBaja.setDisable(true);
        actualizarSumaMacros();
        txtNombre.requestFocus();
    }

    /** Actualiza el indicador de suma de macros (P + C + G) en vivo y marca en rojo si pasa de 100 g. */
    private void actualizarSumaMacros() {
        double suma = sumarMacros(txtProteina.getText(), txtCarbos.getText(), txtGrasas.getText());
        boolean excedido = suma > 100;
        lblSumaMacros.getStyleClass().removeAll("texto-suave", "texto-peligro");
        if (excedido) {
            lblSumaMacros.getStyleClass().add("texto-peligro");
            lblSumaMacros.setText("Suma P + C + G: " + formato(suma) + " / 100 g (excede 100 g)");
        } else {
            lblSumaMacros.getStyleClass().add("texto-suave");
            lblSumaMacros.setText("Suma P + C + G: " + formato(suma) + " / 100 g");
        }
    }

    /** Suma los valores numéricos válidos de proteína, carbohidratos y grasas (vacíos o inválidos cuentan como 0). */
    static double sumarMacros(String proteina, String carbos, String grasas) {
        return parsearOCero(proteina) + parsearOCero(carbos) + parsearOCero(grasas);
    }

    private static double parsearOCero(String texto) {
        if (texto == null || texto.isBlank()) {
            return 0;
        }
        try {
            double valor = Double.parseDouble(texto.trim().replace(',', '.'));
            return valor > 0 ? valor : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Crea o actualiza según si hay un alimento seleccionado. */
    @FXML
    private void guardar() {
        try {
            Macros por100g = new Macros(
                    Navegacion.numero(txtKcal.getText(), "Las calorías"),
                    Navegacion.numero(txtProteina.getText(), "La proteína"),
                    Navegacion.numero(txtCarbos.getText(), "Los carbohidratos"),
                    Navegacion.numero(txtGrasas.getText(), "Las grasas"));

            Alimento guardado;
            if (editando == null) {
                guardado = alimentoService.crear(txtNombre.getText(), por100g);
                Navegacion.info("Se agregó \"" + guardado.getNombre() + "\" al catálogo");
            } else {
                guardado = alimentoService.actualizar(editando.getId(), txtNombre.getText(), por100g);
                Navegacion.info("Se actualizó \"" + guardado.getNombre() + "\"");
            }

            // Mostrar el alimento guardado en la tabla
            txtBuscar.setText(guardado.getNombre());
            esperaBusqueda.stop();
            buscar();
            nuevo();
        } catch (MacroTrackException e) {
            // Validaciones del service: rangos, suma > 100, nombre vacío, etc.
            Navegacion.error(e.getMessage());
        }
    }

    @FXML
    private void darDeBaja() {
        if (editando == null) {
            return;
        }
        if (!Navegacion.confirmar("¿Dar de baja \"" + editando.getNombre() + "\"?\n"
                + "Ya no aparecerá en las búsquedas, pero los registros que lo usan se conservan.")) {
            return;
        }
        try {
            alimentoService.darDeBaja(editando.getId());
            buscar();
            nuevo();
        } catch (MacroTrackException e) {
            Navegacion.error(e.getMessage());
        }
    }

    @FXML
    private void volver() {
        esperaBusqueda.stop();
        Navegacion.ir(Navegacion.DASHBOARD);
    }
}
