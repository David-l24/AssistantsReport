package interfaz.comun;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Clase centralizada de estilos y constructores de UI.
 * Paleta Mint Green:
 *   #99E89D  – muy claro (fondos suaves)
 *   #73C883  – claro      (accentos secundarios)
 *   #4DA768  – medio      (botones primarios)
 *   #26874E  – oscuro     (barra nav, headers)
 *   #006633  – muy oscuro (texto oscuro, bordes fuertes)
 * Resto: blanco predominante, grises suaves para separadores.
 */
public class EstiloUI {

    // ─── COLORES ────────────────────────────────────────────────────────
    public static final String C_VERY_LIGHT  = "#99E89D";  // fondos tarjetas suaves
    public static final String C_LIGHT       = "#73C883";  // badges, accentos
    public static final String C_MEDIUM      = "#4DA768";  // botones primarios
    public static final String C_DARK        = "#26874E";  // navbar, headers
    public static final String C_VERY_DARK   = "#006633";  // texto titles, borde fuerte
    public static final String C_WHITE       = "#FFFFFF";
    public static final String C_OFF_WHITE   = "#F7F7F7";  // fondo principal
    public static final String C_GRAY_LIGHT  = "#E8E8E8";  // separadores, bordes suaves
    public static final String C_GRAY_MID    = "#BFBFBF";  // placeholder text
    public static final String C_GRAY_DARK   = "#555555";  // texto secundario
    public static final String C_TEXT_MAIN   = "#2A2A2A";  // texto principal
    public static final String C_RED         = "#E74C3C";  // alertas / peligro
    public static final String C_RED_LIGHT   = "#FADBD8";
    public static final String C_ORANGE      = "#F39C12";  // avisos
    public static final String C_ORANGE_LIGHT= "#FEF0D1";

