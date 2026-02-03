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

/**
 * Dashboard del Director.
 * Muestra:
 *  – Información del proyecto asignado
 *  – Conteo de personal por tipo y estado
 *  – Reportes del periodo (en edición / enviados)
 *  – Informes pendientes de revisión
 *  – Notificaciones recientes
 */
public class DirDashboard extends VBox {

    private final Director director;
    private final Proyecto proyecto;

    public DirDashboard(Director director, Proyecto proyecto) {
        super(18);
        this.director = director;
        this.proyecto = proyecto;
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";");

        cargar();
    }

    private void cargar() {
        getChildren().clear();
        getChildren().add(EstiloUI.labelTitulo("Dashboard"));

        if (proyecto == null) {
            // Sin proyecto asignado
            VBox aviso = EstiloUI.tarjeta();
            aviso.getChildren().add(EstiloUI.labelSubtitulo("No se encontró proyecto asignado."));
            aviso.getChildren().add(EstiloUI.labelSmall("Comuníquese con Jefatura para verificar el estado de su proyecto."));
            getChildren().add(aviso);
            return;
        }

        try {
            PersonalDeInvestigacionDAO piDAO = new PersonalDeInvestigacionDAO();
            ParticipacionDAO parDAO          = new ParticipacionDAO();
            ReporteDAO rDAO                  = new ReporteDAO();
            InformeActividadesDAO iaDAO      = new InformeActividadesDAO();

            // ── Tarjeta info proyecto ─────────────────────────
            VBox cardProyecto = EstiloUI.tarjeta();
            HBox proyHeader = new HBox(16);
            proyHeader.setAlignment(Pos.CENTER_LEFT);
            proyHeader.getChildren().add(EstiloUI.labelSubtitulo(proyecto.getNombre()));
            proyHeader.getChildren().add(EstiloUI.badgeEstadoProyecto(proyecto.getEstado().name()));
            cardProyecto.getChildren().add(proyHeader);

            GridPane gridInfo = new GridPane();
            gridInfo.setHgap(24); gridInfo.setVgap(8);
            gridInfo.setPadding(new Insets(8, 0, 0, 0));

            addGridRow(gridInfo, 0, "Código:",     proyecto.getCodigoProyecto() != null ? proyecto.getCodigoProyecto() : "—");
            addGridRow(gridInfo, 1, "Tipo:",        proyecto.getTipoProyecto());
            addGridRow(gridInfo, 2, "Periodo:",     proyecto.getPeriodoInicio() != null ? proyecto.getPeriodoInicio().getCodigo() : "—");
            addGridRow(gridInfo, 3, "Duración:",    proyecto.getDuracionMeses() + " meses");

            cardProyecto.getChildren().add(gridInfo);
            getChildren().add(cardProyecto);

            // ── Estadísticas personal ────────────────────────
            List<PersonalDeInvestigacion> personales = piDAO.obtenerTodos().stream()
                    .filter(p -> p.getIdProyecto() == proyecto.getIdProyecto())
                    .collect(java.util.stream.Collectors.toList());

            int totalPersonal = personales.size();
            int activos = 0;
            for (PersonalDeInvestigacion p : personales) {
                List<Participacion> parts = parDAO.obtenerPorPersonal(p.getCedula());
                if (!parts.isEmpty() && parts.get(0).getEstado() == EstadoParticipacion.ACTIVO) activos++;
            }

            List<Reporte> reportes = rDAO.obtenerPorProyecto(proyecto.getIdProyecto());
            long reportesEnEdicion = reportes.stream().filter(r -> r.getEstado() == EstadoReporte.EN_EDICION).count();

            List<InformeActividades> informesPendientes = iaDAO.obtenerPendientesDeRevision(proyecto.getIdProyecto());

            HBox tarjetas = new HBox(16);
            tarjetas.setAlignment(Pos.CENTER_LEFT);
            tarjetas.getChildren().addAll(
                    EstiloUI.tarjetaStat("Personal Total",    String.valueOf(totalPersonal),  "Registrados"),
                    EstiloUI.tarjetaStat("Personal Activo",   String.valueOf(activos),        "En el proyecto"),
                    EstiloUI.tarjetaStat("Reportes",          String.valueOf(reportesEnEdicion), "En edición"),
                    EstiloUI.tarjetaStat("Informes Pendientes", String.valueOf(informesPendientes.size()), "De revisión")
            );
            getChildren().add(tarjetas);

            // ── Informes pendientes de revisión ───────────────
            if (!informesPendientes.isEmpty()) {
                VBox cardInf = EstiloUI.tarjeta();
                cardInf.getChildren().add(EstiloUI.labelSubtitulo("Informes Pendientes de Revisión"));

                for (InformeActividades inf : informesPendientes) {
                    HBox fila = new HBox(16);
                    fila.setAlignment(Pos.CENTER_LEFT);
                    fila.setPadding(new Insets(8, 0, 8, 0));
                    fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

                    String personNom = (inf.getPersonalDeInvestigacion() != null) ?
                            inf.getPersonalDeInvestigacion().getNombresCompletos() : "—";
                    fila.getChildren().addAll(
                            EstiloUI.labelBody(personNom),
                            EstiloUI.labelSmall("Fecha: " + (inf.getFechaRegistro() != null ? inf.getFechaRegistro() : "—")),
                            EstiloUI.badgeEstadoInforme(inf.getEstado().name())
                    );

                    cardInf.getChildren().add(fila);
                }

                getChildren().add(cardInf);
            }

            // ── Notificaciones recientes ──────────────────────
            director.cargarNotificaciones();
            if (!director.getNotificaciones().isEmpty()) {
                VBox cardNotif = EstiloUI.tarjeta();
                cardNotif.getChildren().add(EstiloUI.labelSubtitulo("Notificaciones Recientes"));

                int max = Math.min(5, director.getNotificaciones().size());
                for (int i = 0; i < max; i++) {
                    Notificacion n = director.getNotificaciones().get(i);
                    VBox item = new VBox(2);
                    item.setPadding(new Insets(6, 0, 6, 0));
                    item.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

                    item.getChildren().add(EstiloUI.labelBody(n.getContenido()));
                    item.getChildren().add(EstiloUI.labelSmall(n.getFecha() != null ? n.getFecha().toLocalDate().toString() : ""));
                    cardNotif.getChildren().add(item);
                }
                getChildren().add(cardNotif);
            }

        } catch (SQLException e) {
            getChildren().add(EstiloUI.labelSmall("Error al cargar datos: " + e.getMessage()));
        }
    }

    private void addGridRow(GridPane grid, int row, String label, String valor) {
        Label lbl = EstiloUI.labelSmall(label);
        lbl.setStyle(lbl.getStyle() + "-fx-font-weight: 600;");
        lbl.setMinWidth(100);
        Label val = EstiloUI.labelBody(valor);
        GridPane.setColumnIndex(lbl, 0); GridPane.setRowIndex(lbl, row);
        GridPane.setColumnIndex(val, 1); GridPane.setRowIndex(val, row);
        grid.getChildren().addAll(lbl, val);
    }
}
