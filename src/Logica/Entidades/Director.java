package Logica.Entidades;

import Logica.DAO.NotificacionDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Director {
    private String cedula;
    private String nombres;
    private String apellidos;
    private String correo;
    private int idUsuario;
    private List<Notificacion> notificaciones;

    public Director() {
        this.notificaciones = new ArrayList<>();
    }

    public Director(String cedula, String nombres, String apellidos, String correo) {
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.notificaciones = new ArrayList<>();
    }

    // Getters y Setters
    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public List<Notificacion> getNotificaciones() {
        return notificaciones;
    }

    public void setNotificaciones(List<Notificacion> notificaciones) {
        this.notificaciones = notificaciones;
    }

    public String getNombresCompletos() {
        return nombres + " " + apellidos;
    }

    public void visualizarSeguimiento() {}
    public void registrarParticipacion() {}
    public void gestionarSeguimiento() {}
    public void actualizarContraseña() {}
    public void registrarPersonalDeInvestigacion() {}
    public void inciarReporte() {}
    public void editarReporte() {}
    public void cerrarReporte() {}
    public void enviarReporte() {}
    public void aprobarInformeDeActividades() {}
    public void revisarInformeDeActividades() {}


    public void cargarNotificaciones(Director director) throws SQLException {

        NotificacionDAO notificacionDAO = new NotificacionDAO();

        List<Notificacion> lista = notificacionDAO.obtenerPorDirector(director.getCedula());

        director.setNotificaciones(lista);
    }

    @Override
    public String toString() {
        return "Director{" +
                "cedula='" + cedula + '\'' +
                ", nombres='" + getNombresCompletos() + '\'' +
                ", correo='" + correo + '\'' +
                '}';
    }

    public void agregarNotificacion(Notificacion notificacion) {
        this.notificaciones.add(notificacion);
    }
}