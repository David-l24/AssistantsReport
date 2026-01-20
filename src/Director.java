import java.util.List;

public class Director {

    private String identificacion;
    private String nombres;
    private String apellidos;
    private String correoInstitucional;

    private List<Proyecto> proyectos;

    public String obtenerDatos() {
        return nombres + " " + apellidos + " - " + correoInstitucional;
    }

    public List<Proyecto> obtenerProyectos() {
        return proyectos;
    }
}
