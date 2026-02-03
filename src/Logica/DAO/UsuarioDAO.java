package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    /**
     * Genera una contraseña por defecto basada en la cédula.
     * Formato: "Temporal@" + últimos 4 dígitos de cédula
     */
    public static String generarContrasenaDefecto(String cedula) {
        if (cedula == null || cedula.length() < 4) {
            return "Temporal@1234";
        }
        String ultimos4 = cedula.substring(cedula.length() - 4);
        return "Temporal@" + ultimos4;
    }

    /**
     * Verifica si la contraseña actual del usuario es la por defecto.
     * Útil para forzar cambio en primer login.
     */
    public boolean tieneContrasenaDefecto(int idUsuario, String cedula) throws SQLException {
        Usuario usuario = obtenerPorId(idUsuario);
        if (usuario == null) return false;

        String contrasenaDefecto = generarContrasenaDefecto(cedula);
        return BCrypt.checkpw(contrasenaDefecto, usuario.getContrasena());
    }

    /**
     * Autentica un usuario con username y contraseña.
     * @return Usuario autenticado o null si falla
     */
    public Usuario autenticar(String username, String passwordPlana) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE username = ? AND activo = true";

        try (PreparedStatement stmt = ConexionBD.conectar().prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashGuardado = rs.getString("contrasena");
                    if (BCrypt.checkpw(passwordPlana, hashGuardado)) {
                        return mapResultSetToUsuario(rs);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Guarda un nuevo usuario en la base de datos.
     * La contraseña se hashea automáticamente antes de guardar.
     * @return ID del usuario generado, o -1 si falla
     */
    public int guardar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (username, contrasena, rol, activo) " +
                "VALUES (?, ?, ?, ?) RETURNING id_usuario";

        String hashPassword = BCrypt.hashpw(usuario.getContrasena(), BCrypt.gensalt());

        try (PreparedStatement stmt = ConexionBD.conectar().prepareStatement(sql)) {
            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, hashPassword);
            stmt.setString(3, usuario.getRol());
            stmt.setBoolean(4, usuario.isActivo());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    usuario.setIdUsuario(id);
                    usuario.setContrasena(hashPassword);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Cambia la contraseña de un usuario.
     * La nueva contraseña se hashea automáticamente.
     */
    public boolean cambiarContrasena(int idUsuario, String nuevaContrasena) throws SQLException {
        String nuevoHash = BCrypt.hashpw(nuevaContrasena, BCrypt.gensalt());
        String sql = "UPDATE usuario SET contrasena = ? WHERE id_usuario = ?";

        try (PreparedStatement stmt = ConexionBD.conectar().prepareStatement(sql)) {
            stmt.setString(1, nuevoHash);
            stmt.setInt(2, idUsuario);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Obtiene un usuario por su ID
     */
    public Usuario obtenerPorId(int idUsuario) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE id_usuario = ?";

        try (PreparedStatement stmt = ConexionBD.conectar().prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToUsuario(rs);
            }
        }
        return null;
    }

    /**
     * Obtiene un usuario por su username
     */
    public Usuario obtenerPorUsername(String username) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE username = ?";

        try (PreparedStatement stmt = ConexionBD.conectar().prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToUsuario(rs);
            }
        }
        return null;
    }

    /**
     * Obtiene todos los usuarios del sistema
     */
    public List<Usuario> obtenerTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY username";

        try (Statement stmt = ConexionBD.conectar().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) usuarios.add(mapResultSetToUsuario(rs));
        }
        return usuarios;
    }

    /**
     * Obtiene todos los usuarios con un rol específico
     */
    public List<Usuario> obtenerPorRol(String rol) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario WHERE rol = ? AND activo = true";

        try (PreparedStatement stmt = ConexionBD.conectar().prepareStatement(sql)) {
            stmt.setString(1, rol);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) usuarios.add(mapResultSetToUsuario(rs));
            }
        }
        return usuarios;
    }

    /**
     * Actualiza los datos de un usuario (NO toca la contraseña).
     * Para cambiar contraseña usar cambiarContrasena().
     */
    public boolean actualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuario SET username = ?, rol = ?, activo = ? WHERE id_usuario = ?";

        try (PreparedStatement stmt = ConexionBD.conectar().prepareStatement(sql)) {
            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getRol());
            stmt.setBoolean(3, usuario.isActivo());
            stmt.setInt(4, usuario.getIdUsuario());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Desactiva un usuario (eliminación lógica)
     */
    public boolean eliminar(int idUsuario) throws SQLException {
        String sql = "UPDATE usuario SET activo = false WHERE id_usuario = ?";

        try (PreparedStatement stmt = ConexionBD.conectar().prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate() > 0;
        }
    }

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setUsername(rs.getString("username"));
        usuario.setContrasena(rs.getString("contrasena")); // Es el hash
        usuario.setRol(rs.getString("rol"));
        usuario.setActivo(rs.getBoolean("activo"));
        return usuario;
    }
}
