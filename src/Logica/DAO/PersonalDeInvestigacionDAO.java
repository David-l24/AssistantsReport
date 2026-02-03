package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonalDeInvestigacionDAO {
    private Connection connection;

    public PersonalDeInvestigacionDAO() throws SQLException {
        this.connection = ConexionBD.conectar();
    }

    public List<PersonalDeInvestigacion> obtenerTodos() throws SQLException {
        List<PersonalDeInvestigacion> lista = new ArrayList<>();
        String sql = "SELECT * FROM public.personaldeinvestigacion ORDER BY apellidos, nombres";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        }
        return lista;
    }

    public List<PersonalDeInvestigacion> obtenerPorTipo(String tipo) throws SQLException {
        String sql = "SELECT * FROM public.personaldeinvestigacion WHERE tipo = ? ORDER BY apellidos, nombres";

        // Nota: lista no estaba declarada dentro de este scope en el bloque anterior, corregido aquí:
        List<PersonalDeInvestigacion> lista = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tipo);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        }
        return lista;
    }

    public PersonalDeInvestigacion obtenerPorCedula(String cedula) throws SQLException {
        String sql = "SELECT * FROM public.personaldeinvestigacion WHERE cedula = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cedula);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null;
    }

    public boolean guardar(PersonalDeInvestigacion p) throws SQLException {
        // Se asume que id_proyecto no puede ser nulo en la BD.
        // Si el objeto Java no tiene proyecto (0), esto podría fallar si no hay FK válida.
        String sql = "INSERT INTO public.personaldeinvestigacion (cedula, id_usuario, nombres, apellidos, correo, tipo, id_proyecto) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, p.getCedula());
            stmt.setInt(2, p.getIdUsuario());
            stmt.setString(3, p.getNombres());
            stmt.setString(4, p.getApellidos());
            stmt.setString(5, p.getCorreo());
            stmt.setString(6, p.getClass().getSimpleName()); // Guarda "Ayudante", "Tecnico", etc.
            stmt.setInt(7, p.getIdProyecto());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean actualizar(PersonalDeInvestigacion p) throws SQLException {
        String sql = "UPDATE public.personaldeinvestigacion SET nombres = ?, apellidos = ?, correo = ?, id_proyecto = ? WHERE cedula = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, p.getNombres());
            stmt.setString(2, p.getApellidos());
            stmt.setString(3, p.getCorreo());
            stmt.setInt(4, p.getIdProyecto());
            stmt.setString(5, p.getCedula());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(String cedula) throws SQLException {
        String sql = "DELETE FROM public.personaldeinvestigacion WHERE cedula = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cedula);
            return stmt.executeUpdate() > 0;
        }
    }

    private PersonalDeInvestigacion mapResultSet(ResultSet rs) throws SQLException {
        PersonalDeInvestigacion persona = null;
        String tipoBD = rs.getString("tipo");

        if (tipoBD == null) tipoBD = "";

        switch (tipoBD.toUpperCase()) {
            case "AYUDANTE":
                persona = new Ayudante();
                break;
            case "ASISTENTE":
                persona = new Asistente();
                break;
            case "TECNICO":
            case "TÉCNICO":
                persona = new Tecnico();
                break;
            default:
                // Fallback por defecto
                persona = new Ayudante();
                break;
        }

        persona.setCedula(rs.getString("cedula"));
        persona.setIdUsuario(rs.getInt("id_usuario"));
        persona.setNombres(rs.getString("nombres"));
        persona.setApellidos(rs.getString("apellidos"));
        persona.setCorreo(rs.getString("correo"));
        persona.setIdProyecto(rs.getInt("id_proyecto"));

        return persona;
    }
}