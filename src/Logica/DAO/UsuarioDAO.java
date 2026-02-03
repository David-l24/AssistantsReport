package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    /**
     * Genera una contraseña por defecto basada en la cédula
     * Formato: "Temporal@" + últimos 4 dígitos de cédula
     *
     * @param cedula Cédula del usuario (10 dígitos)
     * @return Contraseña por defecto generada
     */
    public static String generarContrasenaDefecto(String cedula) {
        if (cedula == null || cedula.length() < 4) {
            return "Temporal@1234";
        }
        String ultimos4 = cedula.substring(cedula.length() - 4);
        return "Temporal@" + ultimos4;
    }

    /**
     * Verifica si la contraseña actual del usuario es la por defecto
     * Útil para forzar cambio en primer login
     *
     * @param idUsuario ID del usuario a verificar
     * @param cedula Cédula del usuario para generar la contraseña por defecto
     * @return true si el usuario tiene la contraseña por defecto
     */
    public boolean tieneContrasenaDefecto(int idUsuario, String cedula) throws SQLException {
        Usuario usuario = obtenerPorId(idUsuario);
        if (usuario == null) return false;

        String contrasenaDefecto = generarContrasenaDefecto(cedula);
        // Comparar el hash guardado con la contraseña por defecto
        return BCrypt.checkpw(contrasenaDefecto, usuario.getContrasena());
    }

    /**
     * Autentica un usuario con username y contraseña
     *
     * @param username Nombre de usuario
     * @param passwordPlana Contraseña en texto plano
     * @return Usuario autenticado o null si falla
     */
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
                // Recuperamos el Hash almacenado en la BD
                String hashGuardado = rs.getString("contrasena");

                // Validamos la contraseña plana contra el hash usando BCrypt
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

    /**
     * Guarda un nuevo usuario en la base de datos
     * La contraseña se hashea automáticamente antes de guardar
     *
     * @param usuario Usuario a guardar
     * @return ID del usuario generado, o -1 si falla
     */
    public int guardar(Usuario usuario) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.conectar();

            // Hashear la contraseña antes de guardarla
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

    /**
     * Cambia la contraseña de un usuario
     * La nueva contraseña se hashea automáticamente
     *
     * @param idUsuario ID del usuario
     * @param nuevaContrasena Nueva contraseña en texto plano
     * @return true si se cambió exitosamente
     */
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

    /**
     * Obtiene un usuario por su ID
     */
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

    /**
     * Obtiene un usuario por su username
     */
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

    /**
     * Obtiene todos los usuarios del sistema
     */
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

    /**
     * Actualiza los datos de un usuario (NO toca la contraseña)
     * Para cambiar contraseña, usar cambiarContrasena()
     */
    public boolean actualizar(Usuario usuario) throws SQLException {
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

    /**
     * Desactiva un usuario (eliminación lógica)
     */
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

    /**
     * Mapea un ResultSet a un objeto Usuario
     */
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

    /**
     * Cierra recursos de forma segura
     */
    private void cerrarRecursos(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) try { rs.close(); } catch (SQLException e) {}
        if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
        if (conn != null) try { conn.close(); } catch (SQLException e) {}
    }
}
