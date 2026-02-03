package interfaz.personal;

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
 * Dashboard del Personal de Investigación.
 * Muestra:
 *  – Datos del personal (nombre, tipo, cedula, correo)
 *  – Datos del proyecto asignado
 *  – Estado de participación
 *  – Resumen de informes (en edición / enviados / aprobados / rechazados)
 *  – Notificaciones recientes (máximo 5)
 */
public class PerDashboard extends VBox {

    private final PersonalDeInvestigacion personal;
    private final Proyecto                proyecto;

    public PerDashboard(PersonalDeInvestigacion personal, Proyecto proyecto) {
        super(18);
        this.personal = personal;
        this.proyecto = proyecto;
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";");
        cargar();
    }

    private void cargar() {
        getChildren().clear();
        getChildren().add(EstiloUI.labelTitulo("Dashboard"));

        // ── Tarjeta datos personal ──────────────────────────────────────
        VBox cardPersonal = EstiloUI.tarjeta();
        HBox personHeader = new HBox(16);
        personHeader.setAlignment(Pos.CENTER_LEFT);
        personHeader.getChildren().add(EstiloUI.labelSubtitulo(personal.getNombresCompletos()));

        // Badge con el tipo
        Label badgeTipo = EstiloUI.badge(personal.getTipo(), EstiloUI.C_LIGHT, EstiloUI.C_WHITE);
        personHeader.getChildren().add(badgeTipo);
        cardPersonal.getChildren().add(personHeader);

        GridPane gridPer = new GridPane();
        gridPer.setHgap(24); gridPer.setVgap(8);
        gridPer.setPadding(new Insets(8, 0, 0, 0));

        addGridRow(gridPer, 0, "Cédula:",  personal.getCedula() != null ? personal.getCedula() : "—");
        addGridRow(gridPer, 1, "Correo:",  personal.getCorreo() != null ? personal.getCorreo() : "—");

        // Estado participación
        try {
            ParticipacionDAO parDAO = new ParticipacionDAO();
            List<Participacion> participaciones = parDAO.obtenerPorPersonal(personal.getCedula());
            if (!participaciones.isEmpty()) {
                Participacion p = participaciones.get(0);
                addGridRow(gridPer, 2, "Participación:",
                        p.getEstado().name() + (p.getFechaInicio() != null ? "  (desde " + p.getFechaInicio() + ")" : ""));
            }
        } catch (SQLException e) {
            // ignorar – se muestra sin participación
        }

        cardPersonal.getChildren().add(gridPer);
        getChildren().add(cardPersonal);

        // ── Tarjeta datos proyecto ──────────────────────────────────────
        if (proyecto != null) {
            VBox cardProyecto = EstiloUI.tarjeta();
            HBox proyHeader = new HBox(16);
            proyHeader.setAlignment(Pos.CENTER_LEFT);
            proyHeader.getChildren().add(EstiloUI.labelSubtitulo(proyecto.getNombre()));
            proyHeader.getChildren().add(EstiloUI.badgeEstadoProyecto(proyecto.getEstado().name()));
            cardProyecto.getChildren().add(proyHeader);

            GridPane gridProy = new GridPane();
            gridProy.setHgap(24); gridProy.setVgap(8);
            gridProy.setPadding(new Insets(8, 0, 0, 0));

            addGridRow(gridProy, 0, "Código:",   proyecto.getCodigoProyecto() != null ? proyecto.getCodigoProyecto() : "—");
            addGridRow(gridProy, 1, "Tipo:",     proyecto.getTipoProyecto());
            addGridRow(gridProy, 2, "Periodo:",  proyecto.getPeriodoInicio() != null ? proyecto.getPeriodoInicio().getCodigo() : "—");
            addGridRow(gridProy, 3, "Duración:", proyecto.getDuracionMeses() + " meses");

            cardProyecto.getChildren().add(gridProy);
            getChildren().add(cardProyecto);
        }

        // ── Estadísticas de informes ────────────────────────────────────
        try {
            InformeActividadesDAO iaDAO = new InformeActividadesDAO();
            List<InformeActividades> informes = iaDAO.obtenerPorPersonal(personal.getCedula());

            long enEdicion  = informes.stream().filter(i -> i.getEstado() == EstadoInforme.EN_EDICION).count();
            long enviados   = informes.stream().filter(i -> i.getEstado() == EstadoInforme.ENVIADO).count();
            long aprobados  = informes.stream().filter(i -> i.getEstado() == EstadoInforme.APROBADO).count();
            long rechazados = informes.stream().filter(i -> i.getEstado() == EstadoInforme.RECHAZADO).count();

            HBox tarjetas = new HBox(16);
            tarjetas.setAlignment(Pos.CENTER_LEFT);
            tarjetas.getChildren().addAll(
                    EstiloUI.tarjetaStat("En Edición",  String.valueOf(enEdicion),  "Borradores"),
                    EstiloUI.tarjetaStat("Enviados",    String.valueOf(enviados),   "Pendientes"),
                    EstiloUI.tarjetaStat("Aprobados",   String.valueOf(aprobados),  "Finalizados"),
                    EstiloUI.tarjetaStat("Rechazados",  String.valueOf(rechazados), "Revisar")
            );
            getChildren().add(tarjetas);

            // Si hay algún rechazado, mostrar aviso
            List<InformeActividades> rechazadosList = informes.stream()
                    .filter(i -> i.getEstado() == EstadoInforme.RECHAZADO)
                    .collect(Collectors.toList());
            if (!rechazadosList.isEmpty()) {
                VBox avisoCard = EstiloUI.tarjeta();
                avisoCard.setStyle(avisoCard.getStyle() + "-fx-border-color: " + EstiloUI.C_ORANGE + "; -fx-border-width: 2;");
                avisoCard.getChildren().add(EstiloUI.labelSeccion("⚠ Informes rechazados"));
                for (InformeActividades inf : rechazadosList) {
                    avisoCard.getChildren().add(EstiloUI.labelSmall(
                            "Informe del " + (inf.getFechaRegistro() != null ? inf.getFechaRegistro() : "—") +
                            " – fue rechazado. Puede editarlo y reenviarlo desde la sección Informes."));
                }
                getChildren().add(avisoCard);
            }

        } catch (SQLException e) {
            getChildren().add(EstiloUI.labelSmall("Error al cargar informes: " + e.getMessage()));
        }

        // ── Notificaciones recientes ────────────────────────────────────
        try {
            personal.cargarNotificaciones();
            if (!personal.getNotificaciones().isEmpty()) {
                VBox cardNotif = EstiloUI.tarjeta();
                cardNotif.getChildren().add(EstiloUI.labelSubtitulo("Notificaciones Recientes"));

                int max = Math.min(5, personal.getNotificaciones().size());
                for (int i = 0; i < max; i++) {
                    Notificacion n = personal.getNotificaciones().get(i);
                    VBox item = new VBox(2);
                    item.setPadding(new Insets(6, 0, 6, 0));
                    item.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");
                    item.getChildren().add(EstiloUI.labelBody(n.getContenido()));
                    item.getChildren().add(EstiloUI.labelSmall(
                            n.getFecha() != null ? n.getFecha().toLocalDate().toString() : ""));
                    cardNotif.getChildren().add(item);
                }
                getChildren().add(cardNotif);
            }
        } catch (SQLException e) {
            // silencio – notificaciones no críticas
        }
    }

    // ─── UTILIDAD ───────────────────────────────────────────────────────────
    private void addGridRow(GridPane grid, int row, String label, String valor) {
        Label lbl = EstiloUI.labelSmall(label);
        lbl.setStyle(lbl.getStyle() + "-fx-font-weight: 600;");
        lbl.setMinWidth(120);
        Label val = EstiloUI.labelBody(valor);
        GridPane.setColumnIndex(lbl, 0); GridPane.setRowIndex(lbl, row);
        GridPane.setColumnIndex(val, 1); GridPane.setRowIndex(val, row);
        grid.getChildren().addAll(lbl, val);
    }
}
