package interfaz.director;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import Logica.DAO.*;
import Logica.Entidades.*;
import Logica.Enumeraciones.*;
import interfaz.comun.EstiloUI;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Módulo de Reportes para el Director.
 *  – Lista reportes del proyecto con estado y fechas
 *  – "Nuevo Reporte" crea un reporte EN_EDICION para el periodo actual
 *  – Seleccionar reporte EN_EDICION permite agregar / quitar participaciones activas
 *  – "Enviar" cierra el reporte y lo envía a Jefatura
 *
 * Restricción de dominio: máximo 2 reportes por periodo académico por proyecto.
 * No se puede crear un nuevo reporte mientras existe uno EN_EDICION.
 */
public class DirReportes extends VBox {

    private final Director  director;
    private final Proyecto  proyecto;

    // Estado de edición
    private Reporte          reporteSeleccionado;
    private VBox             panelDetalle;       // se reemplace al seleccionar reporte
    private VBox             contenidoTabla;     // filas de la lista superior

    public DirReportes(Director director, Proyecto proyecto) {
        super(18);
        this.director = director;
        this.proyecto = proyecto;
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";");
        construir();
    }

    // ─── CONSTRUCCIÓN INICIAL ──────────────────────────────────────────────
    private void construir() {
        getChildren().clear();

        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(EstiloUI.labelTitulo("Reportes"));
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);

        Button btnNuevo = EstiloUI.botonPrimario("+ Nuevo Reporte");
        btnNuevo.setOnAction(e -> crearNuevoReporte());
        header.getChildren().add(btnNuevo);
        getChildren().add(header);

        if (proyecto == null) {
            getChildren().add(EstiloUI.labelSmall("No hay proyecto asignado."));
            return;
        }

        // ── Tabla de reportes ────────────────────────────────────────────
        VBox tarjeta = EstiloUI.tarjeta();

        // Encabezado tabla
        HBox tableHeader = new HBox();
        tableHeader.setPadding(new Insets(10, 0, 10, 0));
        tableHeader.setStyle("-fx-border-color: " + EstiloUI.C_DARK + "; -fx-border-width: 0 0 2 0;");
        String[] cols   = {"#", "Periodo", "Estado", "Fecha Inicio", "Fecha Cierre", "Participaciones", "Acciones"};
        double[]  widths = {40, 130,        100,      130,             130,            120,               140};
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + EstiloUI.C_GRAY_DARK + ";");
            lbl.setMinWidth(widths[i]); lbl.setPrefWidth(widths[i]);
            tableHeader.getChildren().add(lbl);
        }
        tarjeta.getChildren().add(tableHeader);

        contenidoTabla = new VBox(0);
        tarjeta.getChildren().add(contenidoTabla);
        getChildren().add(tarjeta);

        // Panel de detalle (aparece al seleccionar un reporte EN_EDICION)
        panelDetalle = new VBox(12);
        getChildren().add(panelDetalle);

        cargarReportes();
    }

    // ─── CARGA DE DATOS ─────────────────────────────────────────────────────
    private void cargarReportes() {
        contenidoTabla.getChildren().clear();
        panelDetalle.getChildren().clear();
        reporteSeleccionado = null;

        try {
            ReporteDAO rDAO = new ReporteDAO();
            List<Reporte> reportes = rDAO.obtenerPorProyecto(proyecto.getIdProyecto());

            if (reportes.isEmpty()) {
                contenidoTabla.getChildren().add(EstiloUI.labelSmall("  No hay reportes aún."));
                return;
            }

            PeriodoAcademicoDAO paDAO = new PeriodoAcademicoDAO();

            int idx = 1;
            for (Reporte r : reportes) {
                rDAO.cargarParticipacionesDelReporte(r);

                // Determinar número dentro del periodo
                PeriodoAcademico pa = (r.getPeriodoAcademico() != null)
                        ? paDAO.obtenerPorCodigo(r.getPeriodoAcademico()) : null;
                int numReporte = (pa != null) ? r.getNumeroReporte(pa) : idx;

                contenidoTabla.getChildren().add(
                        crearFilaReporte(r, numReporte, pa));
                idx++;
            }
        } catch (SQLException e) {
            contenidoTabla.getChildren().add(EstiloUI.labelSmall("  Error: " + e.getMessage()));
        }
    }

    // ─── FILA DE REPORTE ────────────────────────────────────────────────────
    private HBox crearFilaReporte(Reporte r, int numero, PeriodoAcademico pa) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(9, 0, 9, 0));
        fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

        Label lblNum     = txt(String.valueOf(numero),                                      40);
        Label lblPeriodo = txt(r.getPeriodoAcademico() != null ? r.getPeriodoAcademico() : "—", 130);
        Label lblEstado  = EstiloUI.badgeEstadoReporte(r.getEstado().name());
        lblEstado.setMinWidth(100);
        Label lblInicio  = txt(r.getFechaInicio()  != null ? r.getFechaInicio().toString()  : "—", 130);
        Label lblCierre  = txt(r.getFechaCierre()  != null ? r.getFechaCierre().toString()  : "—", 130);
        Label lblParts   = txt(String.valueOf(r.getParticipacionesIncluidas().size()),        120);

        // Verificar si está atrasado
        if (pa != null && r.estaAtrasado(pa)) {
            lblEstado.setStyle(lblEstado.getStyle() + " -fx-border-color: " + EstiloUI.C_RED + "; -fx-border-width: 1;");
        }

        // Acciones
        HBox acciones = new HBox(6);
        if (r.getEstado() == EstadoReporte.EN_EDICION) {
            Button btnEditar = EstiloUI.botonSmall("Editar", EstiloUI.C_MEDIUM);
            btnEditar.setOnAction(e -> mostrarDetalle(r));
            acciones.getChildren().add(btnEditar);

            Button btnEnviar = EstiloUI.botonSmall("Enviar", EstiloUI.C_DARK);
            btnEnviar.setOnAction(e -> enviarReporte(r));
            acciones.getChildren().add(btnEnviar);
        }

        fila.getChildren().addAll(lblNum, lblPeriodo, lblEstado, lblInicio, lblCierre, lblParts, acciones);
        return fila;
    }

    // ─── PANEL DE DETALLE (agregar/quitar participaciones) ─────────────────
    private void mostrarDetalle(Reporte r) {
        reporteSeleccionado = r;
        panelDetalle.getChildren().clear();

        VBox card = EstiloUI.tarjeta();
        card.getChildren().add(EstiloUI.labelSubtitulo(
                "Editar Reporte – Periodo: " + (r.getPeriodoAcademico() != null ? r.getPeriodoAcademico() : "—")));

        try {
            ReporteDAO rDAO = new ReporteDAO();
            rDAO.cargarParticipacionesDelReporte(r);

            ParticipacionDAO parDAO = new ParticipacionDAO();
            List<Participacion> activasProyecto = parDAO.obtenerActivasPorProyecto(proyecto.getIdProyecto());

            // ── Participaciones ya incluidas ──
            card.getChildren().add(EstiloUI.labelSeccion("Participaciones incluidas en el reporte"));

            if (r.getParticipacionesIncluidas().isEmpty()) {
                card.getChildren().add(EstiloUI.labelSmall("  (ninguna aún – agrega desde la lista inferior)"));
            } else {
                for (Participacion p : r.getParticipacionesIncluidas()) {
                    HBox fila = new HBox(12);
                    fila.setAlignment(Pos.CENTER_LEFT);
                    fila.setPadding(new Insets(5, 0, 5, 0));

                    String nombre = (p.getPersonal() != null) ? p.getPersonal().getNombresCompletos() : "ID " + p.getIdParticipacion();
                    fila.getChildren().add(EstiloUI.labelBody(nombre));
                    fila.getChildren().add(EstiloUI.badgeEstadoParticipacion(p.getEstado().name()));

                    Button btnQuitar = EstiloUI.botonSmall("Quitar", EstiloUI.C_RED);
                    btnQuitar.setOnAction(e -> {
                        try {
                            rDAO.quitarParticipacion(r.getIdReporte(), p.getIdParticipacion());
                            r.getParticipacionesIncluidas().remove(p);
                            mostrarDetalle(r); // refrescar
                        } catch (SQLException ex) {
                            EstiloUI.alertaError("Error", ex.getMessage()).showAndWait();
                        }
                    });
                    fila.getChildren().add(btnQuitar);
                    card.getChildren().add(fila);
                }
            }

            // ── Participaciones disponibles para agregar ──
            List<Integer> yaIncluidos = r.getParticipacionesIncluidas().stream()
                    .map(Participacion::getIdParticipacion)
                    .collect(Collectors.toList());

            List<Participacion> disponibles = activasProyecto.stream()
                    .filter(p -> !yaIncluidos.contains(p.getIdParticipacion()))
                    .collect(Collectors.toList());

            card.getChildren().add(EstiloUI.labelSeccion("Participaciones activas disponibles"));

            if (disponibles.isEmpty()) {
                card.getChildren().add(EstiloUI.labelSmall("  Todas las participaciones activas ya están incluidas."));
            } else {
                for (Participacion p : disponibles) {
                    HBox fila = new HBox(12);
                    fila.setAlignment(Pos.CENTER_LEFT);
                    fila.setPadding(new Insets(5, 0, 5, 0));

                    String nombre = (p.getPersonal() != null) ? p.getPersonal().getNombresCompletos() : "ID " + p.getIdParticipacion();
                    fila.getChildren().add(EstiloUI.labelBody(nombre));
                    fila.getChildren().add(EstiloUI.badgeEstadoParticipacion(p.getEstado().name()));

                    Button btnAgregar = EstiloUI.botonSmall("+ Agregar", EstiloUI.C_MEDIUM);
                    btnAgregar.setOnAction(e -> {
                        try {
                            rDAO.agregarParticipacion(r.getIdReporte(), p.getIdParticipacion());
                            r.agregarParticipacion(p);
                            mostrarDetalle(r); // refrescar
                        } catch (SQLException ex) {
                            EstiloUI.alertaError("Error", ex.getMessage()).showAndWait();
                        }
                    });
                    fila.getChildren().add(btnAgregar);
                    card.getChildren().add(fila);
                }
            }

        } catch (SQLException e) {
            card.getChildren().add(EstiloUI.labelSmall("Error: " + e.getMessage()));
        }

        // Botón cerrar panel
        Button btnCerrar = EstiloUI.botonSecundario("Cerrar");
        btnCerrar.setOnAction(e -> {
            panelDetalle.getChildren().clear();
            reporteSeleccionado = null;
        });
        card.getChildren().add(btnCerrar);

        panelDetalle.getChildren().add(card);
    }

    // ─── CREAR NUEVO REPORTE ────────────────────────────────────────────────
    private void crearNuevoReporte() {
        try {
            // ── Validación: no puede haber un reporte EN_EDICION activo ──
            ReporteDAO rDAO = new ReporteDAO();
            List<Reporte> reportesActuales = rDAO.obtenerPorProyecto(proyecto.getIdProyecto());
            boolean tieneDraft = reportesActuales.stream()
                    .anyMatch(r -> r.getEstado() == EstadoReporte.EN_EDICION);

            if (tieneDraft) {
                EstiloUI.alertaError("No se puede crear reporte",
                                "Ya existe un reporte en edición. Debe enviarlo o eliminarlo antes de crear uno nuevo.")
                        .showAndWait();
                return;
            }

            // ── Selector de periodo académico ──
            PeriodoAcademicoDAO paDAO = new PeriodoAcademicoDAO();
            List<PeriodoAcademico> periodos = paDAO.obtenerTodos();

            if (periodos.isEmpty()) {
                EstiloUI.alertaError("Error", "No existen periodos académicos configurados.").showAndWait();
                return;
            }

            // ComboBox de periodos
            ComboBox<String> cboPeriodo = new ComboBox<>();
            for (PeriodoAcademico pa : periodos) {
                cboPeriodo.getItems().add(pa.getCodigo());
            }
            cboPeriodo.setValue(periodos.get(0).getCodigo());
            cboPeriodo.setPrefWidth(240);

            VBox form = new VBox(12);
            form.setPadding(new Insets(10));
            form.getChildren().addAll(
                    EstiloUI.labelSmall("Seleccione el periodo académico:"),
                    cboPeriodo
            );

            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
            dialog.setTitle("Nuevo Reporte");
            dialog.setHeaderText("Crear reporte – Proyecto: " + proyecto.getNombre());
            dialog.getDialogPane().setContent(form);
            dialog.getDialogPane().setMinWidth(400);
            dialog.getDialogPane().getButtonTypes().clear();
            ButtonType btnCrear = new ButtonType("Crear");
            dialog.getDialogPane().getButtonTypes().addAll(btnCrear, ButtonType.CANCEL);

            dialog.showAndWait().ifPresent(btn -> {
                if (btn == btnCrear) {
                    try {
                        String periodo = cboPeriodo.getValue();
                        int idReporte = director.iniciarReporte(periodo, proyecto.getIdProyecto());
                        EstiloUI.alertaInfo("Éxito",
                                "Reporte creado exitosamente (ID: " + idReporte + ").\n" +
                                        "Puede agregar participaciones y enviarlo desde la lista.").showAndWait();
                        cargarReportes(); // refrescar
                    } catch (SQLException e) {
                        EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
                    }
                }
            });

        } catch (SQLException e) {
            EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
        }
    }

    // ─── ENVIAR REPORTE ─────────────────────────────────────────────────────
    private void enviarReporte(Reporte r) {
        Alert confirm = EstiloUI.alertaConfirmacion("Confirmar envío",
                "¿Desea cerrar y enviar este reporte a Jefatura?\n" +
                        "Esta acción no puede deshacerse.");
        confirm.showAndWait().ifPresent(res -> {
            if (res == javafx.scene.control.ButtonType.OK) {
                try {
                    director.enviarReporte(r);
                    EstiloUI.alertaInfo("Éxito", "Reporte enviado exitosamente a Jefatura.").showAndWait();
                    cargarReportes();
                } catch (SQLException e) {
                    EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
                }
            }
        });
    }

    // ─── UTILIDADES ─────────────────────────────────────────────────────────
    private Label txt(String valor, double ancho) {
        Label l = EstiloUI.labelBody(valor);
        l.setMinWidth(ancho);
        l.setPrefWidth(ancho);
        return l;
    }
}