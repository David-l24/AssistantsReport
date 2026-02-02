package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.*;
import Logica.Enumeraciones.EstadoReporte;
import Logica.Enumeraciones.EstadoParticipacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {
    private Connection connection;

    public ReporteDAO() throws SQLException {
        this.connection = ConexionBD.conectar();
    }

    // ==========================================
    // SECCIÓN 1: CRUD BÁSICO DE LA TABLA REPORTE
    // ==========================================

    public List<Reporte> obtenerTodos() throws SQLException {
        List<Reporte> reportes = new ArrayList<>();
        String sql = "SELECT * FROM reporte ORDER BY fecha_inicio DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reportes.add(mapResultSetReporte(rs));
            }
        }
        return reportes;
    }

    public List<Reporte> obtenerPorProyecto(int idProyecto) throws SQLException {
        List<Reporte> reportes = new ArrayList<>();
        String sql = "SELECT * FROM reporte WHERE id_proyecto = ? ORDER BY fecha_inicio DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idProyecto);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reportes.add(mapResultSetReporte(rs));
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
                return mapResultSetReporte(rs);
            }
        }
        return null;
    }

    public int guardar(Reporte reporte) throws SQLException {
        String sql = "INSERT INTO reporte (periodo_academico, id_proyecto, estado, fecha_inicio, fecha_cierre) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id_reporte";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reporte.getPeriodoAcademico());
            stmt.setInt(2, reporte.getIdProyecto());
            stmt.setString(3, reporte.getEstado().name()); // Enum a String

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
                int id = rs.getInt(1);
                reporte.setIdReporte(id);
                return id;
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
        // Primero vaciamos la tabla intermedia para evitar error de FK
        vaciarParticipacionesDelReporte(idReporte);

        String sql = "DELETE FROM reporte WHERE id_reporte = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReporte);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================================================
    // SECCIÓN 2: GESTIÓN DE LA TABLA INTERMEDIA (REPORTE_PARTICIPACION)
    // ============================================================

    /**
     * Vincula una participación existente a este reporte.
     */
    public boolean agregarParticipacion(int idReporte, int idParticipacion) throws SQLException {
        String sql = "INSERT INTO reporte_participacion (id_reporte, id_participacion) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReporte);
            stmt.setInt(2, idParticipacion);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Elimina una participación específica de este reporte.
     */
    public boolean quitarParticipacion(int idReporte, int idParticipacion) throws SQLException {
        String sql = "DELETE FROM reporte_participacion WHERE id_reporte = ? AND id_participacion = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReporte);
            stmt.setInt(2, idParticipacion);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Elimina todas las participaciones de un reporte (limpieza).
     */
    private boolean vaciarParticipacionesDelReporte(int idReporte) throws SQLException {
        String sql = "DELETE FROM reporte_participacion WHERE id_reporte = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReporte);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Carga la lista completa de objetos Participacion (con sus datos de Personal)
     * asociados a un reporte y la asigna al objeto Reporte.
     */
    public void cargarParticipacionesDelReporte(Reporte reporte) throws SQLException {
        if (reporte == null) return;

        List<Participacion> lista = new ArrayList<>();

        // JOIN TRIPLE: reporte_participacion -> participacion -> integrante
        String sql = "SELECT p.*, i.cedula, i.nombres, i.apellidos, i.correo, i.tipo " +
                "FROM reporte_participacion rp " +
                "JOIN participacion p ON rp.id_participacion = p.id_participacion " +
                "JOIN integrante i ON p.cedula_personal = i.cedula " +
                "WHERE rp.id_reporte = ? " +
                "ORDER BY i.apellidos, i.nombres";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reporte.getIdReporte());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(mapResultSetParticipacionCompleta(rs));
            }
        }

        reporte.setParticipacionesIncluidas(lista);
    }


    // ==========================================
    // SECCIÓN 3: MAPEOS (MAPPERS)
    // ==========================================

    private Reporte mapResultSetReporte(ResultSet rs) throws SQLException {
        Reporte reporte = new Reporte();
        reporte.setIdReporte(rs.getInt("id_reporte"));
        reporte.setPeriodoAcademico(rs.getString("periodo_academico"));
        reporte.setIdProyecto(rs.getInt("id_proyecto"));

        String estadoStr = rs.getString("estado");
        if (estadoStr != null) {
            reporte.setEstado(EstadoReporte.fromString(estadoStr));
        }

        Date fechaInicio = rs.getDate("fecha_inicio");
        if (fechaInicio != null) reporte.setFechaInicio(fechaInicio.toLocalDate());

        Date fechaCierre = rs.getDate("fecha_cierre");
        if (fechaCierre != null) reporte.setFechaCierre(fechaCierre.toLocalDate());

        return reporte;
    }

    // Mapeo auxiliar para reconstruir la Participación completa con su Personal
    private Participacion mapResultSetParticipacionCompleta(ResultSet rs) throws SQLException {
        Participacion p = new Participacion();
        p.setIdParticipacion(rs.getInt("id_participacion"));

        // 1. Reconstruir Personal (Factory)
        String tipo = rs.getString("tipo");
        if (tipo == null) tipo = "";

        PersonalDeInvestigacion personal;
        if ("AYUDANTE".equalsIgnoreCase(tipo)) {
            personal = new Ayudante();
        } else if ("ASISTENTE".equalsIgnoreCase(tipo)) {
            personal = new Asistente();
        } else if ("TECNICO".equalsIgnoreCase(tipo)) {
            personal = new Tecnico();
        } else {
            personal = new Ayudante(); // Default
        }

        personal.setCedula(rs.getString("cedula"));
        personal.setNombres(rs.getString("nombres"));
        personal.setApellidos(rs.getString("apellidos"));
        personal.setCorreo(rs.getString("correo"));

        p.setPersonal(personal);

        // 2. Datos de Participación
        Date fi = rs.getDate("fecha_inicio");
        if (fi != null) p.setFechaInicio(fi.toLocalDate());

        Date ff = rs.getDate("fecha_fin");
        if (ff != null) p.setFechaFin(ff.toLocalDate());

        Date fr = rs.getDate("fecha_retiro");
        if (fr != null) p.setFechaRetiro(fr.toLocalDate());

        p.setMotivoRetiro(rs.getString("motivo_retiro"));

        String est = rs.getString("estado");
        if (est != null) p.setEstado(EstadoParticipacion.valueOf(est));

        return p;
    }
}