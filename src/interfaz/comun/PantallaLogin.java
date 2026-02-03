package interfaz.comun;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import Logica.DAO.*;
import Logica.Entidades.*;
import Logica.Enumeraciones.RolSistema;
import interfaz.director.PantallaPrincipalDirector;
import interfaz.jefatura.PantallaPrincipalJefatura;
import interfaz.personal.PantallaPrincipalPersonal;

import java.sql.SQLException;

/**
 * Pantalla de inicio de sesión.
 * Después del login exitoso detecta si se requiere cambio de contraseña
 * (primer login de director o personal).
 */
public class PantallaLogin extends VBox {

    private final TextField txtUsername;
    private final PasswordField txtContrasena;
    private final Label lblMensaje;

    public PantallaLogin() {
        super();
        setAlignment(Pos.CENTER);
        setSpacing(0);
        setPrefWidth(1280);
        setPrefHeight(750);
        setStyle("-fx-background-color: linear-gradient(to bottom, " +
                EstiloUI.C_VERY_LIGHT + " 0%, white 50%, " + EstiloUI.C_VERY_LIGHT + " 100%);");

        // ── Panel central ──────────────────────────────────────
        VBox panel = new VBox(24);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(48, 56, 48, 56));
        panel.setMinWidth(420);
        panel.setMaxWidth(420);
        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-radius: 14;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-drop-shadow-blur-radius: 12;" +
                        "-fx-drop-shadow-color: rgba(0,0,0,0.08);" +
                        "-fx-drop-shadow-offset-x: 0;" +
                        "-fx-drop-shadow-offset-y: 4;"
        );

        // Logo / Marca
        HBox logoArea = new HBox(10);
        logoArea.setAlignment(Pos.CENTER);
        Label logoCírculo = new Label("SG");
        logoCírculo.setStyle(
                "-fx-background-color: " + EstiloUI.C_DARK + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-width: 52;" +
                        "-fx-height: 52;" +
                        "-fx-border-radius: 50;" +
                        "-fx-background-radius: 50;" +
                        "-fx-alignment: center;"
        );
        Label logoTexto = new Label("Sistema de Gestión");
        logoTexto.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        logoTexto.setTextFill(Color.web(EstiloUI.C_VERY_DARK));
        logoArea.getChildren().addAll(logoCírculo, logoTexto);

        // Subtitulo
        Label subTitulo = new Label("Proyectos de Investigación");
        subTitulo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        subTitulo.setTextFill(Color.web(EstiloUI.C_GRAY_DARK));
        subTitulo.setTextAlignment(TextAlignment.CENTER);

        // Separador
        Separator sep = EstiloUI.separador();

        // Username
        Label lblUser = EstiloUI.labelSmall("USUARIO");
        txtUsername = EstiloUI.crearTextField("Ingrese su usuario");
        txtUsername.setPrefWidth(308);

        // Contraseña
        Label lblPass = EstiloUI.labelSmall("CONTRASEÑA");
        txtContrasena = EstiloUI.crearPasswordField("Ingrese su contraseña");
        txtContrasena.setPrefWidth(308);
        txtContrasena.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) iniciarSesion();
        });

        // Mensaje de error
        lblMensaje = new Label("");
        lblMensaje.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        lblMensaje.setTextFill(Color.web(EstiloUI.C_RED));
        lblMensaje.setVisible(false);
        lblMensaje.setMinHeight(18);

        // Botón login
        Button btnLogin = EstiloUI.botonPrimario("Iniciar Sesión");
        btnLogin.setPrefWidth(308);
        btnLogin.setPrefHeight(42);
        btnLogin.setStyle(btnLogin.getStyle() + "-fx-font-size: 14px;");
        btnLogin.setOnAction(e -> iniciarSesion());

        // Footer
        Label footer = new Label("Sistema de Gestión de Proyectos de Investigación");
        footer.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        footer.setTextFill(Color.web(EstiloUI.C_GRAY_MID));

        panel.getChildren().addAll(
                logoArea, subTitulo, sep,
                lblUser, txtUsername,
                lblPass, txtContrasena,
                lblMensaje,
                btnLogin
        );

        // Contenedor final
        VBox wrapper = new VBox();
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefWidth(1280);
        wrapper.setPrefHeight(750);
        wrapper.getChildren().addAll(panel, footer);
        VBox.setMargin(footer, new Insets(24, 0, 0, 0));

        getChildren().add(wrapper);
    }

    public VBox getLayout() {
        return this;
    }

    // ─── LÓGICA DE LOGIN ────────────────────────────────────────────────
    private void iniciarSesion() {
        String username   = txtUsername.getText().trim();
        String contrasena = txtContrasena.getText();

        if (username.isEmpty() || contrasena.isEmpty()) {
            mostrarMensaje("Por favor complete todos los campos.");
            return;
        }

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario usuario = usuarioDAO.autenticar(username, contrasena);

            if (usuario == null) {
                mostrarMensaje("Usuario o contraseña incorrectos.");
                return;
            }

            // Determinar rol y cargar entidad correspondiente
            RolSistema rol = RolSistema.fromString(usuario.getRol());

            switch (rol) {
                case JEFATURA:
                    cargarJefatura(usuario);
                    break;
                case DIRECTOR:
                    cargarDirector(usuario);
                    break;
                case PERSONAL:
                    cargarPersonal(usuario);
                    break;
                default:
                    mostrarMensaje("Rol no reconocido.");
            }

        } catch (SQLException e) {
            mostrarMensaje("Error de conexión: " + e.getMessage());
        }
    }

    private void cargarJefatura(Usuario usuario) throws SQLException {
        JefaturaDAO jDAO = new JefaturaDAO();
        Jefatura jefatura = jDAO.obtenerPorIdUsuario(usuario.getIdUsuario());

        if (jefatura == null) {
            mostrarMensaje("Datos de jefatura no encontrados.");
            return;
        }

        // Navegar a pantalla principal de jefatura
        PantallaPrincipalJefatura pantalla = new PantallaPrincipalJefatura(jefatura);
        Navigador.cambiarPantalla(pantalla);
    }

    private void cargarDirector(Usuario usuario) throws SQLException {
        DirectorDAO dDAO = new DirectorDAO();
        Director director = dDAO.obtenerPorIdUsuario(usuario.getIdUsuario());

        if (director == null) {
            mostrarMensaje("Datos del director no encontrados.");
            return;
        }

        // Verificar si debe cambiar contraseña (primer login)
        if (director.debeActualizarContrasena()) {
            mostrarPantallaCambioContrasena(director, "DIRECTOR");
            return;
        }

        // Obtener proyecto del director
        ProyectoDAO pDAO = new ProyectoDAO();
        var proyectos = pDAO.obtenerPorDirector(director.getCedula());

        PantallaPrincipalDirector pantalla = new PantallaPrincipalDirector(
                director, proyectos.isEmpty() ? null : proyectos.get(0));
        Navigador.cambiarPantalla(pantalla);
    }

    private void cargarPersonal(Usuario usuario) throws SQLException {
        PersonalDeInvestigacionDAO piDAO = new PersonalDeInvestigacionDAO();
        // Buscar personal por id_usuario
        var todos = piDAO.obtenerTodos();
        PersonalDeInvestigacion personal = null;
        for (var p : todos) {
            if (p.getIdUsuario() == usuario.getIdUsuario()) {
                personal = p;
                break;
            }
        }

        if (personal == null) {
            mostrarMensaje("Datos del personal no encontrados.");
            return;
        }

        // Verificar si debe cambiar contraseña (primer login)
        if (personal.debeActualizarContrasena()) {
            mostrarPantallaCambioContrasena(personal, "PERSONAL");
            return;
        }

        // Obtener proyecto del personal
        ProyectoDAO proyDAO = new ProyectoDAO();
        Proyecto proyecto = proyDAO.obtenerPorId(personal.getIdProyecto());

        PantallaPrincipalPersonal pantalla = new PantallaPrincipalPersonal(personal, proyecto);
        Navigador.cambiarPantalla(pantalla);
    }

    /**
     * Muestra un diálogo modal para cambiar la contraseña en primer login.
     */
    private void mostrarPantallaCambioContrasena(Object entidad, String tipo) {
        PantallaCambioContrasena pantalla = new PantallaCambioContrasena(entidad, tipo);
        Navigador.cambiarPantalla(pantalla);
    }

    private void mostrarMensaje(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setVisible(true);
    }
}