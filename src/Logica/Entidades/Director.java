package Logica.Entidades;

import Logica.DAO.*;
import Logica.Enumeraciones.EstadoParticipacion;
import Logica.Enumeraciones.EstadoReporte;

import java.sql.SQLException;
import java.time.LocalDate;
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

    // --- LÓGICA DE NEGOCIO ---

    public void registrarPersonalDeInvestigacion(PersonalDeInvestigacion personal) throws SQLException {
        PersonalDeInvestigacionDAO personalDAO = new PersonalDeInvestigacionDAO();
        personalDAO.guardar(personal);
    }

    public void registrarParticipacion(String cedulaPersonal, LocalDate fechaInicio) throws SQLException {
        ParticipacionDAO participacionDAO = new ParticipacionDAO();
        Participacion p = new Participacion();
        // Usamos una clase concreta (Ayudante) solo para envolver la cédula,
        // ya que Participacion requiere un objeto Personal.
        PersonalDeInvestigacion dummy = new Ayudante();
        dummy.setCedula(cedulaPersonal);

        p.setPersonal(dummy);
        p.setFechaInicio(fechaInicio);
        p.setEstado(EstadoParticipacion.ACTIVO);

        participacionDAO.guardar(p);
    }

    // Gestión de Reportes
    public int iniciarReporte(String periodoAcademico, int idProyecto) throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();
        Reporte reporte = new Reporte(periodoAcademico, idProyecto, EstadoReporte.EN_EDICION);
        return reporteDAO.guardar(reporte);
    }

    public void agregarParticipacionAReporte(int idReporte, int idParticipacion) throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();
        reporteDAO.agregarParticipacion(idReporte, idParticipacion);
    }

    public void editarReporte(Reporte reporte) throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();
        if (reporte.getEstado() != EstadoReporte.CERRADO) {
            reporte.setEstado(EstadoReporte.EN_EDICION);
            reporteDAO.actualizar(reporte);
        } else {
            throw new SQLException("No se puede editar un reporte cerrado.");
        }
    }

    public void cerrarReporte(Reporte reporte) throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();
        reporte.setEstado(EstadoReporte.CERRADO);
        reporte.setFechaCierre(LocalDate.now());
        reporteDAO.actualizar(reporte);
    }

    // Gestión de Informes Semanales
    public InformeActividades revisarInformeDeActividades(int idInforme) throws SQLException {
        InformeActividadesDAO informeDAO = new InformeActividadesDAO();
        return informeDAO.obtenerPorId(idInforme);
    }

    public void aprobarInformeDeActividades(InformeActividades informe) throws SQLException {
        informe.setEstado(EstadoReporte.APROBADO);
        // Aquí podrías llamar al DAO para persistir el cambio
        // InformeActividadesDAO dao = new InformeActividadesDAO();
        // dao.actualizar(informe);
    }

    // Gestión de Cuenta y Notificaciones
    public void actualizarContrasena(String nuevaContrasena) throws SQLException {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        boolean exito = usuarioDAO.cambiarContrasena(this.idUsuario, nuevaContrasena);
        if (!exito) {
            throw new SQLException("Error al actualizar la contraseña.");
        }
    }

    public void cargarNotificaciones() throws SQLException {
        NotificacionDAO notificacionDAO = new NotificacionDAO();
        // Carga usando el ID de usuario, no la cédula
        this.notificaciones = notificacionDAO.obtenerPorUsuario(this.idUsuario);
    }

    public void agregarNotificacion(Notificacion notificacion) {
        this.notificaciones.add(notificacion);
    }

    // --- GETTERS Y SETTERS ---

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public List<Notificacion> getNotificaciones() { return notificaciones; }
    public void setNotificaciones(List<Notificacion> notificaciones) { this.notificaciones = notificaciones; }

    public String getNombresCompletos() {
        return nombres + " " + apellidos;
    }

    @Override
    public String toString() {
        return "Director{" + "cedula='" + cedula + '\'' + ", nombres='" + getNombres() + " " + getApellidos() + '\'' + '}';
    }
}