package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.Jefatura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JefaturaDAO {

    private Connection connection;

    public JefaturaDAO() throws SQLException {
        this.connection = ConexionBD.conectar();
    }

    public List<Jefatura> obtenerTodos() throws SQLException {
        List<Jefatura> jefaturas = new ArrayList<>();
        String sql = "SELECT * FROM public.jefatura ORDER BY apellidos, nombres";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                jefaturas.add(mapResultSet(rs));
            }
        }
        return jefaturas;
    }

    public Jefatura obtenerPorCedula(String cedula) throws SQLException {
        String sql = "SELECT * FROM public.jefatura WHERE cedula = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cedula);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null;
    }

    public Jefatura obtenerPorIdUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT * FROM public.jefatura WHERE id_usuario = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null;
    }

    public boolean guardar(Jefatura jefatura) throws SQLException {
        String sql = "INSERT INTO public.jefatura (cedula, id_usuario, nombres, apellidos, correo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, jefatura.getCedula());
            stmt.setInt(2, jefatura.getIdUsuario());
            stmt.setString(3, jefatura.getNombres());
            stmt.setString(4, jefatura.getApellidos());
            stmt.setString(5, jefatura.getCorreo());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Jefatura jefatura) throws SQLException {
        String sql = "UPDATE public.jefatura SET nombres = ?, apellidos = ?, correo = ? WHERE cedula = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, jefatura.getNombres());
            stmt.setString(2, jefatura.getApellidos());
            stmt.setString(3, jefatura.getCorreo());
            stmt.setString(4, jefatura.getCedula());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(String cedula) throws SQLException {
        String sql = "DELETE FROM public.jefatura WHERE cedula = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cedula);
            return stmt.executeUpdate() > 0;
        }
    }

    private Jefatura mapResultSet(ResultSet rs) throws SQLException {
        Jefatura jefatura = new Jefatura();
        jefatura.setCedula(rs.getString("cedula"));
        jefatura.setNombres(rs.getString("nombres"));
        jefatura.setApellidos(rs.getString("apellidos"));
        jefatura.setIdUsuario(rs.getInt("id_usuario"));
        jefatura.setCorreo(rs.getString("correo"));
        return jefatura;
    }
}