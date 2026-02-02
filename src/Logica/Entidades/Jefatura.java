package Logica.Entidades;

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

    public void registrarProyecto(){

    }

    public void actualizarEstadoProyecto() {

    }

    public void revisasReportes() {

    }

    public void aprobarReportes() {

    }

    public void revisarInformesDeActividades() {

    }
}
