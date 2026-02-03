package interfaz.comun;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Clase utilidad estática que centraliza la navegación entre pantallas.
 *
 * Main llama a {@code Navigador.setStage(primaryStage)} una sola vez al arrancar.
 * Desde cualquier otra clase se usa {@code Navigador.cambiarPantalla(pane)}.
 *
 * Esto evita que las clases de interfaz dependen directamente de Main
 * (que extiende Application y no debe instanciarse ni referenciarse desde el resto del código).
 */
public class Navigador {

    private static Stage stage;

    private Navigador() { /* utilidad estática, no se instancia */ }

    /**
     * Debe ser llamado UNA VEZ desde {@code Main.start()} para registrar el Stage.
     */
    public static void setStage(Stage primaryStage) {
        Navigador.stage = primaryStage;
    }

    /**
     * Reemplaza el contenido de la pantalla actual por {@code nuevaPantalla}.
     * Si la Scene ya existe solo cambia el root; si no, crea una nueva Scene.
     */
    public static void cambiarPantalla(Pane nuevaPantalla) {
        if (stage == null) {
            throw new IllegalStateException("Navigador: Stage no inicializado. " +
                    "Llame a Navigador.setStage() desde Main.start().");
        }
        Scene actual = stage.getScene();
        if (actual != null) {
            actual.setRoot(nuevaPantalla);
        } else {
            stage.setScene(new Scene(nuevaPantalla, 1280, 750));
        }
    }
}
