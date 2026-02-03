package interfaz.jefatura;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import Logica.DAO.*;
import Logica.Entidades.*;
import interfaz.comun.EstiloUI;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Módulo de Administración para Jefatura.
 *  – Sección superior: usuarios de Jefatura (sin cambios).
 *  – Sección inferior (Req 3): creación y listado de Periodos Académicos.
 */
public class JefAdministracion extends VBox {

    private VBox listaUsuarios;
    private VBox listaPeriodos;   // Req 3

    public JefAdministracion() {
        super(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";");

        // ═══════════════════════════════════════════════
        //  SECCIÓN 1 – USUARIOS DE JEFATURA
        // ═══════════════════════════════════════════════
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(EstiloUI.labelTitulo("Administración de Usuarios"));
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);

        Button btnCrear = EstiloUI.botonPrimario("+ Crear Jefatura");
        btnCrear.setOnAction(e -> mostrarFormularioCrear());
        header.getChildren().add(btnCrear);
        getChildren().add(header);

        Label desc = EstiloUI.labelSmall("Desde esta sección puede crear nuevos usuarios con rol de Jefatura.");
        getChildren().add(desc);

        VBox tarjeta = EstiloUI.tarjeta();
        tarjeta.getChildren().add(EstiloUI.labelSubtitulo("Usuarios de Jefatura"));

