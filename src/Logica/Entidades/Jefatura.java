package Logica.Entidades;

import Logica.DAO.InformeActividadesDAO;
import Logica.DAO.NotificacionDAO;
import Logica.DAO.ProyectoDAO;
import Logica.DAO.ReporteDAO;
import Logica.Enumeraciones.EstadoProyecto;
import Logica.Enumeraciones.EstadoReporte;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Jefatura {

    private String nombres;
    private String apellidos;
    private String correo;
    private String cedula;
    private int idUsuario;
    private List<Notificacion> notificaciones;

    public Jefatura (){
        this.notificaciones = new ArrayList<>();
    }

    public Jefatura(String nombres, String apellidos, String correo, String cedula) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.cedula = cedula;
        this.notificaciones = new ArrayList<>();
    }

    // --- LÓGICA DE NEGOCIO ---

    public void registrarProyecto(Proyecto proyecto) throws SQLException {
        ProyectoDAO proyectoDAO = new ProyectoDAO();
        proyectoDAO.guardar(proyecto);
    }

    public void actualizarEstadoProyecto(Proyecto proyecto, EstadoProyecto nuevoEstado) throws SQLException {
        ProyectoDAO proyectoDAO = new ProyectoDAO();
        proyecto.setEstado(nuevoEstado);
        proyectoDAO.actualizar(proyecto);
    }

    public List<Reporte> revisarReportes() throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();
        return reporteDAO.obtenerTodos();
    }

    public void aprobarReporte(Reporte reporte) throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();
        if (reporte.getEstado() == EstadoReporte.CERRADO) {
            reporte.setEstado(EstadoReporte.APROBADO);
            reporteDAO.actualizar(reporte);
        } else {
            throw new SQLException("Solo se pueden aprobar reportes cerrados.");
        }
    }

    public InformeActividades revisarInformeDeActividades(int idInforme) throws SQLException {
        InformeActividadesDAO informeDAO = new InformeActividadesDAO();
        return informeDAO.obtenerPorId(idInforme);
    }

    public void cargarNotificaciones() throws SQLException {
        NotificacionDAO notificacionDAO = new NotificacionDAO();
        // Usa idUsuario
        this.notificaciones = notificacionDAO.obtenerPorUsuario(this.idUsuario);
    }

    public void agregarNotificacion(Notificacion notificacion) {
        this.notificaciones.add(notificacion);
    }

    // --- GETTERS Y SETTERS ---

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public List<Notificacion> getNotificaciones() { return notificaciones; }
    public void setNotificaciones(List<Notificacion> notificaciones) { this.notificaciones = notificaciones; }
}