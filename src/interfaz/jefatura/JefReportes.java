package interfaz.jefatura;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import Logica.DAO.*;
import Logica.Entidades.*;
import Logica.Enumeraciones.EstadoReporte;
import interfaz.comun.EstiloUI;

import java.sql.SQLException;
import java.util.List;

/**
 * Módulo de Reportes para Jefatura.
 * Muestra todos los reportes enviados por directores.
 * Permite aprobar reportes en estado CERRADO (enviados).
 * Botón "Ver" para visualizar las participaciones incluidas.
 */
public class JefReportes extends VBox {

    private final Jefatura jefatura;
    private VBox contenido;
    private ComboBox<String> cboEstado;

    // Se recibe jefatura desde la pantalla principal
    // Si no se tiene, se construye sin ella (solo visualización)
    public JefReportes() {
        this(null);
    }

    public JefReportes(Jefatura jefatura) {
        super(18);
        this.jefatura = jefatura;
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";");

        getChildren().add(EstiloUI.labelTitulo("Reportes de Directores"));

        // Filtro
        HBox filtros = new HBox(16);
        filtros.setAlignment(Pos.CENTER_LEFT);
        cboEstado = new ComboBox<>();
        cboEstado.getItems().addAll("Todos", "EN_EDICION", "CERRADO", "APROBADO");
        cboEstado.setValue("Todos");
        cboEstado.setOnAction(e -> cargar());
        filtros.getChildren().addAll(EstiloUI.labelSmall("Estado:"), cboEstado);
        getChildren().add(filtros);

        // Tarjeta
        VBox tarjeta = EstiloUI.tarjeta();

