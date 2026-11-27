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
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.macrotrack.controller.DashboardController.formato;

/**
 * Pantalla para registrar una porción de comida.
 * Al abrir muestra todo el catálogo (AlimentoService.listarTodos()) y al escribir lo filtra
 * (AlimentoService.buscar()). RegistroService.registrar() guarda la porción.
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

    /** Catálogo completo que se carga al abrir la pantalla. */
    private List<Alimento> catalogo = new ArrayList<>();

    /** Espera 300 ms después de la última tecla antes de buscar, para no consultar la BD en cada letra. */
    private final PauseTransition esperaBusqueda = new PauseTransition(Duration.millis(300));

    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(TipoComida.values());
        cmbTipo.setValue(tipoSegunHora());
        dpFecha.setValue(LocalDate.now());

        txtBuscar.setPromptText("Buscar alimento...");
        lstResultados.setPlaceholder(new Label("No hay alimentos en el catálogo"));

        // Buscador: reinicia la espera con cada tecla
        esperaBusqueda.setOnFinished(e -> buscar());
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> esperaBusqueda.playFromStart());

        // Al elegir un alimento o cambiar los gramos se actualiza la vista previa
        lstResultados.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, nuevo) -> mostrarAlimento(nuevo));
        txtGramos.textProperty().addListener((obs, anterior, nuevo) -> actualizarVistaPrevia());
        lstResultados.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) txtGramos.requestFocus(); });

        btnGuardar.setDisable(true);

        cargarCatalogo();
    }

    /** Trae todos los alimentos para que la lista no aparezca vacía al abrir. */
    private void cargarCatalogo() {
        try {
            catalogo = alimentoService.listarTodos();
            lstResultados.getItems().setAll(catalogo);
        } catch (MacroTrackException e) {
            Navegacion.error(e.getMessage());
        }
    }

    /** El dashboard le pasa la fecha que estaba viendo. */
    public void setFecha(LocalDate fecha) {
        if (fecha != null) {
            dpFecha.setValue(fecha);
        }
    }

    /**
     * Sin texto: muestra todo el catálogo. Con 1 letra: filtra el catálogo que ya está cargado.
     * Con 2 o más: busca en la base de datos con AlimentoService.buscar().
     */
    private void buscar() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim();
        try {
            if (texto.isEmpty()) {
                lstResultados.getItems().setAll(catalogo);
            } else if (texto.length() == 1) {
                lstResultados.getItems().setAll(filtrar(catalogo, texto));
            } else {
                lstResultados.getItems().setAll(alimentoService.buscar(texto));
            }
            if (lstResultados.getItems().isEmpty() && !texto.isEmpty()) {
                lstResultados.setPlaceholder(new Label("No se encontró \"" + texto + "\""));
            } else {
                lstResultados.setPlaceholder(new Label("No hay alimentos en el catálogo"));
            }
        } catch (MacroTrackException e) {
            Navegacion.error(e.getMessage());
        }
    }

    /** Alimentos cuyo nombre contiene el texto, sin importar mayúsculas ni acentos. */
    static List<Alimento> filtrar(List<Alimento> alimentos, String texto) {
        String buscado = normalizar(texto);
        List<Alimento> encontrados = new ArrayList<>();
        for (Alimento a : alimentos) {
            if (normalizar(a.getNombre()).contains(buscado)) {
                encontrados.add(a);
            }
        }
        return encontrados;
    }

    /** "Plátano" -> "platano". */
    static String normalizar(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
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
