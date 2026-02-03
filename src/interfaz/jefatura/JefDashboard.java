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
 * Dashboard principal de Jefatura – solo lectura.
 * Las acciones de aprobar / rechazar se encuentran en JefProyectos y JefReportes.
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
            ProyectoDAO pDAO = new ProyectoDAO();
            ReporteDAO  rDAO = new ReporteDAO();
            List<Proyecto> proyectos = pDAO.obtenerTodos();
            List<Reporte>  reportes  = rDAO.obtenerTodos();

            long activos      = proyectos.stream().filter(p -> p.getEstado() == EstadoProyecto.APROBADO).count();
            long enRevision   = proyectos.stream().filter(p -> p.getEstado() == EstadoProyecto.EN_REVISION).count();
            long reportesPend = reportes.stream().filter(r -> r.getEstado() == EstadoReporte.CERRADO).count();

            getChildren().add(EstiloUI.labelTitulo("Dashboard"));

            HBox tarjetas = new HBox(16);
            tarjetas.setAlignment(Pos.CENTER_LEFT);
            tarjetas.getChildren().addAll(
                    EstiloUI.tarjetaStat("Proyectos Activos",   String.valueOf(activos),     "Aprobados"),
                    EstiloUI.tarjetaStat("En Revisión",         String.valueOf(enRevision),  "Pendientes"),
                    EstiloUI.tarjetaStat("Reportes Pendientes", String.valueOf(reportesPend),"Esperando aprobación"),
                    EstiloUI.tarjetaStat("Total Proyectos",     String.valueOf(proyectos.size()), "Registrados")
            );
            getChildren().add(tarjetas);
            getChildren().add(EstiloUI.separador());

            // ── Proyectos EN_REVISION (informativo) ──────────────
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

            // ── Reportes pendientes (informativo) ────────────────
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

    /** Fila proyecto – solo nombre, director y badge. Sin botones. */
    private HBox crearFilaProyectoRevision(Proyecto p) {
        HBox fila = new HBox(16);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 0, 10, 0));
        fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

        VBox info = new VBox(2);
        info.setMinWidth(220);
        info.getChildren().add(EstiloUI.labelBody(p.getNombre()));
        info.getChildren().add(EstiloUI.labelSmall("Código: " + (p.getCodigoProyecto() != null ? p.getCodigoProyecto() : "—")));

        String dirNombre = (p.getDirector() != null) ? p.getDirector().getNombresCompletos() : "—";
        Label dirLabel = EstiloUI.labelSmall("Director: " + dirNombre);
        dirLabel.setMinWidth(180);

        Label badge = EstiloUI.badgeEstadoProyecto(p.getEstado().name());

        HBox.setHgrow(info, Priority.ALWAYS);
        fila.getChildren().addAll(info, dirLabel, badge);
        return fila;
    }

    /** Fila reporte – solo info y badge. Sin botones. */
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

        HBox.setHgrow(info, Priority.ALWAYS);
        fila.getChildren().addAll(info, badge);
        return fila;
    }
}