package interfaz.comun;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import Logica.Entidades.Notificacion;

import java.util.List;

/**
 * Barra superior compartida.
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  [Logo]  [Tab1] [Tab2] [Tab3] ...          [Notif badge] [User info]│
 * └──────────────────────────────────────────────────────────────────────┘
 *
 * Uso:
 *   BarraNavegacion barra = new BarraNavegacion("Nombres", "Apellidos", "JEFATURA");
 *   barra.agregarTab("Dashboard",   () -> mostrarDashboard());
 *   barra.agregarTab("Proyectos",   () -> mostrarProyectos());
 *   ...
 *   barra.setNotificaciones(listNotificaciones);
 */
public class BarraNavegacion extends HBox {

    private final HBox tabsContainer;
    private final Label lblNotifCount;
    private final VBox panelNotificaciones;
    private final ScrollPane scrollNotif;
    private int tabIndex = 0;
    private Button tabActual = null;

    // Callback para cada tab
    private final java.util.List<Runnable> tabCallbacks = new java.util.ArrayList<>();

    public BarraNavegacion(String nombres, String apellidos, String rol) {
        super();
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(56);
        setStyle(
                "-fx-background-color: " + EstiloUI.C_DARK + ";" +
                        "-fx-padding: 0 24;"
        );

        // ── Logo ─────────────────────────────────────────────
        Label logo = new Label("SG");
        logo.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-width: 36; -fx-height: 36;" +
                        "-fx-background-color: " + EstiloUI.C_VERY_DARK + ";" +
                        "-fx-border-radius: 50;" +
                        "-fx-background-radius: 50;" +
                        "-fx-alignment: center;"
        );
        HBox.setMargin(logo, new Insets(0, 20, 0, 0));

        // ── Tabs container ───────────────────────────────────
        tabsContainer = new HBox(4);
        tabsContainer.setAlignment(Pos.CENTER);
        HBox.setHgrow(tabsContainer, Priority.ALWAYS);

        // ── Zona derecha: notificaciones + usuario ───────────
        // Badge notificaciones
        lblNotifCount = new Label("0");
        lblNotifCount.setStyle(
                "-fx-background-color: " + EstiloUI.C_RED + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-width: 18; -fx-height: 18;" +
                        "-fx-border-radius: 50;" +
                        "-fx-background-radius: 50;" +
                        "-fx-alignment: center;"
        );
        lblNotifCount.setVisible(false);

