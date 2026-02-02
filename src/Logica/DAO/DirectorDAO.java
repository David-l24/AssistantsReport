package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.Director;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DirectorDAO {
    private Connection connection;

    public DirectorDAO() throws SQLException {
        this.connection = ConexionBD.conectar();
    }

    public List<Director> obtenerTodos() throws SQLException {
        List<Director> directores = new ArrayList<>();
        String sql = "SELECT * FROM Director ORDER BY apellidos, nombres";


        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                directores.add(mapResultSet(rs));
            }
        }
        return directores;
    }

    public Director obtenerPorCedula(String cedula) throws SQLException {
        String sql = "SELECT * FROM Director WHERE cedula = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cedula);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null;
    }

    public Director obtenerPorIdUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT * FROM Director WHERE id_usuario = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null;
    }

    public boolean guardar(Director director) throws SQLException {
        String sql = "INSERT INTO Director (cedula, id_usuario, nombres, apellidos, correo) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, director.getCedula());
            stmt.setInt(2, director.getIdUsuario());
            stmt.setString(3, director.getNombres());
            stmt.setString(4, director.getApellidos());
            stmt.setString(5, director.getCorreo());

            return stmt.executeUpdate() > 0;
        }
    }

    // Método Actualizar corregido: Coincidencia de parámetros
    public boolean actualizar(Director director) throws SQLException {
        String sql = "UPDATE Director SET nombres = ?, apellidos = ?, correo = ? WHERE cedula = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, director.getNombres());
            stmt.setString(2, director.getApellidos());
            stmt.setString(3, director.getCorreo());
            // El WHERE usa la Cédula (PK)
            stmt.setString(4, director.getCedula());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(String cedula) throws SQLException {
        String sql = "DELETE FROM Director WHERE cedula = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cedula);
            return stmt.executeUpdate() > 0;
        }
    }

    private Director mapResultSet(ResultSet rs) throws SQLException {
        Director director = new Director();
        director.setCedula(rs.getString("cedula"));
        director.setNombres(rs.getString("nombres"));
        director.setApellidos(rs.getString("apellidos"));
        director.setIdUsuario(rs.getInt("id_usuario"));
        director.setCorreo(rs.getString("correo"));

        return director;
    }
}