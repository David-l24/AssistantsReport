package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.Reporte;
import Logica.Enumeraciones.EstadoReporte;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {
    private Connection connection;

    public ReporteDAO() throws SQLException {
        this.connection = ConexionBD.conectar();
    }

    public List<Reporte> obtenerTodos() throws SQLException {
        List<Reporte> reportes = new ArrayList<>();
        // Ordenamos por fecha_inicio descendente
        String sql = "SELECT * FROM reporte ORDER BY fecha_inicio DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reportes.add(mapResultSet(rs));
            }
        }
        return reportes;
    }

    // OJO: idProyecto ahora es int
    public List<Reporte> obtenerPorProyecto(int idProyecto) throws SQLException {
        List<Reporte> reportes = new ArrayList<>();
        String sql = "SELECT * FROM reporte WHERE id_proyecto = ? ORDER BY fecha_inicio DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idProyecto);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reportes.add(mapResultSet(rs));
            }
        }
        return reportes;
    }

    public Reporte obtenerPorId(int idReporte) throws SQLException {
        String sql = "SELECT * FROM reporte WHERE id_reporte = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReporte);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null;
    }

    public int guardar(Reporte reporte) throws SQLException {
        // Insertamos en 'reporte'. No incluimos id_reporte (es serial).
        // Usamos setString para el estado porque la columna es varchar(50).
        String sql = "INSERT INTO reporte (periodo_academico, id_proyecto, estado, fecha_inicio, fecha_cierre) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id_reporte";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reporte.getPeriodoAcademico());
            stmt.setInt(2, reporte.getIdProyecto());
            stmt.setString(3, reporte.getEstado().name());

            // Conversión LocalDate -> java.sql.Date
            if (reporte.getFechaInicio() != null) {
                stmt.setDate(4, Date.valueOf(reporte.getFechaInicio()));
            } else {
                stmt.setDate(4, Date.valueOf(java.time.LocalDate.now()));
            }

            if (reporte.getFechaCierre() != null) {
                stmt.setDate(5, Date.valueOf(reporte.getFechaCierre()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public boolean actualizar(Reporte reporte) throws SQLException {
        String sql = "UPDATE reporte SET estado = ?, fecha_cierre = ?, periodo_academico = ? WHERE id_reporte = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reporte.getEstado().name());

            if (reporte.getFechaCierre() != null) {
                stmt.setDate(2, Date.valueOf(reporte.getFechaCierre()));
            } else {
                stmt.setNull(2, Types.DATE);
            }

            stmt.setString(3, reporte.getPeriodoAcademico());
            stmt.setInt(4, reporte.getIdReporte());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int idReporte) throws SQLException {
        String sql = "DELETE FROM reporte WHERE id_reporte = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReporte);
            return stmt.executeUpdate() > 0;
        }
    }

    private Reporte mapResultSet(ResultSet rs) throws SQLException {
        Reporte reporte = new Reporte();

        // Mapeo exacto con los nombres de la imagen BD
        reporte.setIdReporte(rs.getInt("id_reporte"));
        reporte.setPeriodoAcademico(rs.getString("periodo_academico"));
        reporte.setIdProyecto(rs.getInt("id_proyecto"));

        // Convertimos String de BD a Enum
        String estadoStr = rs.getString("estado");
        if (estadoStr != null) {
            reporte.setEstado(EstadoReporte.fromString(estadoStr));
        }

        // Convertimos java.sql.Date a LocalDate
        Date fechaInicio = rs.getDate("fecha_inicio");
        if (fechaInicio != null) {
            reporte.setFechaInicio(fechaInicio.toLocalDate());
        }

        Date fechaCierre = rs.getDate("fecha_cierre");
        if (fechaCierre != null) {
            reporte.setFechaCierre(fechaCierre.toLocalDate());
        }

        return reporte;
    }
}