        Button btnNotif = new Button("\uD83D\uDD54"); // campana unicode
        btnNotif.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-cursor: hand;"
        );

        StackPane notifStack = new StackPane();
        notifStack.setAlignment(Pos.TOP_RIGHT);
        notifStack.getChildren().addAll(btnNotif, lblNotifCount);
        StackPane.setAlignment(lblNotifCount, Pos.TOP_RIGHT);

        // Panel desplegable de notificaciones
        panelNotificaciones = new VBox(0);
        panelNotificaciones.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + ";" +
                        "-fx-border-radius: 0 0 8 8;" +
                        "-fx-background-radius: 0 0 8 8;" +
                        "-fx-drop-shadow-blur-radius: 6;" +
                        "-fx-drop-shadow-color: rgba(0,0,0,0.10);" +
                        "-fx-drop-shadow-offset-x: 0;" +
                        "-fx-drop-shadow-offset-y: 3;"
        );
        panelNotificaciones.setMinWidth(340);
        panelNotificaciones.setMaxWidth(340);
        panelNotificaciones.setVisible(false);

        scrollNotif = new ScrollPane(new VBox());
        scrollNotif.setFitToWidth(true);
        scrollNotif.setPrefHeight(280);
        scrollNotif.setMaxHeight(340);
        scrollNotif.setStyle("-fx-background-color: white; -fx-background: white;");

        Label notifTitulo = new Label("  Notificaciones");
        notifTitulo.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-text-fill: " + EstiloUI.C_VERY_DARK + ";" +
                        "-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";" +
                        "-fx-padding: 10 12;" +
                        "-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + ";" +
                        "-fx-border-width: 0 0 1 0;"
        );
        panelNotificaciones.getChildren().addAll(notifTitulo, scrollNotif);

        btnNotif.setOnAction(e -> panelNotificaciones.setVisible(!panelNotificaciones.isVisible()));

        // ── Info usuario ──────────────────────────────────────
        String rolTexto = formatearRol(rol);
        Label lblNombre = new Label(nombres + " " + apellidos);
        lblNombre.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600;");
        Label lblRol = new Label(rolTexto);
        lblRol.setStyle("-fx-text-fill: " + EstiloUI.C_VERY_LIGHT + "; -fx-font-size: 11px;");

        VBox usuarioInfo = new VBox(1);
        usuarioInfo.setAlignment(Pos.CENTER_RIGHT);
        usuarioInfo.getChildren().addAll(lblNombre, lblRol);

        // Botón cerrar sesión
        Button btnCerrar = new Button("Cerrar Sesión");
        btnCerrar.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: rgba(255,255,255,0.4);" +
                        "-fx-text-fill: white;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 4 10;" +
                        "-fx-cursor: hand;"
        );
        btnCerrar.setOnAction(e -> Navigador.cambiarPantalla(new PantallaLogin()));

        HBox zonaDerecha = new HBox(16);
        zonaDerecha.setAlignment(Pos.CENTER);
        zonaDerecha.getChildren().addAll(notifStack, usuarioInfo, btnCerrar);

        getChildren().addAll(logo, tabsContainer, zonaDerecha);

        // Panel notificaciones se posiciona como overlay → se agrega al padre del layout
        // Esto se resuelve al añadir la barra al layout principal via setNotifPanel
    }

    // ─── MÉTODOS PÚBLICOS ─────────────────────────────────────────────────

    /** Agrega un tab a la barra de navegación */
    public void agregarTab(String texto, Runnable callback) {
        Button tab = new Button(texto);
        int indice = tabCallbacks.size();
        tabCallbacks.add(callback);

        tab.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-text-fill: rgba(255,255,255,0.75);" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-padding: 8 16;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );

        tab.setOnMouseEntered(e -> {
            if (tab != tabActual) {
                tab.setStyle(tab.getStyle()
                        .replace("transparent;-fx-border-color: transparent", "rgba(255,255,255,0.12);-fx-border-color: transparent")
                        .replace("rgba(255,255,255,0.75)", "white"));
            }
        });
        tab.setOnMouseExited(e -> {
            if (tab != tabActual) {
                tab.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-color: transparent;" +
                                "-fx-text-fill: rgba(255,255,255,0.75);" +
                                "-fx-font-size: 13px;" +
                                "-fx-font-weight: 600;" +
                                "-fx-padding: 8 16;" +
                                "-fx-border-radius: 6;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;"
                );
            }
        });

        tab.setOnAction(e -> {
            activarTab(tab);
            callback.run();
        });

        tabsContainer.getChildren().add(tab);

        // El primer tab se activa automáticamente
        if (indice == 0) {
            activarTab(tab);
        }
    }

    /** Activa visualmente un tab */
    private void activarTab(Button tab) {
        if (tabActual != null) {
            tabActual.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-border-color: transparent;" +
                            "-fx-text-fill: rgba(255,255,255,0.75);" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: 600;" +
                            "-fx-padding: 8 16;" +
                            "-fx-border-radius: 6;" +
                            "-fx-background-radius: 6;" +
                            "-fx-cursor: hand;"
            );
        }
        tabActual = tab;
        tab.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18);" +
                        "-fx-border-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 16;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );
    }

    /** Actualiza el listado de notificaciones mostradas */
    public void setNotificaciones(List<Notificacion> notificaciones) {
        VBox lista = new VBox(0);
        lista.setStyle("-fx-background-color: white;");

        if (notificaciones == null || notificaciones.isEmpty()) {
            Label empty = new Label("No hay notificaciones.");
            empty.setStyle("-fx-font-size: 12px; -fx-text-fill: " + EstiloUI.C_GRAY_MID + "; -fx-padding: 14 12;");
            lista.getChildren().add(empty);
        } else {
            for (int i = 0; i < notificaciones.size(); i++) {
                Notificacion n = notificaciones.get(i);
                lista.getChildren().add(crearItemNotificacion(n, i == 0));
            }
        }

        scrollNotif.setContent(lista);
        lblNotifCount.setText(String.valueOf(notificaciones != null ? notificaciones.size() : 0));
        lblNotifCount.setVisible(notificaciones != null && !notificaciones.isEmpty());
    }

    /** Retorna el panel de notificaciones para overlay */
    public VBox getPanelNotificaciones() {
        return panelNotificaciones;
    }

    // ─── PRIVADOS ─────────────────────────────────────────────────────────

    private VBox crearItemNotificacion(Notificacion n, boolean primera) {
        VBox item = new VBox(4);
        item.setPadding(new Insets(10, 12, 10, 12));
        String borderBottom = primera ? "" : "-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 1 0 0 0;";
        item.setStyle(
                "-fx-background-color: white;" +
                        borderBottom
        );
        item.setOnMouseEntered(e -> item.setStyle(item.getStyle() + "-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";"));
        item.setOnMouseExited(e  -> item.setStyle(item.getStyle().replaceAll("-fx-background-color:[^;]+;$", "-fx-background-color: white;")));

        Label contenido = new Label(n.getContenido());
        contenido.setStyle("-fx-font-size: 12px; -fx-text-fill: " + EstiloUI.C_TEXT_MAIN + ";");
        contenido.setWrapText(true);
        contenido.setMaxWidth(316);

        String fechaStr = n.getFecha() != null ? n.getFecha().toLocalDate().toString() : "";
        Label fecha = new Label(fechaStr);
        fecha.setStyle("-fx-font-size: 10px; -fx-text-fill: " + EstiloUI.C_GRAY_MID + ";");

        item.getChildren().addAll(contenido, fecha);
        return item;
    }

    private String formatearRol(String rol) {
        if (rol == null) return "";
        switch (rol.toUpperCase()) {
            case "JEFATURA": return "Jefatura";
            case "DIRECTOR": return "Director";
            case "PERSONAL": return "Personal de Investigación";
            default:         return rol;
        }
    }
}