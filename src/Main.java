import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import interfaz.comun.Navigador;
import interfaz.comun.PantallaLogin;

public class Main extends Application {

    private static Stage primaryStage;

    public static void main(String[] args) {
        launch(Main.class, args);
    }

    @Override
    public void init() {
        // Cargar el driver JDBC de PostgreSQL
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL no encontrado: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Main.primaryStage = primaryStage;
        Navigador.setStage(primaryStage);
        primaryStage.setTitle("Sistema de Gestión de Investigación");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(700);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(750);

        // Iniciar con la pantalla de login
        PantallaLogin login = new PantallaLogin();
        Scene scene = new Scene(login.getLayout(), 1280, 750);
        // Cargar stylesheet solo si existe el archivo en resources
        java.net.URL cssUrl = getClass().getResource("/estilo.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Cambia la pantalla principal a un nuevo layout
     */
    public static void cambiarPantalla(javafx.scene.layout.Pane nuevoPantalla) {
        Scene currentScene = primaryStage.getScene();
        if (currentScene != null) {
            currentScene.setRoot(nuevoPantalla);
        } else {
            primaryStage.setScene(new Scene(nuevoPantalla, 1280, 750));
        }
    }
}