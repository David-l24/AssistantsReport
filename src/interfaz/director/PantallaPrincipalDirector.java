package interfaz.director;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import Logica.Entidades.*;
import interfaz.comun.BarraNavegacion;

import java.sql.SQLException;

/**
 * Layout raíz para el rol DIRECTOR.
 * Tabs: Dashboard | Personal | Reportes | Informes
 */
public class PantallaPrincipalDirector extends VBox {

    private final Director director;
    private final Proyecto proyecto;
    private final BarraNavegacion barra;
    private final StackPane contenedorPrincipal;

    public PantallaPrincipalDirector(Director director, Proyecto proyecto) {
        super();
        this.director = director;
        this.proyecto = proyecto;
        setPrefWidth(1280);
        setPrefHeight(750);
        setStyle("-fx-background-color: " + interfaz.comun.EstiloUI.C_OFF_WHITE + ";");

        barra = new BarraNavegacion(
                director.getNombres(),
                director.getApellidos(),
                "DIRECTOR"
        );

        contenedorPrincipal = new StackPane();
        VBox.setVgrow(contenedorPrincipal, Priority.ALWAYS);

        // Panel base
        VBox panelBase = new VBox();
        VBox.setVgrow(panelBase, Priority.ALWAYS);
        contenedorPrincipal.getChildren().add(panelBase);

        // Overlay notificaciones
        VBox overlayNotif = barra.getPanelNotificaciones();
        StackPane.setAlignment(overlayNotif, javafx.geometry.Pos.TOP_RIGHT);
        contenedorPrincipal.getChildren().add(overlayNotif);

        // Tabs
        barra.agregarTab("Dashboard", () -> mostrar(new DirDashboard(director, proyecto)));
        barra.agregarTab("Personal",  () -> mostrar(new DirPersonal(director, proyecto)));
        barra.agregarTab("Reportes",  () -> mostrar(new DirReportes(director, proyecto)));
        barra.agregarTab("Informes",  () -> mostrar(new DirInformes(director, proyecto)));

        getChildren().addAll(barra, contenedorPrincipal);

        // Cargar notificaciones
        cargarNotificaciones();

        // Mostrar dashboard
        mostrar(new DirDashboard(director, proyecto));
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
            director.cargarNotificaciones();
            barra.setNotificaciones(director.getNotificaciones());
        } catch (SQLException e) {
            System.err.println("Error cargando notificaciones: " + e.getMessage());
        }
    }
}
