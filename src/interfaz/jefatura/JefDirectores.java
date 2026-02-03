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
 * Módulo de Directores para Jefatura.
 * Muestra listado de directores registrados con sus proyectos asociados.
 * Incluye buscador por nombre.
 */
public class JefDirectores extends VBox {

    private final TextField txtBuscar;
    private VBox listaContenido;

    public JefDirectores() {
        super(18);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";");

        // ── Header ──────────────────────────────────────────
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(EstiloUI.labelTitulo("Directores"));

        txtBuscar = EstiloUI.crearTextField("Buscar por nombre...");
        txtBuscar.setPrefWidth(280);
        txtBuscar.setOnKeyReleased(e -> cargarDirectores(txtBuscar.getText().trim()));

        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);
        header.getChildren().add(txtBuscar);
        getChildren().add(header);

        // ── Contenido ───────────────────────────────────────
        listaContenido = new VBox(12);
        getChildren().add(listaContenido);

        cargarDirectores("");
    }

    private void cargarDirectores(String filtro) {
        listaContenido.getChildren().clear();
        try {
            DirectorDAO dDAO  = new DirectorDAO();
            ProyectoDAO pDAO  = new ProyectoDAO();
            List<Director> directores = dDAO.obtenerTodos();

            for (Director d : directores) {
                // Filtro
                if (!filtro.isEmpty()) {
                    String buscar = filtro.toLowerCase();
                    if (!d.getNombres().toLowerCase().contains(buscar) &&
                        !d.getApellidos().toLowerCase().contains(buscar)) {
                        continue;
                    }
                }
                List<Proyecto> proyectosDir = pDAO.obtenerPorDirector(d.getCedula());
                listaContenido.getChildren().add(crearTarjetaDirector(d, proyectosDir));
            }

            if (listaContenido.getChildren().isEmpty()) {
                listaContenido.getChildren().add(EstiloUI.labelSmall("No se encontraron directores."));
            }

        } catch (SQLException e) {
            listaContenido.getChildren().add(EstiloUI.labelSmall("Error: " + e.getMessage()));
        }
    }

    private VBox crearTarjetaDirector(Director d, List<Proyecto> proyectos) {
        VBox tarjeta = EstiloUI.tarjeta();

        // ── Info director ──────────────────────────────────
        HBox infoRow = new HBox(20);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        // Avatar inicial
        Label avatar = new Label(String.valueOf(d.getNombres().charAt(0)));
        avatar.setStyle(
                "-fx-background-color: " + EstiloUI.C_MEDIUM + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-width: 42; -fx-height: 42;" +
                "-fx-border-radius: 50;" +
                "-fx-background-radius: 50;" +
                "-fx-alignment: center;"
        );

        VBox info = new VBox(3);
        info.getChildren().add(EstiloUI.labelSubtitulo(d.getNombresCompletos()));
        info.getChildren().add(EstiloUI.labelSmall("Cédula: " + d.getCedula() +
                (d.getCorreo() != null ? " | Correo: " + d.getCorreo() : "")));

        infoRow.getChildren().addAll(avatar, info);
        tarjeta.getChildren().add(infoRow);
        tarjeta.getChildren().add(EstiloUI.separador());

        // ── Proyectos asociados ────────────────────────────
        Label lblProy = EstiloUI.labelSeccion("Proyectos asociados");
        tarjeta.getChildren().add(lblProy);

        if (proyectos.isEmpty()) {
            tarjeta.getChildren().add(EstiloUI.labelSmall("  Sin proyectos asignados."));
        } else {
            for (Proyecto p : proyectos) {
                HBox fila = new HBox(16);
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setPadding(new Insets(6, 8, 6, 8));
                fila.setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";" +
                        "-fx-border-radius: 4; -fx-background-radius: 4;");

                Label nombre = EstiloUI.labelBody(p.getNombre());
                nombre.setMinWidth(200);
                Label codigo = EstiloUI.labelSmall(p.getCodigoProyecto() != null ? p.getCodigoProyecto() : "—");
                codigo.setMinWidth(80);
                Label estado = EstiloUI.badgeEstadoProyecto(p.getEstado().name());

                fila.getChildren().addAll(nombre, codigo, estado);
                tarjeta.getChildren().add(fila);
            }
        }

        return tarjeta;
    }
}
