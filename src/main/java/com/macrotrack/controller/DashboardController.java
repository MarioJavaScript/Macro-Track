package com.macrotrack.controller;

import com.macrotrack.model.Macros;
import com.macrotrack.model.Meta;
import com.macrotrack.model.Registro;
import com.macrotrack.model.ResumenDiario;
import com.macrotrack.service.AuthService;
import com.macrotrack.service.MetaService;
import com.macrotrack.service.RegistroService;
import com.macrotrack.util.Sesion;
import com.macrotrack.util.excepciones.MacroTrackException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

/**
 * Pantalla principal: muestra lo consumido contra la meta del día y la lista de comidas.
 * Usa RegistroService (resumen, editar, eliminar) y MetaService (editar meta).
 */
public class DashboardController {

    @FXML private Label lblSaludo;
    @FXML private Button btnAdmin;
    @FXML private DatePicker dpFecha;

    @FXML private Label lblKcal;
    @FXML private Label lblKcalMeta;
    @FXML private Label lblKcalRestan;
    @FXML private ProgressBar barKcal;
    @FXML private Label lblProteina;
    @FXML private ProgressBar barProteina;
    @FXML private Label lblCarbos;
    @FXML private ProgressBar barCarbos;
    @FXML private Label lblGrasas;
    @FXML private ProgressBar barGrasas;
    @FXML private Label lblSinMeta;

    @FXML private ListView<Registro> lstRegistros;

    private final RegistroService registroService = new RegistroService();
    private final MetaService metaService = new MetaService();
    private final AuthService authService = new AuthService();

    /** JavaFX lo llama solo después de cargar el .fxml. */
    @FXML
    private void initialize() {
        lblSaludo.setText("Hola, " + Sesion.actual().getNombre());

        // El botón de admin solo se ve (y ocupa espacio) si el usuario es ADMIN
        boolean admin = Sesion.actual().esAdmin();
        btnAdmin.setVisible(admin);
        btnAdmin.setManaged(admin);

        lstRegistros.setCellFactory(lista -> new CeldaRegistro());
        lstRegistros.setPlaceholder(new Label("No has registrado comidas este día"));

        dpFecha.setValue(LocalDate.now());
        cargarResumen();

        // Cada vez que cambia la fecha (calendario o flechas) se recarga el resumen
        dpFecha.valueProperty().addListener((obs, anterior, nueva) -> cambiarFecha());
    }

    /** Lo usa RegistrarComidaController al volver, para quedarse en la fecha que se registró. */
    public void setFecha(LocalDate fecha) {
        if (fecha != null) {
            dpFecha.setValue(fecha); // si cambia, el listener recarga
        }
    }

    // ---------------------------------------------------------------- resumen

    /** Pide el resumen del día al service y llena barras y lista. */
    private void cargarResumen() {
        try {
            ResumenDiario resumen = registroService.resumenDelDia(dpFecha.getValue());
            mostrarMacros(resumen.getConsumido(), resumen.getMeta());

            // Ordenar por tipo de comida (desayuno, comida, cena, snack)
            lstRegistros.getItems().setAll(resumen.getRegistros());
            lstRegistros.getItems().sort(Comparator.comparing(Registro::getTipoComida));
        } catch (MacroTrackException e) {
            Navegacion.error(e.getMessage());
        }
    }

    private void mostrarMacros(Macros consumido, Macros meta) {
        boolean tieneMeta = meta.getKcal() > 0;
        lblSinMeta.setVisible(!tieneMeta);
        lblSinMeta.setManaged(!tieneMeta);

        lblKcal.setText(formato(consumido.getKcal()));
        lblKcalMeta.setText("/ " + formato(meta.getKcal()) + " kcal");
        double restan = meta.getKcal() - consumido.getKcal();
        if (!tieneMeta) {
            lblKcalRestan.setText("");
        } else if (restan >= 0) {
            lblKcalRestan.setText("Restan " + formato(restan) + " kcal");
        } else {
            lblKcalRestan.setText("Te pasaste " + formato(-restan) + " kcal");
        }
        barKcal.setProgress(ResumenDiario.porcentaje(consumido.getKcal(), meta.getKcal()) / 100);

        mostrarMacro(lblProteina, barProteina, consumido.getProteina(), meta.getProteina());
        mostrarMacro(lblCarbos, barCarbos, consumido.getCarbohidratos(), meta.getCarbohidratos());
        mostrarMacro(lblGrasas, barGrasas, consumido.getGrasas(), meta.getGrasas());
    }

    private void mostrarMacro(Label etiqueta, ProgressBar barra, double consumido, double meta) {
        etiqueta.setText(formato(consumido) + " / " + formato(meta) + " g");
        barra.setProgress(ResumenDiario.porcentaje(consumido, meta) / 100);
    }

    // ---------------------------------------------------------------- fecha

    private void cambiarFecha() {
        if (dpFecha.getValue() == null) {
            dpFecha.setValue(LocalDate.now());
        }
        cargarResumen();
    }

    @FXML
    private void diaAnterior() {
        dpFecha.setValue(dpFecha.getValue().minusDays(1)); // el listener recarga el resumen
    }

    @FXML
    private void diaSiguiente() {
        dpFecha.setValue(dpFecha.getValue().plusDays(1));
    }

    @FXML
    private void irHoy() {
        dpFecha.setValue(LocalDate.now());
    }

    // ---------------------------------------------------------------- registros