        HBox tableHeader = new HBox();
        tableHeader.setPadding(new Insets(10, 0, 10, 0));
        tableHeader.setStyle("-fx-border-color: " + EstiloUI.C_DARK + "; -fx-border-width: 0 0 2 0;");
        String[] cols = {"Nombres", "Apellidos", "Cédula", "Correo", "Username"};
        double[] widths = {160, 160, 120, 200, 180};
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + EstiloUI.C_GRAY_DARK + ";");
            lbl.setMinWidth(widths[i]); lbl.setPrefWidth(widths[i]);
            tableHeader.getChildren().add(lbl);
        }
        tarjeta.getChildren().add(tableHeader);

        listaUsuarios = new VBox(0);
        tarjeta.getChildren().add(listaUsuarios);
        getChildren().add(tarjeta);

        cargarListado();

        // ═══════════════════════════════════════════════
        //  SECCIÓN 2 – PERIODOS ACADÉMICOS  (Req 3)
        // ═══════════════════════════════════════════════
        getChildren().add(EstiloUI.separador());

        HBox headerPA = new HBox(16);
        headerPA.setAlignment(Pos.CENTER_LEFT);
        headerPA.getChildren().add(EstiloUI.labelTitulo("Periodos Académicos"));
        HBox.setHgrow(headerPA.getChildren().get(0), Priority.ALWAYS);

        Button btnCrearPA = EstiloUI.botonPrimario("+ Crear Periodo");
        btnCrearPA.setOnAction(e -> mostrarFormularioCrearPeriodo());
        headerPA.getChildren().add(btnCrearPA);
        getChildren().add(headerPA);

        VBox tarjetaPA = EstiloUI.tarjeta();
        tarjetaPA.getChildren().add(EstiloUI.labelSubtitulo("Listado de Periodos"));

        HBox tableHeaderPA = new HBox();
        tableHeaderPA.setPadding(new Insets(10, 0, 10, 0));
        tableHeaderPA.setStyle("-fx-border-color: " + EstiloUI.C_DARK + "; -fx-border-width: 0 0 2 0;");
        String[] colsPA  = {"Código", "Fecha Inicio", "Fecha Fin", "Fecha Mitad"};
        double[] widthsPA = {100,      150,            150,         150};
        for (int i = 0; i < colsPA.length; i++) {
            Label lbl = new Label(colsPA[i]);
            lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + EstiloUI.C_GRAY_DARK + ";");
            lbl.setMinWidth(widthsPA[i]); lbl.setPrefWidth(widthsPA[i]);
            tableHeaderPA.getChildren().add(lbl);
        }
        tarjetaPA.getChildren().add(tableHeaderPA);

        listaPeriodos = new VBox(0);
        tarjetaPA.getChildren().add(listaPeriodos);
        getChildren().add(tarjetaPA);

        cargarListadoPeriodos();
    }

    // ─── CARGA USUARIOS ──────────────────────────────────────────────────
    private void cargarListado() {
        listaUsuarios.getChildren().clear();
        try {
            JefaturaDAO jDAO = new JefaturaDAO();
            UsuarioDAO  uDAO = new UsuarioDAO();
            List<Jefatura> jefaturas = jDAO.obtenerTodos();

            for (Jefatura j : jefaturas) {
                Usuario u = uDAO.obtenerPorId(j.getIdUsuario());
                String username = (u != null) ? u.getUsername() : "—";

                HBox fila = new HBox();
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setPadding(new Insets(9, 0, 9, 0));
                fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

                Label nombres  = EstiloUI.labelBody(j.getNombres());   nombres.setMinWidth(160);  nombres.setPrefWidth(160);
                Label apellidos = EstiloUI.labelBody(j.getApellidos()); apellidos.setMinWidth(160); apellidos.setPrefWidth(160);
                Label cedula   = EstiloUI.labelBody(j.getCedula());     cedula.setMinWidth(120);   cedula.setPrefWidth(120);
                Label correo   = EstiloUI.labelBody(j.getCorreo() != null ? j.getCorreo() : "—"); correo.setMinWidth(200); correo.setPrefWidth(200);
                Label user     = EstiloUI.labelBody(username);          user.setMinWidth(180);     user.setPrefWidth(180);

                fila.getChildren().addAll(nombres, apellidos, cedula, correo, user);
                listaUsuarios.getChildren().add(fila);
            }

            if (listaUsuarios.getChildren().isEmpty()) {
                listaUsuarios.getChildren().add(EstiloUI.labelSmall("  No hay usuarios de jefatura registrados."));
            }
        } catch (SQLException e) {
            listaUsuarios.getChildren().add(EstiloUI.labelSmall("  Error: " + e.getMessage()));
        }
    }

    // ─── CARGA PERIODOS (Req 3) ──────────────────────────────────────────
    private void cargarListadoPeriodos() {
        listaPeriodos.getChildren().clear();
        try {
            PeriodoAcademicoDAO paDAO = new PeriodoAcademicoDAO();
            List<PeriodoAcademico> periodos = paDAO.obtenerTodos();

            for (PeriodoAcademico pa : periodos) {
                HBox fila = new HBox();
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setPadding(new Insets(9, 0, 9, 0));
                fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

                Label codigo = EstiloUI.labelBody(pa.getCodigo());                        codigo.setMinWidth(100); codigo.setPrefWidth(100);
                Label inicio = EstiloUI.labelBody(pa.getFechaInicio().toString());        inicio.setMinWidth(150); inicio.setPrefWidth(150);
                Label fin    = EstiloUI.labelBody(pa.getFechaFin().toString());           fin.setMinWidth(150);    fin.setPrefWidth(150);
                Label mitad  = EstiloUI.labelBody(pa.getFechaMitad() != null ? pa.getFechaMitad().toString() : "—");
                mitad.setMinWidth(150); mitad.setPrefWidth(150);

                fila.getChildren().addAll(codigo, inicio, fin, mitad);
                listaPeriodos.getChildren().add(fila);
            }

            if (listaPeriodos.getChildren().isEmpty()) {
                listaPeriodos.getChildren().add(EstiloUI.labelSmall("  No hay periodos académicos registrados."));
            }
        } catch (SQLException e) {
            listaPeriodos.getChildren().add(EstiloUI.labelSmall("  Error: " + e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  FORMULARIO – CREAR USUARIO JEFATURA  (sin cambios respecto al original)
    // ═══════════════════════════════════════════════════════════════════════
    private void mostrarFormularioCrear() {
        VBox form = new VBox(14);
        form.setPadding(new Insets(10));
        form.setMinWidth(460);

        TextField txtNombres   = EstiloUI.crearTextField("Nombres");
        TextField txtApellidos = EstiloUI.crearTextField("Apellidos");
        TextField txtCedula    = EstiloUI.crearTextField("Cédula (10 dígitos)");
        TextField txtCorreo    = EstiloUI.crearTextField("Correo electrónico");
        TextField txtUsername  = EstiloUI.crearTextField("Username (ej: jef.apellido)");

        form.getChildren().addAll(
                wrapLabel("Nombres:", txtNombres),
                wrapLabel("Apellidos:", txtApellidos),
                wrapLabel("Cédula:", txtCedula),
                wrapLabel("Correo:", txtCorreo),
                wrapLabel("Username:", txtUsername)
        );

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Crear Usuario de Jefatura");
        dialog.setHeaderText("Nuevo usuario con rol JEFATURA");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(500);
        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().getButtonTypes().addAll(new ButtonType("Crear"), ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn.getText().equals("Crear")) {
                crearUsuarioJefatura(txtNombres, txtApellidos, txtCedula, txtCorreo, txtUsername);
            }
        });
    }

    private void crearUsuarioJefatura(TextField nombres, TextField apellidos,
                                      TextField cedula, TextField correo, TextField username) {
        if (nombres.getText().trim().isEmpty() || apellidos.getText().trim().isEmpty()) {
            EstiloUI.alertaError("Validación", "Nombres y apellidos son obligatorios.").showAndWait();
            return;
        }
        if (cedula.getText().trim().length() != 10) {
            EstiloUI.alertaError("Validación", "La cédula debe tener 10 dígitos.").showAndWait();
            return;
        }
        if (username.getText().trim().isEmpty()) {
            EstiloUI.alertaError("Validación", "El username es obligatorio.").showAndWait();
            return;
        }

        try {
            UsuarioDAO uDAO  = new UsuarioDAO();
            JefaturaDAO jDAO = new JefaturaDAO();

            String contrasenaDefecto = UsuarioDAO.generarContrasenaDefecto(cedula.getText().trim());
            Usuario usuario = new Usuario(username.getText().trim(), contrasenaDefecto, "JEFATURA");
            int idUsuario = uDAO.guardar(usuario);

            if (idUsuario == -1) {
                EstiloUI.alertaError("Error", "No se pudo crear el usuario.").showAndWait();
                return;
            }

            Jefatura nueva = new Jefatura(
                    nombres.getText().trim(),
                    apellidos.getText().trim(),
                    correo.getText().trim(),
                    cedula.getText().trim()
            );
            nueva.setIdUsuario(idUsuario);
            jDAO.guardar(nueva);

            EstiloUI.alertaInfo("Éxito",
                    "Usuario de jefatura creado.\n" +
                            "Username: " + username.getText().trim() + "\n" +
                            "Contraseña temporal: " + contrasenaDefecto).showAndWait();

            cargarListado();
        } catch (SQLException e) {
            EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  FORMULARIO – CREAR PERIODO ACADÉMICO  (Req 3)
    // ═══════════════════════════════════════════════════════════════════════
    private void mostrarFormularioCrearPeriodo() {
        VBox form = new VBox(14);
        form.setPadding(new Insets(10));
        form.setMinWidth(460);

        TextField txtCodigo = EstiloUI.crearTextField("Código (máx 5 caracteres)");
        txtCodigo.setOnKeyReleased(e -> {
            if (txtCodigo.getText().length() > 5) {
                txtCodigo.setText(txtCodigo.getText().substring(0, 5));
            }
        });

        DatePicker dpInicio = new DatePicker();
        dpInicio.setPromptText("dd/mm/yyyy");
        dpInicio.setPrefWidth(240);

        DatePicker dpFin = new DatePicker();
        dpFin.setPromptText("dd/mm/yyyy");
        dpFin.setPrefWidth(240);

        DatePicker dpMitad = new DatePicker();
        dpMitad.setPromptText("Opcional – dd/mm/yyyy");
        dpMitad.setPrefWidth(240);

        form.getChildren().addAll(
                wrapLabel("Código:",      txtCodigo),
                wrapLabel("Fecha Inicio:", dpInicio),
                wrapLabel("Fecha Fin:",    dpFin),
                wrapLabel("Fecha Mitad:",  dpMitad)
        );

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Crear Periodo Académico");
        dialog.setHeaderText("Nuevo periodo académico");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(500);
        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().getButtonTypes().addAll(new ButtonType("Crear"), ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn.getText().equals("Crear")) {
                crearPeriodoAcademico(txtCodigo, dpInicio, dpFin, dpMitad);
            }
        });
    }

    private void crearPeriodoAcademico(TextField txtCodigo,
                                       DatePicker dpInicio, DatePicker dpFin, DatePicker dpMitad) {
        // ── Validaciones ──
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            EstiloUI.alertaError("Validación", "El código es obligatorio.").showAndWait();
            return;
        }
        if (codigo.length() > 5) {
            EstiloUI.alertaError("Validación", "El código no puede tener más de 5 caracteres.").showAndWait();
            return;
        }

        LocalDate inicio = dpInicio.getValue();
        LocalDate fin    = dpFin.getValue();
        LocalDate mitad  = dpMitad.getValue();   // puede ser null

        if (inicio == null || fin == null) {
            EstiloUI.alertaError("Validación", "Fecha de inicio y fecha de fin son obligatorias.").showAndWait();
            return;
        }
        if (!fin.isAfter(inicio)) {
            EstiloUI.alertaError("Validación", "La fecha de fin debe ser posterior a la fecha de inicio.").showAndWait();
            return;
        }
        if (mitad != null && (!mitad.isAfter(inicio) || !mitad.isBefore(fin))) {
            EstiloUI.alertaError("Validación", "La fecha de mitad debe estar entre inicio y fin.").showAndWait();
            return;
        }

        try {
            // ── Verificar código duplicado ──
            PeriodoAcademicoDAO paDAO = new PeriodoAcademicoDAO();
            PeriodoAcademico existente = paDAO.obtenerPorCodigo(codigo);
            if (existente != null) {
                EstiloUI.alertaError("Validación", "Ya existe un periodo académico con el código: " + codigo).showAndWait();
                return;
            }

            // ── Crear y persistir ──
            PeriodoAcademico nuevo = new PeriodoAcademico();
            nuevo.setCodigo(codigo);
            nuevo.setFechaInicio(inicio);
            nuevo.setFechaFin(fin);
            nuevo.setFechaMitad(mitad);   // null si no se indicó

            paDAO.guardar(nuevo);
            EstiloUI.alertaInfo("Éxito", "Periodo académico \"" + codigo + "\" creado exitosamente.").showAndWait();

            cargarListadoPeriodos();   // refrescar listado

        } catch (SQLException e) {
            EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
        }
    }

    // ─── Utilidad ────────────────────────────────────────────────────────
    private HBox wrapLabel(String label, Control control) {
        HBox wrap = new HBox(8);
        wrap.setAlignment(Pos.CENTER_LEFT);
        Label lbl = EstiloUI.labelSmall(label);
        lbl.setMinWidth(120);
        control.setPrefWidth(240);
        wrap.getChildren().addAll(lbl, control);
        return wrap;
    }
}