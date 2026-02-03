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

/**
 * Módulo de Administración para Jefatura.
 * Permite crear nuevos usuarios de tipo JEFATURA.
 * Muestra listado de usuarios de jefatura actuales.
 */
public class JefAdministracion extends VBox {

    private VBox listaUsuarios;

    public JefAdministracion() {
        super(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";");

        // ── Header ──────────────────────────────────────────
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(EstiloUI.labelTitulo("Administración de Usuarios"));
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);

        Button btnCrear = EstiloUI.botonPrimario("+ Crear Jefatura");
        btnCrear.setOnAction(e -> mostrarFormularioCrear());
        header.getChildren().add(btnCrear);
        getChildren().add(header);

        // ── Descripción ──────────────────────────────────────
        Label desc = EstiloUI.labelSmall("Desde esta sección puede crear nuevos usuarios con rol de Jefatura.");
        getChildren().add(desc);

        // ── Listado usuarios jefatura ────────────────────────
        VBox tarjeta = EstiloUI.tarjeta();
        tarjeta.getChildren().add(EstiloUI.labelSubtitulo("Usuarios de Jefatura"));

        // Header tabla
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
    }

    private void cargarListado() {
        listaUsuarios.getChildren().clear();
        try {
            JefaturaDAO jDAO = new JefaturaDAO();
            UsuarioDAO  uDAO = new UsuarioDAO();
            List<Jefatura> jefaturas = jDAO.obtenerTodos();

            for (Jefatura j : jefaturas) {
                // Obtener username
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

    // ─── FORMULARIO CREAR USUARIO JEFATURA ───────────────────────────────
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
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Crear"),
                ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn.getText().equals("Crear")) {
                crearUsuarioJefatura(txtNombres, txtApellidos, txtCedula, txtCorreo, txtUsername);
            }
        });
    }

    private void crearUsuarioJefatura(TextField nombres, TextField apellidos,
                                       TextField cedula, TextField correo, TextField username) {
        // Validaciones
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

            // Contraseña por defecto
            String contrasenaDefecto = UsuarioDAO.generarContrasenaDefecto(cedula.getText().trim());

            // Crear usuario
            Usuario usuario = new Usuario(username.getText().trim(), contrasenaDefecto, "JEFATURA");
            int idUsuario = uDAO.guardar(usuario);

            if (idUsuario == -1) {
                EstiloUI.alertaError("Error", "No se pudo crear el usuario.").showAndWait();
                return;
            }

            // Crear entidad Jefatura
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

            cargarListado(); // refrescar

        } catch (SQLException e) {
            EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
        }
    }

    private HBox wrapLabel(String label, Control control) {
        HBox wrap = new HBox(8);
        wrap.setAlignment(Pos.CENTER_LEFT);
        Label lbl = EstiloUI.labelSmall(label);
        lbl.setMinWidth(90);
        control.setPrefWidth(300);
        wrap.getChildren().addAll(lbl, control);
        return wrap;
    }
}
