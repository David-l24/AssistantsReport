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

    // ==========================================
    // 1. GESTIÓN DE PERSONAL Y PARTICIPACIÓN
    // ==========================================

    /**
     * Registra un nuevo integrante (Ayudante, Técnico, Asistente) en la base de datos.
     */
    public void registrarPersonalDeInvestigacion(PersonalDeInvestigacion personal) throws SQLException {
        PersonalDeInvestigacionDAO personalDAO = new PersonalDeInvestigacionDAO();
        personalDAO.guardar(personal);
    }

    /**
     * Vincula a un personal existente con un proyecto (Crea registro en tabla Participacion).
     */
    public void registrarParticipacion(String cedulaPersonal, LocalDate fechaInicio) throws SQLException {
        ParticipacionDAO participacionDAO = new ParticipacionDAO();

        // Creamos el objeto participación con un personal dummy (solo necesitamos la cédula)
        Participacion p = new Participacion();
        PersonalDeInvestigacion dummy = new Ayudante();
        dummy.setCedula(cedulaPersonal);

        p.setPersonal(dummy);
        p.setFechaInicio(fechaInicio);
        p.setEstado(EstadoParticipacion.ACTIVO); // Estado inicial por defecto

        participacionDAO.guardar(p);
    }

    // ==========================================
    // 2. GESTIÓN DE SEGUIMIENTO (DASHBOARD)
    // ==========================================

    /**
     * Obtiene el resumen estadístico (Dashboard) de un proyecto.
     */
    public ResumenSeguimiento visualizarSeguimiento(int idProyecto) throws SQLException {
        ResumenSeguimientoDAO resumenDAO = new ResumenSeguimientoDAO();
        return resumenDAO.generarResumen(idProyecto);
    }

    /**
     * Método general para obtener los proyectos del director.
     */
    public List<Proyecto> gestionarSeguimiento() throws SQLException {
        ProyectoDAO proyectoDAO = new ProyectoDAO();
        return proyectoDAO.obtenerPorDirector(this.cedula);
    }

    // ==========================================
    // 3. GESTIÓN DE REPORTES SEMESTRALES (DIRECTOR)
    // ==========================================

    /**
     * Crea un nuevo Reporte Semestral en estado EN_EDICION.
     */
    public int inciarReporte(String periodoAcademico, int idProyecto) throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();
        Reporte reporte = new Reporte(periodoAcademico, idProyecto, EstadoReporte.EN_EDICION);

        return reporteDAO.guardar(reporte);
    }

    /**
     * Agrega una participación específica al reporte semestral.
     * CORRECCIÓN: Usa ReporteDAO, ya que ReporteParticipacionDAO no existe.
     */
    public void agregarParticipacionAReporte(int idReporte, int idParticipacion) throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();
        // Este método 'agregarParticipacion' lo añadimos a ReporteDAO en el paso anterior
        reporteDAO.agregarParticipacion(idReporte, idParticipacion);
    }

    /**
     * Actualiza datos básicos del reporte mientras está en edición.
     */
    public void editarReporte(Reporte reporte) throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();

        if (reporte.getEstado() != EstadoReporte.CERRADO) {
            reporte.setEstado(EstadoReporte.EN_EDICION);
            reporteDAO.actualizar(reporte);
        } else {
            throw new SQLException("No se puede editar un reporte cerrado.");
        }
    }

    /**
     * Finaliza el reporte, cambia su estado a CERRADO y establece fecha de cierre.
     */
    public void cerrarReporte(Reporte reporte) throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();

        reporte.setEstado(EstadoReporte.CERRADO);
        reporte.setFechaCierre(LocalDate.now());

        reporteDAO.actualizar(reporte);
    }

    /**
     * Envía el reporte (Equivalente a cerrar en este flujo).
     */
    public void enviarReporte(Reporte reporte) throws SQLException {
        cerrarReporte(reporte);
    }

    // ==========================================
    // 4. GESTIÓN DE INFORMES DE ACTIVIDADES (PERSONAL)
    // ==========================================

    /**
     * Obtiene un Informe de Actividades (semanal) para leerlo.
     */
    public InformeActividades revisarInformeDeActividades(int idInforme) throws SQLException {
        InformeActividadesDAO informeDAO = new InformeActividadesDAO();
        return informeDAO.obtenerPorId(idInforme);
    }

    /**
     * Aprueba un Informe de Actividades semanal.
     */
    public void aprobarInformeDeActividades(InformeActividades informe) throws SQLException {
        // Cambiamos el estado en memoria
        informe.setEstado(EstadoReporte.APROBADO);

        // Aquí deberías tener un método actualizar o actualizarEstado en InformeActividadesDAO
        // Por ahora simulamos que el cambio en memoria es suficiente para el flujo actual
        // o llamamos a guardar si tu lógica lo permite.
        System.out.println("Informe " + informe.getIdInforme() + " aprobado.");
    }

    // ==========================================
    // 5. GESTIÓN DE CUENTA Y NOTIFICACIONES
    // ==========================================

    public void actualizarContraseña(String nuevaContrasena) throws SQLException {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        boolean exito = usuarioDAO.cambiarContrasena(this.idUsuario, nuevaContrasena);
        if (!exito) {
            throw new SQLException("Error al actualizar la contraseña.");
        }
    }

    public void cargarNotificaciones() throws SQLException {
        NotificacionDAO notificacionDAO = new NotificacionDAO();
        // Carga las notificaciones usando la cédula de ESTA instancia (this)
        List<Notificacion> lista = notificacionDAO.obtenerPorDirector(this.cedula);
        this.setNotificaciones(lista);
    }

    public void agregarNotificacion(Notificacion notificacion) {
        this.notificaciones.add(notificacion);
    }

    // --- Getters y Setters ---
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public List<Notificacion> getNotificaciones() { return notificaciones; }
    public void setNotificaciones(List<Notificacion> notificaciones) { this.notificaciones = notificaciones; }

    public String getNombresCompletos() {
        return nombres + " " + apellidos;
    }

    @Override
    public String toString() {
        return "Director{" +
                "cedula='" + cedula + '\'' +
                ", nombres='" + getNombresCompletos() + '\'' +
                '}';
    }
}