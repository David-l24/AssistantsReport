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

    public List<Participacion> obtenerTodas() throws SQLException {
        List<Participacion> lista = new ArrayList<>();

        // INNER JOIN para traer los datos del integrante
        // Seleccionamos p.* (todo de participación) e i.* (todo del integrante)
        String sql = "SELECT p.*, i.cedula, i.nombres, i.apellidos, i.correo, i.tipo " +
                "FROM participacion p " +
                "INNER JOIN integrante i ON p.cedula_personal = i.cedula " +
                "ORDER BY p.fecha_inicio DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        }
        return lista;
    }

    // Guardar sigue igual, solo necesitamos la cédula
    public boolean guardar(Participacion p) throws SQLException {
        String sql = "INSERT INTO participacion (cedula_personal, fecha_inicio, fecha_fin, fecha_retiro, motivo_retiro, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, p.getCedulaPersonal()); // Sacamos la cédula del objeto
            stmt.setDate(2, Date.valueOf(p.getFechaInicio()));
            stmt.setDate(3, Date.valueOf(p.getFechaFin()));

            if (p.getFechaRetiro() != null) {
                stmt.setDate(4, Date.valueOf(p.getFechaRetiro()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, p.getMotivoRetiro());
            stmt.setString(6, p.getEstado().name());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Participacion p) throws SQLException {
        String sql = "UPDATE participacion SET fecha_inicio = ?, fecha_fin = ?, fecha_retiro = ?, motivo_retiro = ?, estado = ? " +
                "WHERE id_participacion = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(p.getFechaInicio()));
            stmt.setDate(2, Date.valueOf(p.getFechaFin()));

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

    public boolean eliminar(int idParticipacion) throws SQLException {
        String sql = "DELETE FROM participacion WHERE id_participacion = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idParticipacion);
            return stmt.executeUpdate() > 0;
        }
    }

    private Participacion mapResultSet(ResultSet rs) throws SQLException {
        Participacion participacion = new Participacion();
        participacion.setIdParticipacion(rs.getInt("id_participacion")); // Asegúrate que coincida con tu BD

        // Leer tipo de la tabla integrante
        String tipoIntegrante = rs.getString("tipo"); // Viene de la tabla integrante
        if (tipoIntegrante == null) tipoIntegrante = "";

        PersonalDeInvestigacion personal;

        switch (tipoIntegrante.toUpperCase()) {
            case "AYUDANTE":
                personal = new Ayudante();
                break;
            case "TECNICO":
                personal = new Tecnico();
                break;
            case "ASISTENTE":
                personal = new Asistente();
                break;
            default:
                personal = new Ayudante();
        }

        // datos del personal
        personal.setCedula(rs.getString("cedula_personal")); // O rs.getString("cedula")
        personal.setNombres(rs.getString("nombres"));
        personal.setApellidos(rs.getString("apellidos"));
        personal.setCorreo(rs.getString("correo"));

        // objeto personal COMPLETO a la participación
        participacion.setPersonal(personal);

        Date fechaInicio = rs.getDate("fecha_inicio");
        if (fechaInicio != null) participacion.setFechaInicio(fechaInicio.toLocalDate());

        Date fechaFin = rs.getDate("fecha_fin");
        if (fechaFin != null) participacion.setFechaFin(fechaFin.toLocalDate());

        Date fechaRetiro = rs.getDate("fecha_retiro");
        if (fechaRetiro != null) participacion.setFechaRetiro(fechaRetiro.toLocalDate());

        participacion.setMotivoRetiro(rs.getString("motivo_retiro"));

        String estadoStr = rs.getString("estado");
        if (estadoStr != null) {
            participacion.setEstado(EstadoParticipacion.valueOf(estadoStr));
        }

        return participacion;
    }
}