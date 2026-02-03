package Logica.Entidades;

import Logica.DAO.*;
import Logica.Enumeraciones.EstadoParticipacion;
import Logica.Enumeraciones.EstadoReporte;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /**
     * Registra nuevo personal de investigación en el proyecto
     * Crea: Usuario, PersonalDeInvestigacion, Participacion inicial
     * Envía notificación con credenciales
     */
    public void registrarPersonalDeInvestigacion(PersonalDeInvestigacion personal,
                                                 int idProyecto,
                                                 LocalDate fechaInicioParticipacion) throws SQLException {
        // Paso 1: Crear Usuario
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        String username = generarUsernamePersonal(personal);
        String contrasenaDefecto = UsuarioDAO.generarContrasenaDefecto(personal.getCedula());

        Usuario usuarioPersonal = new Usuario(username, contrasenaDefecto, "PERSONAL");
        int idUsuarioGenerado = usuarioDAO.guardar(usuarioPersonal);

        if (idUsuarioGenerado == -1) {
            throw new SQLException("Error al crear usuario para el personal");
        }

        // Paso 2: Vincular usuario y proyecto al personal
        personal.setIdUsuario(idUsuarioGenerado);
        personal.setIdProyecto(idProyecto);

        // Paso 3: Guardar Personal
        PersonalDeInvestigacionDAO personalDAO = new PersonalDeInvestigacionDAO();
        personalDAO.guardar(personal);

        // Paso 4: Crear Participacion inicial
        ParticipacionDAO participacionDAO = new ParticipacionDAO();
        Participacion participacion = new Participacion();
        participacion.setPersonal(personal);
        participacion.setFechaInicio(fechaInicioParticipacion);
        participacion.setEstado(EstadoParticipacion.ACTIVO);
        participacionDAO.guardar(participacion);

        // Paso 5: Notificar al personal
        NotificacionDAO notifDAO = new NotificacionDAO();
        Notificacion notif = new Notificacion();
        notif.setFecha(LocalDateTime.now());
        notif.setIdUsuario(idUsuarioGenerado);
        notif.setContenido("Ha sido registrado en el proyecto. Usuario: " + username +
                ". Por favor, ingrese y cambie su contraseña.");
        notifDAO.guardar(notif);
    }

    /**
     * Genera username para personal
     * Ejemplo: "pers.apellido123"
     */
    private String generarUsernamePersonal(PersonalDeInvestigacion personal) {
        String apellido = personal.getApellidos().split(" ")[0]; // Primer apellido
        String cedula = personal.getCedula().substring(personal.getCedula().length() - 3);
        String base = "pers." + apellido + cedula;
        return base.toLowerCase().replaceAll("[^a-z0-9.]", "");
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

    /**
     * Inicia un nuevo reporte para el periodo académico actual
     * Valida que no existan ya 2 reportes para ese periodo
     */
    public int iniciarReporte(String periodoAcademico, int idProyecto) throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();

        // Validar que no existan ya 2 reportes para este periodo y proyecto
        List<Reporte> reportesExistentes = reporteDAO.obtenerPorProyectoYPeriodo(
                idProyecto, periodoAcademico);

        if (reportesExistentes.size() >= 2) {
            throw new SQLException("Ya existen 2 reportes para este periodo académico. " +
                    "No se pueden crear más.");
        }

        Reporte reporte = new Reporte(periodoAcademico, idProyecto, EstadoReporte.EN_EDICION);
        reporte.setFechaInicio(LocalDate.now());

        return reporteDAO.guardar(reporte);
    }

    /**
     * Cierra y envía el reporte a jefatura
     */
    public void enviarReporte(Reporte reporte) throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();
        reporte.cerrarYEnviar();
        reporteDAO.actualizar(reporte);

        // Notificar a jefatura
        NotificacionDAO notifDAO = new NotificacionDAO();
        // Aquí deberías obtener el id_usuario de jefatura
        // Por ahora, asumimos que hay un método para obtenerlo
        int idUsuarioJefatura = obtenerIdUsuarioJefatura();

        Notificacion notif = new Notificacion();
        notif.setIdUsuario(idUsuarioJefatura);
        notif.setFecha(LocalDateTime.now());
        notif.setContenido("El director " + this.getNombresCompletos() +
                " ha enviado un reporte para revisión.");
        notifDAO.guardar(notif);
    }

    /**
     * Obtiene el ID del usuario de jefatura consultando por rol directamente
     */
    private int obtenerIdUsuarioJefatura() throws SQLException {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        List<Usuario> jefaturas = usuarioDAO.obtenerPorRol("JEFATURA");

        if (jefaturas.isEmpty()) {
            throw new SQLException("No se encontró usuario de Jefatura en el sistema");
        }
        return jefaturas.get(0).getIdUsuario();
    }

    public void agregarParticipacionAReporte(int idReporte, int idParticipacion) throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();
        reporteDAO.agregarParticipacion(idReporte, idParticipacion);
    }

    public void editarReporte(Reporte reporte) throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();
        if (reporte.getEstado() == EstadoReporte.EN_EDICION) {
            // Ya está en edición, persiste cualquier cambio que se haya hecho
            reporteDAO.actualizar(reporte);
        } else {
            throw new SQLException("Solo se pueden editar reportes en estado EN_EDICION.");
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
        // aprobar() ya valida que el estado sea ENVIADO y envía notificación al personal
        informe.aprobar();

        // Persistir el cambio de estado en la BD
        InformeActividadesDAO dao = new InformeActividadesDAO();
        dao.actualizar(informe);
    }

    /**
     * Rechaza un informe de actividades con un motivo
     */
    public void rechazarInformeDeActividades(InformeActividades informe, String motivo) throws SQLException {
        // rechazar() valida estado y notifica al personal
        informe.rechazar(motivo);

        // Persistir
        InformeActividadesDAO dao = new InformeActividadesDAO();
        dao.actualizar(informe);
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


    /**
     * Verifica si es el primer login del director
     * Retorna true si necesita cambiar contraseña
     */
    public boolean debeActualizarContrasena() throws SQLException {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        return usuarioDAO.tieneContrasenaDefecto(this.idUsuario, this.cedula);
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