        // Header tabla
        HBox tableHeader = new HBox();
        tableHeader.setPadding(new Insets(10, 0, 10, 0));
        tableHeader.setStyle("-fx-border-color: " + EstiloUI.C_DARK + "; -fx-border-width: 0 0 2 0;");
        String[] cols = {"ID", "Proyecto", "Periodo", "Fecha Ini.", "Fecha Cierre", "Estado", "Acciones"};
        double[] widths = {60, 180, 90, 100, 100, 110, 160};
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + EstiloUI.C_GRAY_DARK + ";");
            lbl.setMinWidth(widths[i]); lbl.setPrefWidth(widths[i]);
            tableHeader.getChildren().add(lbl);
        }
        tarjeta.getChildren().add(tableHeader);

        contenido = new VBox(0);
        tarjeta.getChildren().add(contenido);
        getChildren().add(tarjeta);

        cargar();
    }

    private void cargar() {
        contenido.getChildren().clear();
        try {
            ReporteDAO rDAO  = new ReporteDAO();
            ProyectoDAO pDAO = new ProyectoDAO();
            List<Reporte> reportes = rDAO.obtenerTodos();

            String filtro = cboEstado.getValue();

            for (Reporte r : reportes) {
                if (!"Todos".equals(filtro) && !r.getEstado().name().equals(filtro)) continue;

                // Obtener nombre del proyecto
                Proyecto p = pDAO.obtenerPorId(r.getIdProyecto());
                String proyNom = (p != null) ? p.getNombre() : "Proyecto #" + r.getIdProyecto();

                contenido.getChildren().add(crearFila(r, proyNom));
            }

            if (contenido.getChildren().isEmpty()) {
                contenido.getChildren().add(EstiloUI.labelSmall("  No hay reportes disponibles."));
            }

        } catch (SQLException e) {
            contenido.getChildren().add(EstiloUI.labelSmall("  Error: " + e.getMessage()));
        }
    }

    private HBox crearFila(Reporte r, String proyNom) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(9, 0, 9, 0));
        fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

        Label id = EstiloUI.labelBody(String.valueOf(r.getIdReporte()));
        id.setMinWidth(60); id.setPrefWidth(60);

        Label proyecto = EstiloUI.labelBody(proyNom);
        proyecto.setMinWidth(180); proyecto.setPrefWidth(180);

        Label periodo = EstiloUI.labelBody(r.getPeriodoAcademico() != null ? r.getPeriodoAcademico() : "—");
        periodo.setMinWidth(90); periodo.setPrefWidth(90);

        Label fechaIni = EstiloUI.labelBody(r.getFechaInicio() != null ? r.getFechaInicio().toString() : "—");
        fechaIni.setMinWidth(100); fechaIni.setPrefWidth(100);

        Label fechaCierre = EstiloUI.labelBody(r.getFechaCierre() != null ? r.getFechaCierre().toString() : "—");
        fechaCierre.setMinWidth(100); fechaCierre.setPrefWidth(100);

        Label estado = EstiloUI.badgeEstadoReporte(r.getEstado().name());
        estado.setMinWidth(110);

        // Acciones
        HBox acciones = new HBox(6);
        Button btnVer = EstiloUI.botonSmall("Ver", EstiloUI.C_DARK);
        btnVer.setOnAction(e -> mostrarDetalleReporte(r));
        acciones.getChildren().add(btnVer);

        if (r.getEstado() == EstadoReporte.CERRADO && jefatura != null) {
            Button btnAprobar = EstiloUI.botonSmall("Aprobar", EstiloUI.C_MEDIUM);
            btnAprobar.setOnAction(e -> aprobarReporte(r));
            acciones.getChildren().add(btnAprobar);
        }

        fila.getChildren().addAll(id, proyecto, periodo, fechaIni, fechaCierre, estado, acciones);
        return fila;
    }

    private void aprobarReporte(Reporte r) {
        Alert confirma = EstiloUI.alertaConfirmacion("Aprobar Reporte",
                "¿Desea aprobar el reporte #" + r.getIdReporte() + "?");
        confirma.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    jefatura.aprobarReporte(r);
                    EstiloUI.alertaInfo("Éxito", "Reporte aprobado.").showAndWait();
                    cargar();
                } catch (SQLException e) {
                    EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
                }
            }
        });
    }

    private void mostrarDetalleReporte(Reporte r) {
        try {
            ReporteDAO rDAO = new ReporteDAO();
            rDAO.cargarParticipacionesDelReporte(r);

            VBox detalle = new VBox(10);
            detalle.setPadding(new Insets(10));
            detalle.setMinWidth(480);

            detalle.getChildren().add(EstiloUI.labelSubtitulo("Reporte #" + r.getIdReporte()));
            detalle.getChildren().add(EstiloUI.labelSmall("Periodo: " + r.getPeriodoAcademico() +
                    " | Estado: " + r.getEstado().name()));
            detalle.getChildren().add(EstiloUI.separador());
            detalle.getChildren().add(EstiloUI.labelSeccion("Participaciones incluidas:"));

            if (r.getParticipacionesIncluidas().isEmpty()) {
                detalle.getChildren().add(EstiloUI.labelSmall("  Sin participaciones."));
            } else {
                for (Participacion p : r.getParticipacionesIncluidas()) {
                    HBox fila = new HBox(16);
                    fila.setPadding(new Insets(6, 8, 6, 8));
                    fila.setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";" +
                            "-fx-border-radius: 4; -fx-background-radius: 4;");

                    String nom = (p.getPersonal() != null) ? p.getPersonal().getNombresCompletos() : "—";
                    String tipo = (p.getPersonal() != null) ? p.getPersonal().getTipo() : "—";
                    fila.getChildren().addAll(
                            EstiloUI.labelBody(nom),
                            EstiloUI.labelSmall(tipo),
                            EstiloUI.labelSmall("Inicio: " + (p.getFechaInicio() != null ? p.getFechaInicio() : "—")),
                            EstiloUI.badgeEstadoParticipacion(p.getEstado().name())
                    );
                    detalle.getChildren().add(fila);
                }
            }

            Alert popup = new Alert(Alert.AlertType.INFORMATION);
            popup.setTitle("Detalle del Reporte");
            popup.setHeaderText("Reporte #" + r.getIdReporte());
            popup.getDialogPane().setContent(detalle);
            popup.getDialogPane().setMinWidth(500);
            popup.showAndWait();

        } catch (SQLException e) {
            EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
        }
    }
}
