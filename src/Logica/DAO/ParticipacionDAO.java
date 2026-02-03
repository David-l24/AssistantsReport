package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.Participacion;
import Logica.Enumeraciones.EstadoParticipacion;
import Logica.Entidades.PersonalDeInvestigacion;
import Logica.Entidades.Ayudante;
import Logica.Entidades.Tecnico;
import Logica.Entidades.Asistente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipacionDAO {
    private Connection connection;

    public ParticipacionDAO() throws SQLException {
        this.connection = ConexionBD.conectar();
    }

    /**
     * Obtiene todas las participaciones
     */
    public List<Participacion> obtenerTodas() throws SQLException {
        List<Participacion> lista = new ArrayList<>();

        // CORRECCIÓN: La tabla es personaldeinvestigacion, NO integrante
        String sql = "SELECT p.*, " +
                "pi.cedula, pi.nombres, pi.apellidos, pi.correo, pi.tipo " +
                "FROM participacion p " +
                "INNER JOIN personaldeinvestigacion pi ON p.cedula_personal = pi.cedula " +
                "ORDER BY p.fecha_inicio DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        }
        return lista;
    }

    /**
     * Obtiene todas las participaciones de un personal específico
     * MÉTODO AGREGADO - Faltaba en el código original
     * Se usa en ResumenSeguimientoDAO para determinar el estado de cada personal
     */
    public List<Participacion> obtenerPorPersonal(String cedula) throws SQLException {
        List<Participacion> lista = new ArrayList<>();

        String sql = "SELECT p.*, " +
                "pi.cedula, pi.nombres, pi.apellidos, pi.correo, pi.tipo " +
                "FROM participacion p " +
                "INNER JOIN personaldeinvestigacion pi ON p.cedula_personal = pi.cedula " +
                "WHERE p.cedula_personal = ? " +
                "ORDER BY p.fecha_inicio DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cedula);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        }
        return lista;
    }

    /**
     * Obtiene una participación por su ID
     * MÉTODO AGREGADO - Útil para consultas específicas
     */
    public Participacion obtenerPorId(int idParticipacion) throws SQLException {
        String sql = "SELECT p.*, " +
                "pi.cedula, pi.nombres, pi.apellidos, pi.correo, pi.tipo " +
                "FROM participacion p " +
                "INNER JOIN personaldeinvestigacion pi ON p.cedula_personal = pi.cedula " +
                "WHERE p.id_participacion = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idParticipacion);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null;
    }

    /**
     * Obtiene participaciones activas de un proyecto
     * MÉTODO AGREGADO - Útil para saber quién está trabajando actualmente
     */
    public List<Participacion> obtenerActivasPorProyecto(int idProyecto) throws SQLException {
        List<Participacion> lista = new ArrayList<>();

        String sql = "SELECT p.*, " +
                "pi.cedula, pi.nombres, pi.apellidos, pi.correo, pi.tipo " +
                "FROM participacion p " +
                "INNER JOIN personaldeinvestigacion pi ON p.cedula_personal = pi.cedula " +
                "WHERE pi.id_proyecto = ? AND p.estado = ? " +
                "ORDER BY p.fecha_inicio DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idProyecto);
            stmt.setString(2, EstadoParticipacion.ACTIVO.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        }
        return lista;
    }

    /**
     * Guarda una nueva participación
     */
    public int guardar(Participacion p) throws SQLException {
        String sql = "INSERT INTO participacion " +
                "(cedula_personal, fecha_inicio, fecha_fin, fecha_retiro, motivo_retiro, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_participacion";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, p.getCedulaPersonal());
            stmt.setDate(2, Date.valueOf(p.getFechaInicio()));

            // fecha_fin puede ser null para participaciones activas
            if (p.getFechaFin() != null) {
                stmt.setDate(3, Date.valueOf(p.getFechaFin()));
            } else {
                stmt.setNull(3, Types.DATE);
            }

            if (p.getFechaRetiro() != null) {
                stmt.setDate(4, Date.valueOf(p.getFechaRetiro()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, p.getMotivoRetiro());
            stmt.setString(6, p.getEstado().name());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                p.setIdParticipacion(id);
                return id;
            }
            return -1;
        }
    }

    /**
     * Actualiza una participación existente
     */
    public boolean actualizar(Participacion p) throws SQLException {
        String sql = "UPDATE participacion SET " +
                "fecha_inicio = ?, fecha_fin = ?, fecha_retiro = ?, " +
                "motivo_retiro = ?, estado = ? " +
                "WHERE id_participacion = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(p.getFechaInicio()));

            if (p.getFechaFin() != null) {
                stmt.setDate(2, Date.valueOf(p.getFechaFin()));
            } else {
                stmt.setNull(2, Types.DATE);
            }

            if (p.getFechaRetiro() != null) {
                stmt.setDate(3, Date.valueOf(p.getFechaRetiro()));
            } else {
                stmt.setNull(3, Types.DATE);
            }

            stmt.setString(4, p.getMotivoRetiro());
            stmt.setString(5, p.getEstado().name());
            stmt.setInt(6, p.getIdParticipacion());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Elimina una participación
     */
    public boolean eliminar(int idParticipacion) throws SQLException {
        String sql = "DELETE FROM participacion WHERE id_participacion = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idParticipacion);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Mapea un ResultSet a un objeto Participacion
     * Incluye la creación del objeto PersonalDeInvestigacion asociado
     */
    private Participacion mapResultSet(ResultSet rs) throws SQLException {
        Participacion participacion = new Participacion();
        participacion.setIdParticipacion(rs.getInt("id_participacion"));

        // Factory: Crear instancia correcta según el tipo
        String tipoPersonal = rs.getString("tipo");
        if (tipoPersonal == null) tipoPersonal = "";

        PersonalDeInvestigacion personal;

        // CORRECCIÓN: Usar nombres consistentes con getTipo()
        if ("Ayudante".equalsIgnoreCase(tipoPersonal)) {
            personal = new Ayudante();
        } else if ("Asistente".equalsIgnoreCase(tipoPersonal)) {
            personal = new Asistente();
        } else if ("Tecnico".equalsIgnoreCase(tipoPersonal)) {
            personal = new Tecnico();
        } else {
            // Default fallback
            personal = new Ayudante();
        }

        // Mapear datos del personal
        personal.setCedula(rs.getString("cedula"));
        personal.setNombres(rs.getString("nombres"));
        personal.setApellidos(rs.getString("apellidos"));
        personal.setCorreo(rs.getString("correo"));

        // Asignar el objeto personal completo a la participación
        participacion.setPersonal(personal);

        // Mapear fechas de participación
        Date fechaInicio = rs.getDate("fecha_inicio");
        if (fechaInicio != null) {
            participacion.setFechaInicio(fechaInicio.toLocalDate());
        }

        Date fechaFin = rs.getDate("fecha_fin");
        if (fechaFin != null) {
            participacion.setFechaFin(fechaFin.toLocalDate());
        }

        Date fechaRetiro = rs.getDate("fecha_retiro");
        if (fechaRetiro != null) {
            participacion.setFechaRetiro(fechaRetiro.toLocalDate());
        }

        participacion.setMotivoRetiro(rs.getString("motivo_retiro"));

        // Mapear estado
        String estadoStr = rs.getString("estado");
        if (estadoStr != null) {
            participacion.setEstado(EstadoParticipacion.valueOf(estadoStr));
        }

        return participacion;
    }
}
