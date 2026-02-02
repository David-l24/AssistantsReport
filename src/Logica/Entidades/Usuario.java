package Logica.Entidades;

import Logica.Enumeraciones.RolSistema; // Asumiendo que tienes este Enum
// Si no tienes el Enum, cámbialo a String.

public class Usuario {
    private int idUsuario; // En BD es id_usuario
    private String username;
    private String contrasena; // En BD es contrasena
    private String rol; // En BD es VARCHAR(30)
    private boolean activo;

    public Usuario() {
        this.activo = true;
    }

    public Usuario(String username, String contrasena, String rol) {
        this();
        this.username = username;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    // Getters y Setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return username + " (" + rol + ")";
    }
}