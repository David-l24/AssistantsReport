package Logica.Entidades;

import Logica.DAO.*;
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

    /**
     * Registra un proyecto en estado EN_REVISION.
     * El director aún no existe como entidad en la BD, por lo que sus datos
     * se guardan en los campos candidato_* del proyecto.
     */
    public void registrarProyecto(Proyecto proyecto) throws SQLException {
        ProyectoDAO proyectoDAO = new ProyectoDAO();

        // El director que tiene el objeto proyecto es temporal (solo con datos candidato).
        Director dirCandidato = proyecto.getDirector();

        proyectoDAO.guardarEnRevision(proyecto,
                dirCandidato != null ? dirCandidato.getNombres() : null,
                dirCandidato != null ? dirCandidato.getApellidos() : null,
                dirCandidato != null ? dirCandidato.getCedula() : null,
                dirCandidato != null ? dirCandidato.getCorreo() : null);
    }

    /**
     * Actualiza el estado del proyecto y ejecuta las acciones correspondientes.
     * Cuando se aprueba un proyecto EN_REVISION:
     * 1. Crea el usuario del director
     * 2. Crea la entidad Director (linking al usuario)
     * 3. Asigna cedula_director en el proyecto (FK)
     * 4. Actualiza el estado del proyecto
     * 5. Envía notificación al director
     */
    public void actualizarEstadoProyecto(Proyecto proyecto, EstadoProyecto nuevoEstado) throws SQLException {
        if (nuevoEstado == EstadoProyecto.APROBADO &&
                proyecto.getEstado() == EstadoProyecto.EN_REVISION) {

            Director dirCandidato = proyecto.getDirector();
            if (dirCandidato == null || dirCandidato.getCedula() == null) {
                throw new SQLException("El proyecto no tiene datos del director candidato");
            }

            // Paso 1: Crear Usuario del director
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            String username = generarUsernameDirector(proyecto);
            String contrasenaDefecto = UsuarioDAO.generarContrasenaDefecto(dirCandidato.getCedula());

            Usuario usuarioDirector = new Usuario(username, contrasenaDefecto, "DIRECTOR");
            int idUsuarioGenerado = usuarioDAO.guardar(usuarioDirector);

            if (idUsuarioGenerado == -1) {
                throw new SQLException("Error al crear usuario para el director");
            }

            // Paso 2: Crear entidad Director vinculada al usuario
            DirectorDAO directorDAO = new DirectorDAO();
            dirCandidato.setIdUsuario(idUsuarioGenerado);

            boolean directorCreado = directorDAO.guardar(dirCandidato);
            if (!directorCreado) {
                throw new SQLException("Error al crear el director");
            }

            // Paso 3: Asignar cedula_director en el proyecto (ahora el director existe)
            ProyectoDAO proyectoDAO = new ProyectoDAO();
            proyectoDAO.actualizarDirector(proyecto.getIdProyecto(), dirCandidato.getCedula());

            // Paso 4: Actualizar estado del proyecto
            proyecto.setEstado(nuevoEstado);
            proyectoDAO.actualizar(proyecto);

            // Paso 5: Enviar notificación al director
            NotificacionDAO notifDAO = new NotificacionDAO();
            Notificacion notif = new Notificacion();
            notif.setIdUsuario(idUsuarioGenerado);
            notif.setContenido("Su proyecto '" + proyecto.getNombre() +
                    "' ha sido aprobado. Usuario: " + username +
                    ". Por favor, ingrese y cambie su contraseña.");
            notifDAO.guardar(notif);

        } else {
            // Para otros cambios de estado, solo actualizar
            proyecto.setEstado(nuevoEstado);
            ProyectoDAO proyectoDAO = new ProyectoDAO();
            proyectoDAO.actualizar(proyecto);
        }
    }

    /**
     * Genera el username para el director basado en datos del proyecto
     * Ejemplo: "dir.proyecto123" o "dir.apellido"
     */
    private String generarUsernameDirector(Proyecto proyecto) {
        String base = "dir." + proyecto.getCodigoProyecto();
        return base.toLowerCase().replaceAll("[^a-z0-9.]", "");
    }

    /**
     * Genera un resumen de seguimiento para un proyecto específico
     * Compara lo planificado vs lo realmente registrado
     */
    public ResumenSeguimiento generarResumenSeguimiento(int idProyecto) throws SQLException {
        ResumenSeguimientoDAO resumenDAO = new ResumenSeguimientoDAO();
        return resumenDAO.generarResumen(idProyecto);
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

            // Notificar al director: el flujo exige que le llegue notificación
            ProyectoDAO proyectoDAO = new ProyectoDAO();
            Proyecto proyecto = proyectoDAO.obtenerPorId(reporte.getIdProyecto());
            if (proyecto != null && proyecto.getDirector() != null) {
                NotificacionDAO notifDAO = new NotificacionDAO();
                Notificacion notif = new Notificacion();
                notif.setIdUsuario(proyecto.getDirector().getIdUsuario());
                notif.setContenido("Su reporte del proyecto '" + proyecto.getNombre() +
                        "' ha sido aprobado por jefatura.");
                notifDAO.guardar(notif);
            }
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