package Logica.Conexiones;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static Connection connection;
    private static final String URL = "jdbc:postgresql://26.224.201.172/AssistantsReportDB";
    private static final String USUARIO = "postgres";
    private static final String CONTRASEÑA = "admin";

    public static Connection conectar() throws SQLException {
        if(connection == null){
            connection = DriverManager.getConnection(URL, USUARIO, CONTRASEÑA);
        }
        return connection;
    }
}