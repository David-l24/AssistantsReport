package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.PersonalDeInvestigacion;
import Logica.Entidades.Ayudante;
import Logica.Entidades.Asistente; // Asegúrate de tener esta clase creada
import Logica.Entidades.Tecnico;   // Asegúrate de tener esta clase creada
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
        // Ordenamos por apellido para que se vea bien en listas
        String sql = "SELECT * FROM integrante ORDER BY apellidos, nombres";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        }
        return lista;
    }

    public List<PersonalDeInvestigacion> obtenerPorTipo(String tipo) throws SQLException {
        List<PersonalDeInvestigacion> lista = new ArrayList<>();
        // OJO: En tu imagen la columna se llama 'tipo', no 'tipo_integrante'
        String sql = "SELECT * FROM integrante WHERE tipo = ? ORDER BY apellidos, nombres";

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
        // OJO: En tu imagen la columna PK es 'cedula', no 'identificacion'
        String sql = "SELECT * FROM integrante WHERE cedula = ?";

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
        // Quitamos carrera y semestre. Agregamos 'tipo' que es obligatorio para saber quién es.
        String sql = "INSERT INTO integrante (cedula, nombres, apellidos, correo, tipo, id_usuario) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, p.getCedula());
            stmt.setString(2, p.getNombres());
            stmt.setString(3, p.getApellidos());
            stmt.setString(4, p.getCorreo());

            // MAGIA AQUÍ: Obtenemos el nombre de la clase (Ayudante, Tecnico, etc.)
            // y lo guardamos en la BD para saber qué es en el futuro.
            stmt.setString(5, p.getClass().getSimpleName());
            stmt.setInt(6, p.getIdUsuario());


            return stmt.executeUpdate() > 0;
        }
    }

    public boolean actualizar(PersonalDeInvestigacion p) throws SQLException {
        String sql = "UPDATE integrante SET nombres = ?, apellidos = ?, correo = ? WHERE cedula = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, p.getNombres());
            stmt.setString(2, p.getApellidos());
            stmt.setString(3, p.getCorreo());
            stmt.setString(4, p.getCedula());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(String cedula) throws SQLException {
        String sql = "DELETE FROM integrante WHERE cedula = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cedula);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Método Factory: Decide qué instancia crear según la columna 'tipo' de la BD.
     */
    private PersonalDeInvestigacion mapResultSet(ResultSet rs) throws SQLException {
        PersonalDeInvestigacion persona = null;

        // 1. Obtenemos el tipo discriminador de la base de datos
        String tipoBD = rs.getString("tipo");

        // Si el campo es nulo, debemos manejarlo (o lanzar error)
        if (tipoBD == null) {
            tipoBD = "";
        }

        // 2. Instanciamos la clase correcta
        switch (tipoBD.toUpperCase()) { // Convertimos a mayúsculas para evitar errores de tipeo
            case "AYUDANTE":
                persona = new Ayudante();
                break;
            case "ASISTENTE":
                persona = new Asistente();
                break;
            case "TECNICO":
            case "TÉCNICO": // Por si acaso guardaron con tilde
                persona = new Tecnico();
                break;
            default:
                // Opción por defecto o lanzar excepción si el tipo es desconocido
                // Por ahora usamos Ayudante como fallback para no romper el programa
                persona = new Ayudante();
                System.out.println("ADVERTENCIA: Tipo desconocido en BD: " + tipoBD);
                break;
        }

        // 3. Llenamos los datos comunes del padre
        persona.setCedula(rs.getString("cedula"));
        persona.setNombres(rs.getString("nombres"));
        persona.setApellidos(rs.getString("apellidos"));
        persona.setCorreo(rs.getString("correo"));

        // Nota: usuarioId e id_proyecto están en la tabla pero no en tu clase Java.
        // Si los necesitas, agrégalos a la clase padre.

        return persona;
    }
}