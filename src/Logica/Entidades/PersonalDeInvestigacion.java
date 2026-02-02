package Logica.Entidades;

/**
 * Entidad base Integrante alineada con la tabla 'integrante' de la BD
 * Todos los tipos de integrantes (Ayudante, Asistente, Técnico) heredan de aquí
 */
public abstract class PersonalDeInvestigacion {
    private String cedula;
    private int idUsuario;
    private String nombres;
    private String apellidos;
    private String correo;

    public PersonalDeInvestigacion() {

    }

    public PersonalDeInvestigacion(String cedula, String nombres, String apellidos, String correo) {
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
    }


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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombresCompletos() {
        return nombres + " " + apellidos;
    }

    @Override
    public String toString() {
        return "Integrante{" +
                "cedula='" + cedula + '\'' +
                ", nombres='" + getNombresCompletos() + '\'' +
                ", tipo='" + this.getClass().getSimpleName() + '\'' +
                '}';
    }
}
