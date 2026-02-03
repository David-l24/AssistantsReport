package Logica.Entidades;

import Logica.DAO.InformeActividadesDAO;
import Logica.DAO.NotificacionDAO;
import Logica.DAO.ProyectoDAO;
import Logica.DAO.UsuarioDAO;
import Logica.Enumeraciones.EstadoInforme;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class PersonalDeInvestigacion {
    private String cedula;
    private int idUsuario;
    private int idProyecto; // Añadido para coincidir con la FK de la tabla
    private String nombres;
    private String apellidos;
    private String correo;

    private List<InformeActividades> informesActividades;

    // Cambiado a protected para acceso en herencia si fuera necesario
    protected List<Notificacion> notificaciones;

    public PersonalDeInvestigacion() {
        this.notificaciones = new ArrayList<>();
        this.informesActividades = new ArrayList<>();
    }

    public PersonalDeInvestigacion(String cedula, String nombres, String apellidos, String correo) {
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.notificaciones = new ArrayList<>();
        this.informesActividades = new ArrayList<>();
    }

    // LÓGICA DE NEGOCIO

    public void cargarNotificaciones() throws SQLException {
        NotificacionDAO notificacionDAO = new NotificacionDAO();
        this.notificaciones = notificacionDAO.obtenerPorUsuario(this.idUsuario);
    }

    public void agregarInforme(InformeActividades informeActividades) {
        if (!this.informesActividades.contains(informeActividades)) {
            this.informesActividades.add(informeActividades);
            // Establecemos la relación bidireccional
            informeActividades.setPersonalDeInvestigacion(this);
        }
    }

    /**
     * Crea un nuevo informe de actividades para este personal
     */
    public InformeActividades iniciarInformeActividades() throws SQLException {
        InformeActividades informe = new InformeActividades();
        informe.setPersonalDeInvestigacion(this);
        informe.setProyecto(obtenerProyectoActual());
        informe.setFechaRegistro(LocalDate.now());
        informe.setEstado(EstadoInforme.EN_EDICION);

        return informe;
    }

    /**
     * Edita un informe existente (solo si está en edición)
     */
    public void editarInformeActividades(InformeActividades informe) throws SQLException {
        if (informe.getEstado() != EstadoInforme.EN_EDICION) {
            throw new IllegalStateException("Solo se pueden editar informes en estado EN_EDICION");
        }

        InformeActividadesDAO dao = new InformeActividadesDAO();
        dao.actualizar(informe);
    }

    /**
     * Cierra y envía el informe al director
     */
    public void cerrarInformeActividades(InformeActividades informe) throws SQLException {
        if (informe.getEstado() != EstadoInforme.EN_EDICION) {
            throw new IllegalStateException("El informe ya fue enviado");
        }

        informe.enviarParaRevision();

        InformeActividadesDAO dao = new InformeActividadesDAO();
        dao.actualizar(informe);
    }

    /**
     * Obtiene el proyecto en el que este personal está actualmente
     */
    private Proyecto obtenerProyectoActual() throws SQLException {
        ProyectoDAO proyectoDAO = new ProyectoDAO();
        return proyectoDAO.obtenerPorId(this.idProyecto);
    }

    /**
     * Verifica si debe cambiar contraseña en primer login
     */
    public boolean debeActualizarContrasena() throws SQLException {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        return usuarioDAO.tieneContrasenaDefecto(this.idUsuario, this.cedula);
    }

    /**
     * Actualiza la contraseña del personal
     */
    public void actualizarContrasena(String nuevaContrasena) throws SQLException {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        boolean exito = usuarioDAO.cambiarContrasena(this.idUsuario, nuevaContrasena);
        if (!exito) {
            throw new SQLException("Error al actualizar la contraseña.");
        }
    }


    // GETTERS Y SETTERS

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

    public int getIdProyecto() { return idProyecto; }
    public void setIdProyecto(int idProyecto) { this.idProyecto = idProyecto; }

    public List<Notificacion> getNotificaciones() { return notificaciones; }
    public void setNotificaciones(List<Notificacion> notificaciones) { this.notificaciones = notificaciones; }


    public List<InformeActividades> getInformesActividades() { return informesActividades; }

    public void setInformesActividades(List<InformeActividades> informesActividades) {
        this.informesActividades = informesActividades;
    }

    public String getNombresCompletos() {
        return nombres + " " + apellidos;
    }

    public abstract String getTipo();

    @Override
    public String toString() {
        return "Personal{" +
                "cedula='" + cedula + '\'' +
                ", nombres='" + getNombresCompletos() + '\'' +
                ", numInformes=" + (informesActividades != null ? informesActividades.size() : 0) +
                ", tipo='" + this.getClass().getSimpleName() + '\'' +
                '}';
    }
}