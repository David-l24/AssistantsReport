package Logica.Entidades;
import Logica.DAO.*;
import Logica.Enumeraciones.EstadoProyecto;
import Logica.Enumeraciones.EstadoReporte;
import java.sql.SQLException;
import java.util.List;

public class Jefatura {

    private String nombres;
    private String apellidos;
    private String correo;
    private String cedula;
    private int idUsuario;

    public Jefatura (){

    }

    public Jefatura(String nombres, String apellidos, String correo, String cedula) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.cedula = cedula;
    }

    //GETS y SETS

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

    public String getCorreo(){
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCedula(){
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    //RESTO FUNCIONES

    /**
     * Registra un nuevo proyecto en la base de datos.
     */
    public void registrarProyecto(Proyecto proyecto) throws SQLException {
        ProyectoDAO proyectoDAO = new ProyectoDAO();
        proyectoDAO.guardar(proyecto);
    }
    //Revisar este método ya que se supone que un proyecto se debe crear con más datos

    /**
     * Actualiza el estado de un proyecto existente.
     */
    public void actualizarEstadoProyecto(Proyecto proyecto, EstadoProyecto nuevoEstado) throws SQLException {
        ProyectoDAO proyectoDAO = new ProyectoDAO();

        proyecto.setEstado(nuevoEstado);
        proyectoDAO.actualizar(proyecto);
    }

    /**
     * Obtiene todos los reportes semestrales para revisión.
     */
    public List<Reporte> revisarReportes() throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();
        return reporteDAO.obtenerTodos();
    }

    /**
     * Aprueba un reporte semestral.
     */
    public void aprobarReporte(Reporte reporte) throws SQLException {
        ReporteDAO reporteDAO = new ReporteDAO();

        if (reporte.getEstado() == EstadoReporte.CERRADO) {
            reporte.setEstado(EstadoReporte.APROBADO);
            reporteDAO.actualizar(reporte);
        } else {
            throw new SQLException("Solo se pueden aprobar reportes cerrados.");
        }
    }

    /**
     * Obtiene un informe de actividades específico para revisión.
     */
    public InformeActividades revisarInformeDeActividades(int idInforme) throws SQLException {
        InformeActividadesDAO informeDAO = new InformeActividadesDAO();
        return informeDAO.obtenerPorId(idInforme);
    }
}
