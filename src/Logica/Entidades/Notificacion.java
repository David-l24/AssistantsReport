package Logica.Entidades;

import java.time.LocalDateTime;

public class Notificacion {
    private int idNotificacion;
    private String contenido;
    private Director destinatario;
    private LocalDateTime fecha;

    public Notificacion() {
    }

    // Constructor para crear una nueva notificación (sin ID, la BD lo asigna)
    public Notificacion(String contenido, Director destinatario) {
        this.contenido = contenido;
        this.destinatario = destinatario;
        this.fecha = LocalDateTime.now(); // Asigna la fecha actual automáticamente
    }

    public void enviar(){
        if(destinatario != null) {
            // Aquí podrías llamar al DAO para guardar,
            // o simplemente añadirlo a la lista en memoria del director.
            destinatario.agregarNotificacion(this);
        }
    }

    // Getters y Setters
    public int getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(int idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Director getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Director destinatario) {
        this.destinatario = destinatario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}