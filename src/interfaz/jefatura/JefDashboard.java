package interfaz.jefatura;

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
 * Dashboard principal de Jefatura.
 * Muestra:
 *  – Tarjetas resumen: proyectos activos, en revisión, reportes pendientes
 *  – Lista de proyectos EN_REVISION con botón "Aprobar"
 *  – Reportes pendientes de aprobación
 */
public class JefDashboard extends VBox {

    private final Jefatura jefatura;

    public JefDashboard(Jefatura jefatura) {
        super(20);
        this.jefatura = jefatura;
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";");

        cargar();
    }

    private void cargar() {
        getChildren().clear();

        try {
            ProyectoDAO pDAO    = new ProyectoDAO();
            ReporteDAO  rDAO    = new ReporteDAO();
            List<Proyecto> proyectos = pDAO.obtenerTodos();
            List<Reporte>  reportes  = rDAO.obtenerTodos();

            long activos      = proyectos.stream().filter(p -> p.getEstado() == EstadoProyecto.APROBADO).count();
            long enRevision   = proyectos.stream().filter(p -> p.getEstado() == EstadoProyecto.EN_REVISION).count();
            long reportesPend = reportes.stream().filter(r -> r.getEstado() == EstadoReporte.CERRADO).count();

            // ── Título ───────────────────────────────────────────
            getChildren().add(EstiloUI.labelTitulo("Dashboard"));

            // ── Tarjetas estadísticas ────────────────────────────
            HBox tarjetas = new HBox(16);
            tarjetas.setAlignment(Pos.CENTER_LEFT);
            tarjetas.getChildren().addAll(
                    EstiloUI.tarjetaStat("Proyectos Activos",  String.valueOf(activos),     "Aprobados"),
                    EstiloUI.tarjetaStat("En Revisión",        String.valueOf(enRevision),  "Pendientes"),
                    EstiloUI.tarjetaStat("Reportes Pendientes",String.valueOf(reportesPend),"Esperando aprobación"),
                    EstiloUI.tarjetaStat("Total Proyectos",    String.valueOf(proyectos.size()), "Registrados")
            );
            getChildren().add(tarjetas);

            getChildren().add(EstiloUI.separador());

            // ── Proyectos EN_REVISION ────────────────────────────
            List<Proyecto> enRevisionList = proyectos.stream()
                    .filter(p -> p.getEstado() == EstadoProyecto.EN_REVISION)
                    .collect(Collectors.toList());

            VBox seccion1 = EstiloUI.tarjeta();
            seccion1.getChildren().add(EstiloUI.labelSubtitulo("Proyectos en Revisión"));

            if (enRevisionList.isEmpty()) {
                seccion1.getChildren().add(EstiloUI.labelSmall("No hay proyectos pendientes de revisión."));
            } else {
                for (Proyecto p : enRevisionList) {
                    seccion1.getChildren().add(crearFilaProyectoRevision(p));
                }
            }
            getChildren().add(seccion1);

            // ── Reportes pendientes ──────────────────────────────
            List<Reporte> pendientes = reportes.stream()
                    .filter(r -> r.getEstado() == EstadoReporte.CERRADO)
                    .collect(Collectors.toList());

            VBox seccion2 = EstiloUI.tarjeta();
            seccion2.getChildren().add(EstiloUI.labelSubtitulo("Reportes Pendientes de Aprobación"));

            if (pendientes.isEmpty()) {
                seccion2.getChildren().add(EstiloUI.labelSmall("No hay reportes pendientes."));
            } else {
                for (Reporte r : pendientes) {
                    seccion2.getChildren().add(crearFilaReportePendiente(r));
                }
            }
            getChildren().add(seccion2);

        } catch (SQLException e) {
            getChildren().add(EstiloUI.labelSmall("Error al cargar datos: " + e.getMessage()));
        }
    }

    // ─── FILA: proyecto en revisión ──────────────────────────────────────
    private HBox crearFilaProyectoRevision(Proyecto p) {
        HBox fila = new HBox(16);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 0, 10, 0));
        fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

        // Nombre + código
        VBox info = new VBox(2);
        info.setMinWidth(220);
        info.getChildren().add(EstiloUI.labelBody(p.getNombre()));
        info.getChildren().add(EstiloUI.labelSmall("Código: " + (p.getCodigoProyecto() != null ? p.getCodigoProyecto() : "—")));

        // Director candidato
        String dirNombre = (p.getDirector() != null) ? p.getDirector().getNombresCompletos() : "—";
        Label dirLabel = EstiloUI.labelSmall("Director: " + dirNombre);
        dirLabel.setMinWidth(180);

        // Badge estado
        Label badge = EstiloUI.badgeEstadoProyecto(p.getEstado().name());

        // Botones
        Button btnAprobar = EstiloUI.botonPrimario("Aprobar");
        btnAprobar.setOnAction(e -> aprobarProyecto(p));

        Button btnRechazar = EstiloUI.botonSmall("Rechazar", EstiloUI.C_RED);
        btnRechazar.setOnAction(e -> rechazarProyecto(p));

        HBox.setHgrow(info, Priority.ALWAYS);
        fila.getChildren().addAll(info, dirLabel, badge, btnAprobar, btnRechazar);
        return fila;
    }

    private void aprobarProyecto(Proyecto proyecto) {
        Alert confirma = EstiloUI.alertaConfirmacion(
                "Aprobar Proyecto",
                "¿Desea aprobar el proyecto \"" + proyecto.getNombre() + "\"? " +
                "Se creará el usuario y entidad del director.");
        confirma.showAndWait().ifPresent(btn -> {
            if (btn == javafx.scene.control.ButtonType.OK) {
                try {
                    jefatura.actualizarEstadoProyecto(proyecto, EstadoProyecto.APROBADO);
                    EstiloUI.alertaInfo("Éxito", "Proyecto aprobado exitosamente.").showAndWait();
                    cargar(); // refrescar
                } catch (SQLException e) {
                    EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
                }
            }
        });
    }

    private void rechazarProyecto(Proyecto proyecto) {
        Alert confirma = EstiloUI.alertaConfirmacion(
                "Rechazar Proyecto",
                "¿Desea marcar como no aprobado el proyecto \"" + proyecto.getNombre() + "\"?");
        confirma.showAndWait().ifPresent(btn -> {
            if (btn == javafx.scene.control.ButtonType.OK) {
                try {
                    jefatura.actualizarEstadoProyecto(proyecto, EstadoProyecto.NO_APROBADO);
                    EstiloUI.alertaInfo("Listo", "Proyecto marcado como no aprobado.").showAndWait();
                    cargar();
                } catch (SQLException e) {
                    EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
                }
            }
        });
    }

    // ─── FILA: reporte pendiente ──────────────────────────────────────────
    private HBox crearFilaReportePendiente(Reporte r) {
        HBox fila = new HBox(16);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 0, 10, 0));
        fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

        VBox info = new VBox(2);
        info.setMinWidth(200);
        info.getChildren().add(EstiloUI.labelBody("Reporte #" + r.getIdReporte()));
        info.getChildren().add(EstiloUI.labelSmall("Proyecto ID: " + r.getIdProyecto() + " | Periodo: " + r.getPeriodoAcademico()));

        Label badge = EstiloUI.badgeEstadoReporte(r.getEstado().name());

        Button btnAprobar = EstiloUI.botonSmall("Aprobar", EstiloUI.C_MEDIUM);
        btnAprobar.setOnAction(e -> aprobarReporte(r));

        HBox.setHgrow(info, Priority.ALWAYS);
        fila.getChildren().addAll(info, badge, btnAprobar);
        return fila;
    }

    private void aprobarReporte(Reporte reporte) {
        try {
            jefatura.aprobarReporte(reporte);
            EstiloUI.alertaInfo("Éxito", "Reporte aprobado.").showAndWait();
            cargar();
        } catch (SQLException e) {
            EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
        }
    }
}
