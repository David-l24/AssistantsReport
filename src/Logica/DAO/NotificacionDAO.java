package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.Director;
import Logica.Entidades.Notificacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificacionDAO {
    private Connection connection;
    private DirectorDAO directorDAO;

    public NotificacionDAO() throws SQLException {
        this.connection = ConexionBD.conectar();
        this.directorDAO = new DirectorDAO(); // Necesario para llenar el destinatario
    }

    /**
     * Guarda una nueva notificación en la base de datos.
     * La fecha se toma del objeto Notificacion.
     */
    public boolean guardar(Notificacion notificacion) throws SQLException {
        String sql = "INSERT INTO Notificacion (cedula_director, fecha_envio, contenido) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // 1. Cédula del director (FK)
            stmt.setString(1, notificacion.getDestinatario().getCedula());

            // 2. Fecha (Conversión de LocalDateTime a Timestamp)
            stmt.setTimestamp(2, Timestamp.valueOf(notificacion.getFecha()));

            // 3. Contenido
            stmt.setString(3, notificacion.getContenido());

            int affectedRows = stmt.executeUpdate();

            // Si se insertó correctamente, recuperamos el ID generado
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        notificacion.setIdNotificacion(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Obtiene todas las notificaciones de un Director específico.
     */
    public List<Notificacion> obtenerPorDirector(String cedulaDirector) throws SQLException {
        List<Notificacion> notificaciones = new ArrayList<>();
        String sql = "SELECT * FROM Notificacion WHERE cedula_director = ? ORDER BY fecha_envio DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cedulaDirector);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notificaciones.add(mapResultSet(rs));
            }
        }
        return notificaciones;
    }

    /**
     * Obtiene una notificación específica por su ID.
     */
    public Notificacion obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Notificacion WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null; // Si no existe
    }

    /**
     * Mapea una fila del ResultSet a un objeto Notificacion.
     */
    private Notificacion mapResultSet(ResultSet rs) throws SQLException {
        Notificacion notificacion = new Notificacion();

        // Mapeo directo de columnas primitivas
        notificacion.setIdNotificacion(rs.getInt("id"));
        notificacion.setContenido(rs.getString("contenido"));

        // Mapeo de Fecha: Timestamp -> LocalDateTime
        Timestamp timestamp = rs.getTimestamp("fecha_envio");
        if (timestamp != null) {
            notificacion.setFecha(timestamp.toLocalDateTime());
        }

        // Mapeo del Objeto Director
        String cedulaDir = rs.getString("cedula_director");
        if (cedulaDir != null) {
            // Usamos el DAO auxiliar para obtener el objeto completo
            Director director = directorDAO.obtenerPorCedula(cedulaDir);
            notificacion.setDestinatario(director);
        }

        return notificacion;
    }
}