    @FXML
    private void editarGramos() {
        Registro seleccionado = registroSeleccionado();
        if (seleccionado == null) {
            return;
        }

        TextInputDialog dialogo = new TextInputDialog(formato(seleccionado.getGramos()));
        dialogo.setTitle("Editar gramos");
        dialogo.setHeaderText(seleccionado.getAlimento().getNombre());
        dialogo.setContentText("Gramos:");

        Optional<String> respuesta = dialogo.showAndWait();
        if (respuesta.isEmpty()) {
            return;
        }
        try {
            double gramos = Navegacion.numero(respuesta.get(), "Los gramos");
            registroService.editarGramos(seleccionado.getId(), gramos);
            cargarResumen();
        } catch (MacroTrackException e) {
            Navegacion.error(e.getMessage());
        }
    }

    @FXML
    private void eliminarRegistro() {
        Registro seleccionado = registroSeleccionado();
        if (seleccionado == null) {
            return;
        }
        if (!Navegacion.confirmar("¿Eliminar " + seleccionado.getAlimento().getNombre() + "?")) {
            return;
        }
        try {
            registroService.eliminar(seleccionado.getId());
            cargarResumen();
        } catch (MacroTrackException e) {
            Navegacion.error(e.getMessage());
        }
    }

    private Registro registroSeleccionado() {
        Registro r = lstRegistros.getSelectionModel().getSelectedItem();
        if (r == null) {
            Navegacion.info("Primero selecciona una comida de la lista");
        }
        return r;
    }

    // ---------------------------------------------------------------- meta

    /** Abre un diálogo con los 4 campos de la meta y la guarda con MetaService. */
    @FXML
    private void editarMeta() {
        Macros actual;
        try {
            actual = metaService.obtener().map(Meta::getObjetivo).orElse(Macros.CERO);
        } catch (MacroTrackException e) {
            Navegacion.error(e.getMessage());
            return;
        }

        TextField txtKcal = campo(actual.getKcal());
        TextField txtProteina = campo(actual.getProteina());
        TextField txtCarbos = campo(actual.getCarbohidratos());
        TextField txtGrasas = campo(actual.getGrasas());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Calorías (kcal)"), txtKcal);
        grid.addRow(1, new Label("Proteína (g)"), txtProteina);
        grid.addRow(2, new Label("Carbohidratos (g)"), txtCarbos);
        grid.addRow(3, new Label("Grasas (g)"), txtGrasas);

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Meta diaria");
        dialogo.setHeaderText("¿Cuánto quieres consumir al día?");
        dialogo.getDialogPane().setContent(new VBox(grid));
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // valido al tocar aceptar, y si hay error no cierro el dialogo
        Button btnOk = (Button) dialogo.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Macros nueva = new Macros(
                        Navegacion.numero(txtKcal.getText(), "Las calorías"),
                        Navegacion.numero(txtProteina.getText(), "La proteína"),
                        Navegacion.numero(txtCarbos.getText(), "Los carbohidratos"),
                        Navegacion.numero(txtGrasas.getText(), "Las grasas"));
                metaService.guardar(nueva);
                cargarResumen();
            } catch (MacroTrackException e) {
                Navegacion.error(e.getMessage());
                event.consume(); // dejo el dialogo abierto con lo escrito
            }
        });
        Optional<ButtonType> respuesta = dialogo.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }
    }

    private TextField campo(double valor) {
        TextField txt = new TextField(valor > 0 ? formato(valor) : "");
        txt.setPrefColumnCount(8);
        return txt;
    }

    // ---------------------------------------------------------------- navegación

    @FXML
    private void irRegistrarComida() {
        RegistrarComidaController c = Navegacion.ir(Navegacion.REGISTRAR_COMIDA);
        if (c != null) {
            c.setFecha(dpFecha.getValue());
        }
    }

    @FXML
    private void irHistorial() {
        Navegacion.ir(Navegacion.HISTORIAL);
    }

    @FXML
    private void irAdmin() {
        Navegacion.ir(Navegacion.ADMIN_ALIMENTOS);
    }

    @FXML
    private void cerrarSesion() {
        if (Navegacion.confirmar("¿Cerrar sesión?")) {
            authService.logout();
            Navegacion.ir(Navegacion.LOGIN);
        }
    }

    // ---------------------------------------------------------------- utilidades

    /** 150.0 -> "150", 12.5 -> "12.5". */
    static String formato(double n) {
        if (n == Math.rint(n)) {
            return String.format(Locale.US, "%.0f", n);
        }
        return String.format(Locale.US, "%.1f", n);
    }

    /** Cómo se ve cada comida en la lista: nombre y gramos arriba, tipo y macros abajo. */
    private static class CeldaRegistro extends ListCell<Registro> {
        @Override
        protected void updateItem(Registro r, boolean vacia) {
            super.updateItem(r, vacia);
            if (vacia || r == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            Label nombre = new Label(r.getAlimento().getNombre() + "  ·  " + formato(r.getGramos()) + " g");
            nombre.setStyle("-fx-font-weight: bold;");
            Macros m = r.getMacros();
            Label detalle = new Label(r.getTipoComida() + "   " + formato(m.getKcal()) + " kcal"
                    + "  |  P " + formato(m.getProteina())
                    + "  C " + formato(m.getCarbohidratos())
                    + "  G " + formato(m.getGrasas()));
            detalle.getStyleClass().add("texto-suave");
            setText(null);
            setGraphic(new VBox(2, nombre, detalle));
        }
    }
}
