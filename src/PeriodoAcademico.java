import java.util.Date;

public class PeriodoAcademico {

    private String codigo;
    private String nombre;
    private Date fechaInicio;
    private Date fechaFin;

    public boolean esActivo() {
        Date hoy = new Date();
        return hoy.after(fechaInicio) && hoy.before(fechaFin);
    }
}
