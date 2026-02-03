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
import java.time.LocalDate;
import java.util.List;

/**
 * Módulo de Personal de Investigación para Director.
 * – Listado de personal del proyecto con sus participaciones
 * – Formulario para agregar nuevo personal
 * – Botón para registrar retiro de un personal activo
 */
public class DirPersonal extends VBox {

    private final Director director;
    private final Proyecto proyecto;
    private VBox contenido;

    public DirPersonal(Director director, Proyecto proyecto) {
        super(18);
        this.director = director;
        this.proyecto = proyecto;
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";");

        construir();
    }

    private void construir() {
        getChildren().clear();

        // ── Header ──────────────────────────────────────────
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(EstiloUI.labelTitulo("Personal de Investigación"));
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);

        Button btnAgregar = EstiloUI.botonPrimario("+ Agregar Personal");
        btnAgregar.setOnAction(e -> mostrarFormularioAgregar());
        header.getChildren().add(btnAgregar);
        getChildren().add(header);

        if (proyecto == null) {
            getChildren().add(EstiloUI.labelSmall("No hay proyecto asignado."));
            return;
        }

        // ── Tabla ───────────────────────────────────────────
        VBox tarjeta = EstiloUI.tarjeta();

        HBox tableHeader = new HBox();
        tableHeader.setPadding(new Insets(10, 0, 10, 0));
        tableHeader.setStyle("-fx-border-color: " + EstiloUI.C_DARK + "; -fx-border-width: 0 0 2 0;");
        String[] cols = {"Nombres", "Apellidos", "Tipo", "Cédula", "Correo", "Participación", "Acciones"};
        double[] widths = {140, 140, 90, 120, 170, 110, 130};
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

        cargarPersonal();
    }

    private void cargarPersonal() {
        contenido.getChildren().clear();
        try {
            PersonalDeInvestigacionDAO piDAO = new PersonalDeInvestigacionDAO();
            ParticipacionDAO parDAO          = new ParticipacionDAO();

            List<PersonalDeInvestigacion> personales = piDAO.obtenerTodos().stream()
                    .filter(p -> p.getIdProyecto() == proyecto.getIdProyecto())
                    .collect(java.util.stream.Collectors.toList());

            if (personales.isEmpty()) {
                contenido.getChildren().add(EstiloUI.labelSmall("  No hay personal registrado en este proyecto."));
                return;
            }

            for (PersonalDeInvestigacion p : personales) {
                List<Participacion> participaciones = parDAO.obtenerPorPersonal(p.getCedula());
                Participacion partActual = participaciones.isEmpty() ? null : participaciones.get(0);
                contenido.getChildren().add(crearFila(p, partActual));
            }

        } catch (SQLException e) {
            contenido.getChildren().add(EstiloUI.labelSmall("  Error: " + e.getMessage()));
        }
    }

    private HBox crearFila(PersonalDeInvestigacion p, Participacion part) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(9, 0, 9, 0));
        fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

        Label nombres   = EstiloUI.labelBody(p.getNombres());    nombres.setMinWidth(140); nombres.setPrefWidth(140);
        Label apellidos = EstiloUI.labelBody(p.getApellidos());  apellidos.setMinWidth(140); apellidos.setPrefWidth(140);
        Label tipo      = EstiloUI.labelBody(p.getTipo());       tipo.setMinWidth(90);      tipo.setPrefWidth(90);
        Label cedula    = EstiloUI.labelBody(p.getCedula());     cedula.setMinWidth(120);   cedula.setPrefWidth(120);
        Label correo    = EstiloUI.labelBody(p.getCorreo() != null ? p.getCorreo() : "—"); correo.setMinWidth(170); correo.setPrefWidth(170);

        String estadoStr = (part != null) ? part.getEstado().name() : "—";
        Label estado = EstiloUI.badgeEstadoParticipacion(estadoStr);
        estado.setMinWidth(110);

        // Acciones
        HBox acciones = new HBox(6);
        if (part != null && part.getEstado() == EstadoParticipacion.ACTIVO) {
            Button btnRetiro = EstiloUI.botonSmall("Retiro", EstiloUI.C_RED);
            btnRetiro.setOnAction(e -> registrarRetiro(part, p));
            acciones.getChildren().add(btnRetiro);
        }

        fila.getChildren().addAll(nombres, apellidos, tipo, cedula, correo, estado, acciones);
        return fila;
    }

    // ─── REGISTRAR RETIRO ─────────────────────────────────────────────────
    private void registrarRetiro(Participacion part, PersonalDeInvestigacion p) {
        // Diálogo para motivo
        TextInputDialog dialog = EstiloUI.dialogoEntrada("Registrar Retiro",
                "Indique el motivo del retiro de " + p.getNombresCompletos());
        dialog.getDialogPane().setMinWidth(420);

        dialog.showAndWait().ifPresent(motivo -> {
            if (motivo.trim().isEmpty()) {
                EstiloUI.alertaError("Validación", "El motivo es obligatorio.").showAndWait();
                return;
            }
            try {
                part.registrarRetiro(motivo.trim());
                EstiloUI.alertaInfo("Éxito", "Retiro registrado para " + p.getNombresCompletos()).showAndWait();
                construir(); // refrescar
            } catch (Exception e) {
                EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
            }
        });
    }

    // ─── FORMULARIO AGREGAR PERSONAL ────────────────────────────────────
    private void mostrarFormularioAgregar() {
        if (proyecto == null) {
            EstiloUI.alertaError("Error", "No se tiene proyecto asignado.").showAndWait();
            return;
        }

        VBox form = new VBox(14);
        form.setPadding(new Insets(10));
        form.setMinWidth(500);

        TextField txtNombres   = EstiloUI.crearTextField("Nombres");
        TextField txtApellidos = EstiloUI.crearTextField("Apellidos");
        TextField txtCedula    = EstiloUI.crearTextField("Cédula (10 dígitos)");
        TextField txtCorreo    = EstiloUI.crearTextField("Correo electrónico");

        ComboBox<String> cboTipo = new ComboBox<>();
        cboTipo.getItems().addAll("Asistente", "Ayudante", "Tecnico");
        cboTipo.setValue("Ayudante");
        cboTipo.setPrefHeight(36);

        // Fecha inicio participación
        DatePicker dpInicio = new DatePicker(LocalDate.now());
        dpInicio.setPrefWidth(180);

        form.getChildren().addAll(
                wrapLabel("Nombres:", txtNombres),
                wrapLabel("Apellidos:", txtApellidos),
                wrapLabel("Cédula:", txtCedula),
                wrapLabel("Correo:", txtCorreo),
                wrapLabel("Tipo:", cboTipo),
                wrapLabel("Fecha inicio participación:", dpInicio)
        );

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Agregar Personal");
        dialog.setHeaderText("Nuevo personal de investigación – Proyecto: " + proyecto.getNombre());
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(540);
        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Guardar"),
                ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn.getText().equals("Guardar")) {
                agregarPersonal(txtNombres, txtApellidos, txtCedula, txtCorreo, cboTipo, dpInicio);
            }
        });
    }

    private void agregarPersonal(TextField nombres, TextField apellidos, TextField cedula,
                                  TextField correo, ComboBox<String> tipo, DatePicker dpInicio) {
        // Validaciones
        if (nombres.getText().trim().isEmpty() || apellidos.getText().trim().isEmpty()) {
            EstiloUI.alertaError("Validación", "Nombres y apellidos son obligatorios.").showAndWait();
            return;
        }
        if (cedula.getText().trim().length() != 10) {
            EstiloUI.alertaError("Validación", "La cédula debe tener 10 dígitos.").showAndWait();
            return;
        }

        try {
            // Crear objeto personal según tipo
            PersonalDeInvestigacion personal;
            switch (tipo.getValue()) {
                case "Asistente": personal = new Logica.Entidades.Asistente(); break;
                case "Tecnico":   personal = new Logica.Entidades.Tecnico();   break;
                default:          personal = new Logica.Entidades.Ayudante();  break;
            }

            personal.setNombres(nombres.getText().trim());
            personal.setApellidos(apellidos.getText().trim());
            personal.setCedula(cedula.getText().trim());
            personal.setCorreo(correo.getText().trim());

            LocalDate fechaInicio = dpInicio.getValue() != null ? dpInicio.getValue() : LocalDate.now();

            // Usar lógica del Director para registrar (crea usuario, personal, participación, notificación)
            director.registrarPersonalDeInvestigacion(personal, proyecto.getIdProyecto(), fechaInicio);

            EstiloUI.alertaInfo("Éxito",
                    "Personal registrado exitosamente.\n" +
                    "Se ha creado un usuario con contraseña temporal.\n" +
                    "El personal debe cambiarla en su primer inicio de sesión.").showAndWait();

            construir(); // refrescar

        } catch (SQLException e) {
            EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
        }
    }

    private HBox wrapLabel(String label, Control control) {
        HBox wrap = new HBox(8);
        wrap.setAlignment(Pos.CENTER_LEFT);
        Label lbl = EstiloUI.labelSmall(label);
        lbl.setMinWidth(180);
        control.setPrefWidth(240);
        wrap.getChildren().addAll(lbl, control);
        return wrap;
    }
}
