package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.Notificacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificacionDAO {
    private Connection connection;

    public NotificacionDAO() throws SQLException {
        this.connection = ConexionBD.conectar();
    }

    public boolean guardar(Notificacion notificacion) throws SQLException {
        String sql = "INSERT INTO public.notificacion (id_usuario, fecha_envio, contenido) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, notificacion.getIdUsuario());
            stmt.setTimestamp(2, Timestamp.valueOf(notificacion.getFecha()));
            stmt.setString(3, notificacion.getContenido());

            int affectedRows = stmt.executeUpdate();

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

    public List<Notificacion> obtenerPorUsuario(int idUsuario) throws SQLException {
        List<Notificacion> notificaciones = new ArrayList<>();
        String sql = "SELECT * FROM public.notificacion WHERE id_usuario = ? ORDER BY fecha_envio DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notificaciones.add(mapResultSet(rs));
            }
        }
        return notificaciones;
    }

    public Notificacion obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM public.notificacion WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null;
    }

    private Notificacion mapResultSet(ResultSet rs) throws SQLException {
        Notificacion notificacion = new Notificacion();
        notificacion.setIdNotificacion(rs.getInt("id"));
        notificacion.setIdUsuario(rs.getInt("id_usuario"));
        notificacion.setContenido(rs.getString("contenido"));

        Timestamp timestamp = rs.getTimestamp("fecha_envio");
        if (timestamp != null) {
            notificacion.setFecha(timestamp.toLocalDateTime());
        }

        return notificacion;
    }
}