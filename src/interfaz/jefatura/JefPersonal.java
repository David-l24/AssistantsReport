package interfaz.jefatura;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import Logica.DAO.*;
import Logica.Entidades.*;
import interfaz.comun.EstiloUI;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Módulo de Personal de Investigación para Jefatura.
 * Listado global con filtros: proyecto, tipo (Asistente/Ayudante/Técnico), estado participación.
 * Información completa de cada personal + sus participaciones.
 */
public class JefPersonal extends VBox {

    private final ComboBox<String> cboProyecto;
    private final ComboBox<String> cboTipo;
    private final ComboBox<String> cboEstado;
    private final TextField txtBuscar;
    private VBox contenido;

    public JefPersonal() {
        super(18);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";");

        // ── Título ──────────────────────────────────────────
        getChildren().add(EstiloUI.labelTitulo("Personal de Investigación"));

        // ── Filtros ─────────────────────────────────────────
        VBox filtros = EstiloUI.tarjeta();
        filtros.setPadding(new Insets(14));

        HBox filaFiltros = new HBox(16);
        filaFiltros.setAlignment(Pos.CENTER_LEFT);

        // Buscador
        txtBuscar = EstiloUI.crearTextField("Buscar nombre...");
        txtBuscar.setPrefWidth(220);
        txtBuscar.setOnKeyReleased(e -> aplicarFiltros());

        // Filtro proyecto
        cboProyecto = new ComboBox<>();
        cboProyecto.getItems().add("Todos los proyectos");
        cboProyecto.setValue("Todos los proyectos");
        cboProyecto.setPrefWidth(200);
        try {
            ProyectoDAO pDAO = new ProyectoDAO();
            pDAO.obtenerTodos().forEach(p -> cboProyecto.getItems().add(p.getIdProyecto() + " – " + p.getNombre()));
        } catch (SQLException ignored) {}
        cboProyecto.setOnAction(e -> aplicarFiltros());

        // Filtro tipo
        cboTipo = new ComboBox<>();
        cboTipo.getItems().addAll("Todos", "Asistente", "Ayudante", "Tecnico");
        cboTipo.setValue("Todos");
        cboTipo.setPrefWidth(150);
        cboTipo.setOnAction(e -> aplicarFiltros());

        // Filtro estado participación
        cboEstado = new ComboBox<>();
        cboEstado.getItems().addAll("Todos", "ACTIVO", "RETIRADO", "FINALIZADO");
        cboEstado.setValue("Todos");
        cboEstado.setPrefWidth(150);
        cboEstado.setOnAction(e -> aplicarFiltros());

        filaFiltros.getChildren().addAll(
                wrapFiltro("Buscar:", txtBuscar),
                wrapFiltro("Proyecto:", cboProyecto),
                wrapFiltro("Tipo:", cboTipo),
                wrapFiltro("Estado:", cboEstado)
        );

        filtros.getChildren().add(filaFiltros);
        getChildren().add(filtros);

        // ── Contenido listado ───────────────────────────────
        contenido = new VBox(0);
        VBox tarjeta = EstiloUI.tarjeta();

        // Header tabla
        HBox tableHeader = new HBox();
        tableHeader.setPadding(new Insets(10, 0, 10, 0));
        tableHeader.setStyle("-fx-border-color: " + EstiloUI.C_DARK + "; -fx-border-width: 0 0 2 0;");
        String[] cols = {"Nombres", "Apellidos", "Tipo", "Cédula", "Proyecto", "Participación"};
        double[] widths = {150, 150, 100, 120, 180, 120};
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + EstiloUI.C_GRAY_DARK + ";");
            lbl.setMinWidth(widths[i]);
            lbl.setPrefWidth(widths[i]);
            tableHeader.getChildren().add(lbl);
        }
        tarjeta.getChildren().addAll(tableHeader, contenido);
        getChildren().add(tarjeta);

        aplicarFiltros();
    }

    private void aplicarFiltros() {
        contenido.getChildren().clear();
        try {
            PersonalDeInvestigacionDAO piDAO = new PersonalDeInvestigacionDAO();
            ParticipacionDAO parDAO          = new ParticipacionDAO();
            List<PersonalDeInvestigacion> todos = piDAO.obtenerTodos();

            String buscar   = txtBuscar.getText().trim().toLowerCase();
            String tipoSel  = cboTipo.getValue();
            String estadoSel= cboEstado.getValue();
            String proyectoSel = cboProyecto.getValue();

            // Extraer ID del proyecto si no es "Todos"
            int idProyectoFiltro = -1;
            if (proyectoSel != null && !proyectoSel.equals("Todos los proyectos")) {
                try {
                    idProyectoFiltro = Integer.parseInt(proyectoSel.split(" – ")[0].trim());
                } catch (Exception ignored) {}
            }

            for (PersonalDeInvestigacion p : todos) {
                // Filtro nombre
                if (!buscar.isEmpty() &&
                    !p.getNombres().toLowerCase().contains(buscar) &&
                    !p.getApellidos().toLowerCase().contains(buscar)) continue;

                // Filtro tipo
                if (!"Todos".equals(tipoSel) && !tipoSel.equalsIgnoreCase(p.getTipo())) continue;

                // Filtro proyecto
                if (idProyectoFiltro > 0 && p.getIdProyecto() != idProyectoFiltro) continue;

                // Obtener participación
                List<Participacion> participaciones = parDAO.obtenerPorPersonal(p.getCedula());

                // Filtro estado participación
                if (!"Todos".equals(estadoSel)) {
                    boolean tiene = participaciones.stream()
                            .anyMatch(par -> par.getEstado().name().equalsIgnoreCase(estadoSel));
                    if (!tiene) continue;
                }

                // Estado de la participación más reciente
                String estadoParticipacion = participaciones.isEmpty() ? "—" :
                        participaciones.get(0).getEstado().name();

                contenido.getChildren().add(crearFila(p, estadoParticipacion));
            }

            if (contenido.getChildren().isEmpty()) {
                contenido.getChildren().add(EstiloUI.labelSmall("  No se encontró personal con los filtros aplicados."));
            }

        } catch (SQLException e) {
            contenido.getChildren().add(EstiloUI.labelSmall("  Error: " + e.getMessage()));
        }
    }

    private HBox crearFila(PersonalDeInvestigacion p, String estadoParticipacion) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(9, 0, 9, 0));
        fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

        Label nombres = EstiloUI.labelBody(p.getNombres());     nombres.setMinWidth(150); nombres.setPrefWidth(150);
        Label apellidos = EstiloUI.labelBody(p.getApellidos()); apellidos.setMinWidth(150); apellidos.setPrefWidth(150);
        Label tipo = EstiloUI.labelBody(p.getTipo());           tipo.setMinWidth(100);      tipo.setPrefWidth(100);
        Label cedula = EstiloUI.labelBody(p.getCedula());       cedula.setMinWidth(120);    cedula.setPrefWidth(120);
        Label proyecto = EstiloUI.labelBody("Proy #" + p.getIdProyecto()); proyecto.setMinWidth(180); proyecto.setPrefWidth(180);
        Label estado = EstiloUI.badgeEstadoParticipacion(estadoParticipacion); estado.setMinWidth(120);

        fila.getChildren().addAll(nombres, apellidos, tipo, cedula, proyecto, estado);
        return fila;
    }

    private HBox wrapFiltro(String label, Control control) {
        HBox wrap = new HBox(4);
        wrap.setAlignment(Pos.CENTER_LEFT);
        wrap.getChildren().addAll(EstiloUI.labelSmall(label), control);
        return wrap;
    }
}
