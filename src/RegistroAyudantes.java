import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegistroAyudantes {

    private String id;
    private String estado;
    private Date fechaCreacion;
    private Date fechaCierre;

    private List<Ayudante> ayudantes;
    private PeriodoAcademico periodoAcademico;

    public boolean agregarAyudante(Ayudante ayudante) {
        if (ayudantes == null) {
            ayudantes = new ArrayList<>();
        }
        return ayudantes.add(ayudante);
    }

    public boolean validarCompletitud() {
        return ayudantes != null && !ayudantes.isEmpty();
    }

    public String generarResumen() {
        return "Registro " + id + " - Total ayudantes: " +
                (ayudantes == null ? 0 : ayudantes.size());
    }

    public boolean cerrarRegistro() {
        this.fechaCierre = new Date();
        this.estado = "CERRADO";
        return true;
    }
}

