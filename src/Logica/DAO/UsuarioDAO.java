package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario autenticar(String username, String passwordPlana) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.conectar();
            String sql = "SELECT * FROM usuario WHERE username = ? AND activo = true";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            rs = stmt.executeQuery();

            if (rs.next()) {
                // 2. Recuperamos el Hash almacenado en la BD
                String hashGuardado = rs.getString("contrasena");

                // 3. Validamos la contraseña plana contra el hash usando BCrypt
                if (BCrypt.checkpw(passwordPlana, hashGuardado)) {
                    // Si coincide, retornamos el usuario mapeado
                    return mapResultSetToUsuario(rs);
                }
            }
            // Si no existe el usuario o la contraseña no coincide
            return null;

        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
    }


    public int guardar(Usuario usuario) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.conectar();

            // 1. Hashear la contraseña antes de guardarla
            String hashPassword = BCrypt.hashpw(usuario.getContrasena(), BCrypt.gensalt());

            String sql = "INSERT INTO usuario (username, contrasena, rol, activo) " +
                    "VALUES (?, ?, ?, ?) RETURNING id_usuario";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, hashPassword); // Guardamos el HASH, no el texto plano
            stmt.setString(3, usuario.getRol());
            stmt.setBoolean(4, usuario.isActivo());

            rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt(1);
                usuario.setIdUsuario(id);
                // Actualizamos el objeto en memoria con el hash por seguridad
                usuario.setContrasena(hashPassword);
                return id;
            }
            return -1;

        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
    }


    public boolean cambiarContrasena(int idUsuario, String nuevaContrasena) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConexionBD.conectar();

            // Hashear la nueva contraseña
            String nuevoHash = BCrypt.hashpw(nuevaContrasena, BCrypt.gensalt());

            String sql = "UPDATE usuario SET contrasena = ? WHERE id_usuario = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, nuevoHash);
            stmt.setInt(2, idUsuario);

            return stmt.executeUpdate() > 0;

        } finally {
            cerrarRecursos(null, stmt, conn);
        }
    }

    // --- MÉTODOS SIN CAMBIOS LÓGICOS (Solo estructura) ---

    public Usuario obtenerPorId(int idUsuario) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.conectar();
            String sql = "SELECT * FROM usuario WHERE id_usuario = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idUsuario);
            rs = stmt.executeQuery();
            if (rs.next()) return mapResultSetToUsuario(rs);
            return null;
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
    }

    public Usuario obtenerPorUsername(String username) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.conectar();
            String sql = "SELECT * FROM usuario WHERE username = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            if (rs.next()) return mapResultSetToUsuario(rs);
            return null;
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
    }

    public List<Usuario> obtenerTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.conectar();
            String sql = "SELECT * FROM usuario ORDER BY username";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) usuarios.add(mapResultSetToUsuario(rs));
            return usuarios;
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
    }

    public boolean actualizar(Usuario usuario) throws SQLException {
        // Nota: Este método NO toca la contraseña
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConexionBD.conectar();
            String sql = "UPDATE usuario SET username = ?, rol = ?, activo = ? WHERE id_usuario = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getRol());
            stmt.setBoolean(3, usuario.isActivo());
            stmt.setInt(4, usuario.getIdUsuario());
            return stmt.executeUpdate() > 0;
        } finally {
            cerrarRecursos(null, stmt, conn);
        }
    }

    public boolean eliminar(int idUsuario) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexionBD.conectar();
            String sql = "UPDATE usuario SET activo = false WHERE id_usuario = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate() > 0;
        } finally {
            cerrarRecursos(null, stmt, conn);
        }
    }

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setUsername(rs.getString("username"));
        // Aquí viene el HASH, no la contraseña plana
        usuario.setContrasena(rs.getString("contrasena"));
        usuario.setRol(rs.getString("rol"));
        usuario.setActivo(rs.getBoolean("activo"));
        return usuario;
    }

    private void cerrarRecursos(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) try { rs.close(); } catch (SQLException e) {}
        if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
        if (conn != null) try { conn.close(); } catch (SQLException e) {}
    }
}