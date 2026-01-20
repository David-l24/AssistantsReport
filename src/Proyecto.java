import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Proyecto {

    private String codigo;
    private String nombre;
    private EstadoProyecto estado;
    private Date fechaInicio;
    private Date fechaFinalizacion;
    private Date fechaAprobacionJefatura;

    private Director director;
    private PlanificacionAyudantes planificacionAyudantes;
    private List<RegistroAyudantes> registros;

    public void solicitarAprobacion() {
        this.estado = EstadoProyecto.SOLICITADO;
    }

    public void marcarComoAprobado() {
        this.estado = EstadoProyecto.APROBADO;
        this.fechaAprobacionJefatura = new Date();
    }

    public boolean estaAprobado() {
        return estado == EstadoProyecto.APROBADO;
    }

    public String obtenerInformacion() {
        return codigo + " - " + nombre + " (" + estado + ")";
    }
}
