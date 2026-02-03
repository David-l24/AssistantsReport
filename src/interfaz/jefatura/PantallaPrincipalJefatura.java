package interfaz.jefatura;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.control.ScrollPane;

import Logica.Entidades.*;
import interfaz.comun.BarraNavegacion;

import java.sql.SQLException;
import java.util.List;

/**
 * Layout raíz para el rol JEFATURA.
 * Estructura vertical:
 *   ┌─────────────── BarraNavegacion ───────────────┐
 *   │  Dashboard | Proyectos | Directores | Personal │
 *   │  Informes  | Reportes  | Administración       │
 *   ├───────────────────────────────────────────────┤
 *   │           Contenido del módulo activo          │
 *   └───────────────────────────────────────────────┘
 *
 * El panel de notificaciones se monta como overlay sobre el contenido.
 */
public class PantallaPrincipalJefatura extends VBox {

    private final Jefatura jefatura;
    private final BarraNavegacion barra;
    private final StackPane contenedorPrincipal; // stack para overlay de notif

    public PantallaPrincipalJefatura(Jefatura jefatura) {
        super();
        this.jefatura = jefatura;
        setPrefWidth(1280);
        setPrefHeight(750);
        setStyle("-fx-background-color: " + interfaz.comun.EstiloUI.C_OFF_WHITE + ";");

        // ── Barra deNavigación ─────────────────────────────────
        barra = new BarraNavegacion(
                jefatura.getNombres(),
                jefatura.getApellidos(),
                "JEFATURA"
        );

        // ── Contenedor principal con overlay de notificaciones ──
        contenedorPrincipal = new StackPane();
        VBox.setVgrow(contenedorPrincipal, Priority.ALWAYS);

        // Panel base (intercambiable)
        VBox panelBase = new VBox();
        VBox.setVgrow(panelBase, Priority.ALWAYS);
        contenedorPrincipal.getChildren().add(panelBase);

        // Overlay de notificaciones (esquina superior derecha)
        VBox overlayNotif = barra.getPanelNotificaciones();
        StackPane.setAlignment(overlayNotif, javafx.geometry.Pos.TOP_RIGHT);
        contenedorPrincipal.getChildren().add(overlayNotif);

        // ── Registrar tabs DESPUÉS de montar contenedorPrincipal ─
        barra.agregarTab("Dashboard",        () -> mostrar(new JefDashboard(jefatura)));
        barra.agregarTab("Proyectos",        () -> mostrar(new JefProyectos(jefatura)));
        barra.agregarTab("Directores",       () -> mostrar(new JefDirectores()));
        barra.agregarTab("Personal",         () -> mostrar(new JefPersonal()));
        barra.agregarTab("Informes",         () -> mostrar(new JefInformes()));
        barra.agregarTab("Reportes",         () -> mostrar(new JefReportes()));
        barra.agregarTab("Administración",   () -> mostrar(new JefAdministracion()));

        getChildren().addAll(barra, contenedorPrincipal);

        // Cargar notificaciones al inicio
        cargarNotificaciones();

        // Mostrar dashboard por defecto
        mostrar(new JefDashboard(jefatura));
    }

    /**
     * Intercambia el contenido del panel central.
     */
    private void mostrar(Pane modulo) {
        // El primer hijo del stack es el contenedor base
        if (!contenedorPrincipal.getChildren().isEmpty()) {
            contenedorPrincipal.getChildren().set(0, modulo);
        }
        VBox.setVgrow(modulo, Priority.ALWAYS);

        // Cerrar panel notificaciones al cambiar de modulo
        barra.getPanelNotificaciones().setVisible(false);
    }

    private void cargarNotificaciones() {
        try {
            jefatura.cargarNotificaciones();
            barra.setNotificaciones(jefatura.getNotificaciones());
        } catch (SQLException e) {
            System.err.println("Error cargando notificaciones: " + e.getMessage());
        }
    }
}
