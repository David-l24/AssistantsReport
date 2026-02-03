package interfaz.personal;

import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import Logica.Entidades.*;
import interfaz.comun.BarraNavegacion;

import java.sql.SQLException;

/**
 * Layout raíz para el rol PERSONAL DE INVESTIGACIÓN.
 * Tabs: Dashboard | Informes
 */
public class PantallaPrincipalPersonal extends VBox {

    private final PersonalDeInvestigacion personal;
    private final Proyecto                proyecto;
    private final BarraNavegacion         barra;
    private final StackPane               contenedorPrincipal;

    public PantallaPrincipalPersonal(PersonalDeInvestigacion personal, Proyecto proyecto) {
        super();
        this.personal = personal;
        this.proyecto = proyecto;
        setPrefWidth(1280);
        setPrefHeight(750);
        setStyle("-fx-background-color: " + interfaz.comun.EstiloUI.C_OFF_WHITE + ";");

        barra = new BarraNavegacion(
                personal.getNombres(),
                personal.getApellidos(),
                personal.getTipo().toUpperCase()
        );

        contenedorPrincipal = new StackPane();
        VBox.setVgrow(contenedorPrincipal, Priority.ALWAYS);

        // Panel base (contenido intercambiable)
        VBox panelBase = new VBox();
        VBox.setVgrow(panelBase, Priority.ALWAYS);
        contenedorPrincipal.getChildren().add(panelBase);

        // Overlay de notificaciones (se superpone arriba a la derecha)
        VBox overlayNotif = barra.getPanelNotificaciones();
        StackPane.setAlignment(overlayNotif, Pos.TOP_RIGHT);
        contenedorPrincipal.getChildren().add(overlayNotif);

        // Tabs
        barra.agregarTab("Dashboard", () -> mostrar(new PerDashboard(personal, proyecto)));
        barra.agregarTab("Informes",  () -> mostrar(new PerInformes(personal, proyecto)));

        getChildren().addAll(barra, contenedorPrincipal);

        // Cargar notificaciones
        cargarNotificaciones();

        // Mostrar dashboard por defecto
        mostrar(new PerDashboard(personal, proyecto));
    }

    private void mostrar(Pane modulo) {
        ScrollPane scrollPane = new ScrollPane(modulo);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        if (!contenedorPrincipal.getChildren().isEmpty()) {
            contenedorPrincipal.getChildren().set(0, scrollPane);
        }
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        barra.getPanelNotificaciones().setVisible(false);
    }

    private void cargarNotificaciones() {
        try {
            personal.cargarNotificaciones();
            barra.setNotificaciones(personal.getNotificaciones());
        } catch (SQLException e) {
            System.err.println("Error cargando notificaciones: " + e.getMessage());
        }
    }
}
