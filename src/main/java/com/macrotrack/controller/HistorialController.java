package com.macrotrack.controller;

import com.macrotrack.model.Macros;
import com.macrotrack.service.ReporteService;
import com.macrotrack.util.excepciones.MacroTrackException;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

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

    private String linea(LocalDate fecha, Macros m) {
        return fecha + "   " + lineaMacros(m);
    }

    private String lineaMacros(Macros m) {
        return formato(m.getKcal()) + " kcal | P " + formato(m.getProteina())
                + " g | C " + formato(m.getCarbohidratos()) + " g | G " + formato(m.getGrasas()) + " g";
    }

    @FXML
    private void volver() {
        Navegacion.ir(Navegacion.DASHBOARD);
    }
}
