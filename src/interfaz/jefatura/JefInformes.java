package interfaz.jefatura;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import Logica.DAO.*;
import Logica.Entidades.*;
import Logica.Enumeraciones.EstadoInforme;
import interfaz.comun.EstiloUI;

import java.sql.SQLException;
import java.util.List;

/**
 * Módulo de Informes de Actividades para Jefatura.
 * Listado de todos los informes del sistema con estado y personal asociado.
 * Permite visualizar el detalle (semanas) de cada informe.
 */
public class JefInformes extends VBox {

    private VBox contenido;

    public JefInformes() {
        super(18);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";");

        getChildren().add(EstiloUI.labelTitulo("Informes de Actividades"));

        // Filtro por estado
        HBox filtros = new HBox(16);
        filtros.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> cboEstado = new ComboBox<>();
        cboEstado.getItems().addAll("Todos", "EN_EDICION", "ENVIADO", "APROBADO", "RECHAZADO");
        cboEstado.setValue("Todos");
        cboEstado.setOnAction(e -> cargar(cboEstado.getValue()));

        filtros.getChildren().addAll(EstiloUI.labelSmall("Estado:"), cboEstado);
        getChildren().add(filtros);

        // Tarjeta con listado
        VBox tarjeta = EstiloUI.tarjeta();

        // Header
        HBox tableHeader = new HBox();
        tableHeader.setPadding(new Insets(10, 0, 10, 0));
        tableHeader.setStyle("-fx-border-color: " + EstiloUI.C_DARK + "; -fx-border-width: 0 0 2 0;");
        String[] cols = {"ID", "Personal", "Proyecto", "Fecha Reg.", "Estado", "Acciones"};
        double[] widths = {60, 200, 160, 110, 110, 140};
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

        cargar("Todos");
    }

    private void cargar(String filtroEstado) {
        contenido.getChildren().clear();
        try {
            InformeActividadesDAO iaDAO = new InformeActividadesDAO();
            PersonalDeInvestigacionDAO piDAO = new PersonalDeInvestigacionDAO();

            // Obtener todos los personales y luego sus informes
            List<PersonalDeInvestigacion> personales = piDAO.obtenerTodos();

            for (PersonalDeInvestigacion p : personales) {
                List<InformeActividades> informes = iaDAO.obtenerPorPersonal(p.getCedula());
                for (InformeActividades inf : informes) {
                    // Filtro
                    if (!"Todos".equals(filtroEstado) && !inf.getEstado().name().equals(filtroEstado)) continue;

                    contenido.getChildren().add(crearFila(inf));
                }
            }

            if (contenido.getChildren().isEmpty()) {
                contenido.getChildren().add(EstiloUI.labelSmall("  No hay informes disponibles."));
            }

        } catch (SQLException e) {
            contenido.getChildren().add(EstiloUI.labelSmall("  Error: " + e.getMessage()));
        }
    }

    private HBox crearFila(InformeActividades inf) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(9, 0, 9, 0));
        fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

        Label id = EstiloUI.labelBody(String.valueOf(inf.getIdInforme()));
        id.setMinWidth(60); id.setPrefWidth(60);

        String personNom = (inf.getPersonalDeInvestigacion() != null) ?
                inf.getPersonalDeInvestigacion().getNombresCompletos() : "—";
        Label personal = EstiloUI.labelBody(personNom);
        personal.setMinWidth(200); personal.setPrefWidth(200);

        String proyNom = (inf.getProyecto() != null) ? inf.getProyecto().getNombre() : "Proy #" +
                (inf.getProyecto() != null ? inf.getProyecto().getIdProyecto() : "?");
        Label proyecto = EstiloUI.labelBody(proyNom);
        proyecto.setMinWidth(160); proyecto.setPrefWidth(160);

        Label fecha = EstiloUI.labelBody(inf.getFechaRegistro() != null ? inf.getFechaRegistro().toString() : "—");
        fecha.setMinWidth(110); fecha.setPrefWidth(110);

        Label estado = EstiloUI.badgeEstadoInforme(inf.getEstado().name());
        estado.setMinWidth(110);

        // Acciones
        HBox acciones = new HBox(6);
        Button btnVer = EstiloUI.botonSmall("Ver", EstiloUI.C_DARK);
        btnVer.setOnAction(e -> mostrarDetalleInforme(inf));
        acciones.getChildren().add(btnVer);

        fila.getChildren().addAll(id, personal, proyecto, fecha, estado, acciones);
        return fila;
    }

    private void mostrarDetalleInforme(InformeActividades inf) {
        VBox detalle = new VBox(10);
        detalle.setPadding(new Insets(10));
        detalle.setMinWidth(500);

        // Personal info
        String personNom = (inf.getPersonalDeInvestigacion() != null) ?
                inf.getPersonalDeInvestigacion().getNombresCompletos() : "—";
        detalle.getChildren().add(EstiloUI.labelSubtitulo("Personal: " + personNom));
        detalle.getChildren().add(EstiloUI.labelSmall("Fecha: " +
                (inf.getFechaRegistro() != null ? inf.getFechaRegistro().toString() : "—") +
                " | Estado: " + inf.getEstado().name()));
        detalle.getChildren().add(EstiloUI.separador());
        detalle.getChildren().add(EstiloUI.labelBody("Horas totales: " +
                String.format("%.1f", inf.calcularHorasTotales())));
        detalle.getChildren().add(EstiloUI.separador());

        // Semanas
        if (inf.getSemanas() != null && !inf.getSemanas().isEmpty()) {
            for (Logica.Entidades.SemanaActividades sem : inf.getSemanas()) {
                VBox semanaBox = new VBox(4);
                semanaBox.setPadding(new Insets(8));
                semanaBox.setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";" +
                        "-fx-border-radius: 4; -fx-background-radius: 4;");

                semanaBox.getChildren().add(EstiloUI.labelSeccion("Semana " + sem.getNumeroSemana()));
                if (sem.getActividadSemanal() != null)
                    semanaBox.getChildren().add(EstiloUI.labelBody("Actividad: " + sem.getActividadSemanal()));
                if (sem.getObservaciones() != null)
                    semanaBox.getChildren().add(EstiloUI.labelSmall("Observaciones: " + sem.getObservaciones()));
                semanaBox.getChildren().add(EstiloUI.labelSmall("Días trabajados: " + sem.contarDiasTrabajados() +
                        " | Horas: " + String.format("%.1f", sem.calcularHorasTotales())));

                detalle.getChildren().add(semanaBox);
            }
        } else {
            detalle.getChildren().add(EstiloUI.labelSmall("Sin semanas registradas."));
        }

        Alert popup = new Alert(Alert.AlertType.INFORMATION);
        popup.setTitle("Detalle del Informe");
        popup.setHeaderText("Informe #" + inf.getIdInforme());
        popup.getDialogPane().setContent(detalle);
        popup.getDialogPane().setMinWidth(540);
        popup.showAndWait();
    }
}
