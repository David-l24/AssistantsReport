package Logica.Entidades;

import java.time.LocalDateTime;

public class Notificacion {
    private int idNotificacion;
    private int idUsuario; // Identificador universal del usuario (Director, Jefe o Personal)
    private String contenido;
    private LocalDateTime fecha;

    // Constructor vacío
    public Notificacion() {
    }

    // Constructor para crear nueva notificación
    public Notificacion(int idUsuario, String contenido) {
        this.idUsuario = idUsuario;
        this.contenido = contenido;
        this.fecha = LocalDateTime.now();
    }

    // Constructor completo (para mapeo desde BD)
    public Notificacion(int idNotificacion, int idUsuario, String contenido, LocalDateTime fecha) {
        this.idNotificacion = idNotificacion;
        this.idUsuario = idUsuario;
        this.contenido = contenido;
        this.fecha = fecha;
    }

    public int getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(int idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "Notificacion{" +
                "id=" + idNotificacion +
                ", idUsuario=" + idUsuario +
                ", contenido='" + contenido + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}