    // ─── FUENTES ────────────────────────────────────────────────────────
    public static final Font FONT_TITLE     = Font.font("Segoe UI", FontWeight.BOLD, 22);
    public static final Font FONT_SUBTITLE  = Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16);
    public static final Font FONT_SECTION   = Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14);
    public static final Font FONT_BODY      = Font.font("Segoe UI", FontWeight.NORMAL, 13);
    public static final Font FONT_SMALL     = Font.font("Segoe UI", FontWeight.NORMAL, 12);
    public static final Font FONT_NAV       = Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13);

    // ─── CONSTRUCTORES DE CONTROLES ─────────────────────────────────────

    /** Botón primario verde */
    public static Button botonPrimario(String texto) {
        Button btn = new Button(texto);
        btn.setStyle(
                "-fx-background-color: " + C_MEDIUM + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-padding: 8 20;" +
                        "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace(C_MEDIUM, C_DARK)));
        btn.setOnMouseExited(e  -> btn.setStyle(btn.getStyle().replace(C_DARK, C_MEDIUM)));
        return btn;
    }

    /** Botón secundario (borde verde, fondo blanco) */
    public static Button botonSecundario(String texto) {
        Button btn = new Button(texto);
        btn.setStyle(
                "-fx-background-color: white;" +
                        "-fx-text-fill: " + C_VERY_DARK + ";" +
                        "-fx-border-color: " + C_MEDIUM + ";" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-padding: 8 18;" +
                        "-fx-cursor: hand;"
        );
        return btn;
    }

    /** Botón de peligro (rojo) */
    public static Button botonPeligro(String texto) {
        Button btn = new Button(texto);
        btn.setStyle(
                "-fx-background-color: " + C_RED + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-padding: 8 18;" +
                        "-fx-cursor: hand;"
        );
        return btn;
    }

    /** Botón pequeño para acciones inline */
    public static Button botonSmall(String texto, String color) {
        Button btn = new Button(texto);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 4 10;" +
                        "-fx-cursor: hand;"
        );
        return btn;
    }

    /** TextField estilo limpio */
    public static TextField crearTextField(String placeholder) {
        TextField tf = new TextField();
        tf.setPromptText(placeholder);
        tf.setPrefHeight(36);
        tf.setStyle(
                "-fx-border-color: " + C_GRAY_LIGHT + ";" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-background-color: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 0 10;"
        );
        tf.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) tf.setStyle(tf.getStyle().replace(C_GRAY_LIGHT, C_MEDIUM));
            else        tf.setStyle(tf.getStyle().replace(C_MEDIUM, C_GRAY_LIGHT));
        });
        return tf;
    }

    /** PasswordField estilo limpio */
    public static PasswordField crearPasswordField(String placeholder) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(placeholder);
        pf.setPrefHeight(36);
        pf.setStyle(
                "-fx-border-color: " + C_GRAY_LIGHT + ";" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-background-color: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 0 10;"
        );
        return pf;
    }

    /** TextArea estilo limpio */
    public static TextArea crearTextArea(String placeholder) {
        TextArea ta = new TextArea();
        ta.setPromptText(placeholder);
        ta.setPrefHeight(100);
        ta.setWrapText(true);
        ta.setStyle(
                "-fx-border-color: " + C_GRAY_LIGHT + ";" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-background-color: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 8;"
        );
        return ta;
    }

    /** Label título de sección */
    public static Label labelTitulo(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(FONT_TITLE);
        lbl.setTextFill(Color.web(C_VERY_DARK));
        return lbl;
    }

    /** Label subtítulo */
    public static Label labelSubtitulo(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(FONT_SUBTITLE);
        lbl.setTextFill(Color.web(C_DARK));
        return lbl;
    }

    /** Label sección menor */
    public static Label labelSeccion(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(FONT_SECTION);
        lbl.setTextFill(Color.web(C_VERY_DARK));
        return lbl;
    }

    /** Label texto normal */
    public static Label labelBody(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(FONT_BODY);
        lbl.setTextFill(Color.web(C_TEXT_MAIN));
        return lbl;
    }

    /** Label pequeño gris */
    public static Label labelSmall(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(FONT_SMALL);
        lbl.setTextFill(Color.web(C_GRAY_DARK));
        return lbl;
    }

    /** Badge / chip para estados */
    public static Label badge(String texto, String bgColor, String txtColor) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
        lbl.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: " + txtColor + ";" +
                        "-fx-padding: 2 8;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );
        return lbl;
    }

    /** Badge según EstadoProyecto */
    public static Label badgeEstadoProyecto(String estado) {
        switch (estado.toUpperCase()) {
            case "EN_REVISION":  return badge("En Revisión", C_ORANGE_LIGHT, C_ORANGE);
            case "APROBADO":     return badge("Aprobado",   C_VERY_LIGHT,   C_VERY_DARK);
            case "NO_APROBADO":  return badge("No Aprobado", C_RED_LIGHT,   C_RED);
            case "FINALIZADO":   return badge("Finalizado",  C_GRAY_LIGHT,  C_GRAY_DARK);
            default:             return badge(estado,        C_GRAY_LIGHT,  C_GRAY_DARK);
        }
    }

    /** Badge según EstadoReporte */
    public static Label badgeEstadoReporte(String estado) {
        switch (estado.toUpperCase()) {
            case "EN_EDICION": return badge("En Edición", C_ORANGE_LIGHT, C_ORANGE);
            case "CERRADO":    return badge("Enviado",    C_VERY_LIGHT,   C_VERY_DARK);
            case "APROBADO":   return badge("Aprobado",   C_VERY_LIGHT,   C_VERY_DARK);
            default:           return badge(estado,       C_GRAY_LIGHT,   C_GRAY_DARK);
        }
    }

    /** Badge según EstadoInforme */
    public static Label badgeEstadoInforme(String estado) {
        switch (estado.toUpperCase()) {
            case "EN_EDICION": return badge("En Edición",  C_ORANGE_LIGHT, C_ORANGE);
            case "ENVIADO":    return badge("Enviado",     C_VERY_LIGHT,   C_DARK);
            case "APROBADO":   return badge("Aprobado",    C_VERY_LIGHT,   C_VERY_DARK);
            case "RECHAZADO":  return badge("Rechazado",   C_RED_LIGHT,    C_RED);
            default:           return badge(estado,        C_GRAY_LIGHT,   C_GRAY_DARK);
        }
    }

    /** Badge según EstadoParticipacion */
    public static Label badgeEstadoParticipacion(String estado) {
        switch (estado.toUpperCase()) {
            case "ACTIVO":     return badge("Activo",     C_VERY_LIGHT,  C_VERY_DARK);
            case "RETIRADO":   return badge("Retirado",   C_RED_LIGHT,   C_RED);
            case "FINALIZADO": return badge("Finalizado", C_GRAY_LIGHT,  C_GRAY_DARK);
            default:           return badge(estado,       C_GRAY_LIGHT,  C_GRAY_DARK);
        }
    }

    /** Tarjeta contenedora (pane con sombra suave) */
    public static VBox tarjeta() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + C_GRAY_LIGHT + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-drop-shadow-blur-radius: 4;" +
                        "-fx-drop-shadow-color: rgba(0,0,0,0.06);" +
                        "-fx-drop-shadow-offset-x: 0;" +
                        "-fx-drop-shadow-offset-y: 2;"
        );
        return card;
    }

    /** Tarjeta estadística pequeña (dashboard) */
    public static VBox tarjetaStat(String titulo, String valor, String subtexto) {
        VBox card = tarjeta();
        card.setPadding(new Insets(16));
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setStyle(card.getStyle() + "-fx-min-width: 150; -fx-pref-width: 180;");

        Label lTitulo = labelSmall(titulo.toUpperCase());
        Label lValor  = new Label(valor);
        lValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lValor.setTextFill(Color.web(C_VERY_DARK));
        Label lSub    = labelSmall(subtexto);

        card.getChildren().addAll(lTitulo, lValor, lSub);
        return card;
    }

    /** Separador horizontal fino */
    public static Separator separador() {
        Separator sep = new Separator();
        sep.setStyle("-fx-border-color: " + C_GRAY_LIGHT + ";");
        return sep;
    }

    /** Contenedor principal (fondo off-white, padding) */
    public static ScrollPane contenedorPrincipal(VBox contenido) {
        contenido.setPadding(new Insets(24));
        contenido.setSpacing(18);
        contenido.setStyle("-fx-background-color: " + C_OFF_WHITE + ";");

        ScrollPane sp = new ScrollPane(contenido);
        sp.setFitToWidth(true);
        sp.setStyle(
                "-fx-background-color: " + C_OFF_WHITE + ";" +
                        "-fx-background: " + C_OFF_WHITE + ";"
        );
        return sp;
    }

    /** Crea un GridPane con estilo para formularios */
    public static GridPane crearGridFormulario() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(10, 0, 10, 0));
        return grid;
    }

    /** Alerta informativa */
    public static Alert alertaInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.getDialogPane().setStyle("-fx-background-color: white;");
        return alert;
    }

    /** Alerta de error */
    public static Alert alertaError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        return alert;
    }

    /** Alerta de confirmación */
    public static Alert alertaConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        return alert;
    }

    /** Diálogo de entrada de texto */
    public static TextInputDialog dialogoEntrada(String titulo, String mensaje) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(titulo);
        dialog.setHeaderText(mensaje);
        dialog.setContentText("Texto:");
        return dialog;
    }
}