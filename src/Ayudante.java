public class Ayudante {

    private String nombreCompleto;
    private String identificacion;
    private String correoElectronico;
    private String estado;

    public boolean esActivo() {
        return "ACTIVO".equalsIgnoreCase(estado);
    }
}
