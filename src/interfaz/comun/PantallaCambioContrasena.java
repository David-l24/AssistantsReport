package interfaz.comun;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import Logica.Entidades.*;
import Logica.DAO.*;
import interfaz.director.PantallaPrincipalDirector;
import interfaz.personal.PantallaPrincipalPersonal;

import java.sql.SQLException;

/**
 * Pantalla que se muestra cuando un director o personal tiene la contraseña
 * por defecto y debe cambiarla antes de acceder al sistema.
 */
public class PantallaCambioContrasena extends VBox {

    private final Object entidad;   // Director o PersonalDeInvestigacion
    private final String tipoRol;   // "DIRECTOR" o "PERSONAL"

    private final PasswordField txtNueva;
    private final PasswordField txtConfirma;
    private final Label lblMensaje;

    public PantallaCambioContrasena(Object entidad, String tipoRol) {
        super();
        this.entidad  = entidad;
        this.tipoRol  = tipoRol;

        setAlignment(Pos.CENTER);
        setSpacing(0);
        setPrefWidth(1280);
        setPrefHeight(750);
        setStyle("-fx-background-color: linear-gradient(to bottom, " +
                EstiloUI.C_VERY_LIGHT + " 0%, white 50%, " + EstiloUI.C_VERY_LIGHT + " 100%);");

        // ── Panel central ──────────────────────────────────────
        VBox panel = new VBox(22);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(44, 52, 44, 52));
        panel.setMinWidth(440);
        panel.setMaxWidth(440);
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

        // Icono aviso
        Label iconAviso = new Label("\u26A0");
        iconAviso.setStyle(
                "-fx-background-color: " + EstiloUI.C_ORANGE_LIGHT + ";" +
                        "-fx-text-fill: " + EstiloUI.C_ORANGE + ";" +
                        "-fx-font-size: 26px;" +
                        "-fx-width: 58;" +
                        "-fx-height: 58;" +
                        "-fx-border-radius: 50;" +
                        "-fx-background-radius: 50;" +
                        "-fx-alignment: center;"
        );

        Label titulo = EstiloUI.labelTitulo("Cambio de Contraseña");
        titulo.setAlignment(Pos.CENTER);

        Label descripcion = new Label(
                "Por seguridad, debe cambiar su contraseña antes de continuar. " +
                        "Esta es su primera sesión en el sistema.");
        descripcion.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        descripcion.setTextFill(Color.web(EstiloUI.C_GRAY_DARK));
        descripcion.setWrapText(true);
        descripcion.setMaxWidth(336);

        Separator sep = EstiloUI.separador();

        // Nueva contraseña
        Label lblNueva = EstiloUI.labelSmall("NUEVA CONTRASEÑA");
        txtNueva = EstiloUI.crearPasswordField("Mínimo 8 caracteres");
        txtNueva.setPrefWidth(336);

        // Confirmar
        Label lblConfirma = EstiloUI.labelSmall("CONFIRMAR CONTRASEÑA");
        txtConfirma = EstiloUI.crearPasswordField("Repita su nueva contraseña");
        txtConfirma.setPrefWidth(336);

        // Criterios
        Label criterios = new Label(
                "La contraseña debe tener al menos 8 caracteres, una letra " +
                        "mayúscula, una minúscula y un número.");
        criterios.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        criterios.setTextFill(Color.web(EstiloUI.C_GRAY_MID));
        criterios.setWrapText(true);
        criterios.setMaxWidth(336);

        // Mensaje error
        lblMensaje = new Label("");
        lblMensaje.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        lblMensaje.setTextFill(Color.web(EstiloUI.C_RED));
        lblMensaje.setVisible(false);

        // Botón confirmar
        Button btnConfirmar = EstiloUI.botonPrimario("Actualizar Contraseña");
        btnConfirmar.setPrefWidth(336);
        btnConfirmar.setPrefHeight(42);
        btnConfirmar.setOnAction(e -> cambiarContrasena());

        panel.getChildren().addAll(
                iconAviso, titulo, descripcion, sep,
                lblNueva, txtNueva,
                lblConfirma, txtConfirma,
                criterios, lblMensaje,
                btnConfirmar
        );

        VBox wrapper = new VBox();
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefWidth(1280);
        wrapper.setPrefHeight(750);
        wrapper.getChildren().add(panel);

        getChildren().add(wrapper);
    }

    // ─── LÓGICA ──────────────────────────────────────────────────────────
    private void cambiarContrasena() {
        String nueva    = txtNueva.getText();
        String confirma = txtConfirma.getText();

        // Validaciones
        if (nueva.isEmpty() || confirma.isEmpty()) {
            mostrarMensaje("Ambos campos son obligatorios.");
            return;
        }
        if (!nueva.equals(confirma)) {
            mostrarMensaje("Las contraseñas no coinciden.");
            return;
        }
        if (!validarFortaleza(nueva)) {
            mostrarMensaje("La contraseña no cumple los requisitos de seguridad.");
            return;
        }

        try {
            if ("DIRECTOR".equals(tipoRol)) {
                Director director = (Director) entidad;
                director.actualizarContrasena(nueva);

                // Redirigir al sistema
                ProyectoDAO pDAO = new ProyectoDAO();
                var proyectos = pDAO.obtenerPorDirector(director.getCedula());
                PantallaPrincipalDirector pantalla = new PantallaPrincipalDirector(
                        director, proyectos.isEmpty() ? null : proyectos.get(0));
                Navigador.cambiarPantalla(pantalla);

            } else if ("PERSONAL".equals(tipoRol)) {
                PersonalDeInvestigacion personal = (PersonalDeInvestigacion) entidad;
                personal.actualizarContrasena(nueva);

                // Obtener proyecto del personal
                ProyectoDAO proyDAO = new ProyectoDAO();
                Proyecto proyecto = proyDAO.obtenerPorId(personal.getIdProyecto());

                PantallaPrincipalPersonal pantalla = new PantallaPrincipalPersonal(personal, proyecto);
                Navigador.cambiarPantalla(pantalla);
            }

        } catch (SQLException e) {
            mostrarMensaje("Error al actualizar contraseña: " + e.getMessage());
        }
    }

    private boolean validarFortaleza(String pass) {
        if (pass.length() < 8) return false;
        boolean mayuscula = false, minuscula = false, numero = false;
        for (char c : pass.toCharArray()) {
            if (Character.isUpperCase(c)) mayuscula = true;
            if (Character.isLowerCase(c)) minuscula = true;
            if (Character.isDigit(c))     numero    = true;
        }
        return mayuscula && minuscula && numero;
    }

    private void mostrarMensaje(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setVisible(true);
    }
}