package com.macrotrack.controller;

import com.macrotrack.model.Macros;
import com.macrotrack.service.ReporteService;
import com.macrotrack.util.excepciones.MacroTrackException;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

import static com.macrotrack.controller.DashboardController.formato;

public class HistorialController {

    @FXML private DatePicker dpInicio;
    @FXML private ListView<String> lstDias;
    @FXML private Label lblPromedio;

    private final ReporteService reporteService = new ReporteService();

    @FXML
    private void initialize() {
        // lunes de la semana actual
        dpInicio.setValue(LocalDate.now().with(DayOfWeek.MONDAY));
        lstDias.setPlaceholder(new Label("No hay registros esta semana"));
        lstDias.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) abrirDia(); });
        cargar();
        dpInicio.valueProperty().addListener((obs, anterior, nueva) -> cargar());
    }

    private void cargar() {
        LocalDate inicio = dpInicio.getValue();
        if (inicio == null) {
            return;
        }
        try {
            Map<LocalDate, Macros> semana = reporteService.semana(inicio);
            lstDias.getItems().clear();
            for (Map.Entry<LocalDate, Macros> dia : semana.entrySet()) {
                lstDias.getItems().add(linea(dia.getKey(), dia.getValue()));
            }
            lblPromedio.setText(lineaMacros(reporteService.promedioDiario(inicio)));
        } catch (MacroTrackException e) {
            Navegacion.error(e.getMessage());
        }
    }

    private void abrirDia() {
        int i = lstDias.getSelectionModel().getSelectedIndex();
        if (i < 0) {
            return;
        }
        LocalDate fecha = dpInicio.getValue().plusDays(i);
        DashboardController c = Navegacion.ir(Navegacion.DASHBOARD);
        if (c != null) {
            c.setFecha(fecha);
        }
    }

    private String linea(LocalDate fecha, Macros m) {
        return fecha + "   " + lineaMacros(m);
    }

    private String lineaMacros(Macros m) {
        return formato(m.getKcal()) + " kcal | P " + formato(m.getProteina())
                + " g | C " + formato(m.getCarbohidratos()) + " g | G " + formato(m.getGrasas()) + " g";
    }

    @FXML
    private void exportar() {
        LocalDate inicio = dpInicio.getValue();
        if (inicio == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar historial");
        chooser.setInitialFileName("historial-" + inicio + ".csv");
        File destino = chooser.showSaveDialog(lstDias.getScene().getWindow());
        if (destino == null) {
            return;
        }
        try (PrintWriter out = new PrintWriter(destino, StandardCharsets.UTF_8)) {
            out.print('\uFEFF');
            out.println("fecha,kcal,proteina,carbohidratos,grasas");
            for (Map.Entry<LocalDate, Macros> dia : reporteService.semana(inicio).entrySet()) {
                out.println(csvFila(dia.getKey().toString(), dia.getValue()));
            }
            out.println(csvFila("promedio", reporteService.promedioDiario(inicio)));
            Navegacion.info("Historial exportado a " + destino.getName());
        } catch (IOException e) {
            Navegacion.error("No se pudo exportar el historial");
        }
    }

    static String csvFila(String etiqueta, Macros m) {
        return etiqueta + "," + formato(m.getKcal()) + "," + formato(m.getProteina())
                + "," + formato(m.getCarbohidratos()) + "," + formato(m.getGrasas());
    }

    @FXML
    private void volver() {
        Navegacion.ir(Navegacion.DASHBOARD);
    }
}
