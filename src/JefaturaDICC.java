import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JefaturaDICC {

    private List<Proyecto> proyectos;

    private String identificacion;
    private String nombres;
    private boolean esAdministrador;


    public Proyecto registrarProyectoManual(Object datosProyecto, String idDirector, int numAyudantes) {

        if (proyectos == null) {
            proyectos = new ArrayList<>();
        }

        Proyecto proyecto = new Proyecto();

        try {
            Field codigo = Proyecto.class.getDeclaredField("codigo");
            codigo.setAccessible(true);
            codigo.set(proyecto, "PRJ-" + (proyectos.size() + 1));

            Field nombre = Proyecto.class.getDeclaredField("nombre");
            nombre.setAccessible(true);
            nombre.set(proyecto, (String) datosProyecto);

            Field estado = Proyecto.class.getDeclaredField("estado");
            estado.setAccessible(true);
            estado.set(proyecto, EstadoProyecto.SOLICITADO);

            Field fechaInicio = Proyecto.class.getDeclaredField("fechaInicio");
            fechaInicio.setAccessible(true);
            fechaInicio.set(proyecto, new Date());

        } catch (Exception e) {
            e.printStackTrace();
        }

        proyectos.add(proyecto);
        return proyecto;
    }


    public List<Proyecto> obtenerProyectosPendientes() {
        List<Proyecto> pendientes = new ArrayList<>();
        if (proyectos != null) {
            for (Proyecto p : proyectos) {
                if (!p.estaAprobado()) {
                    pendientes.add(p);
                }
            }
        }
        return pendientes;
    }

    public boolean confirmarAprobacionProyecto(String codigoProyecto) {

        if (proyectos == null) return false;

        for (Proyecto p : proyectos) {
            try {
                Field codigo = Proyecto.class.getDeclaredField("codigo");
                codigo.setAccessible(true);

                String valorCodigo = (String) codigo.get(p);

                if (valorCodigo.equals(codigoProyecto)) {
                    p.marcarComoAprobado();
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public boolean rechazarProyecto(String codigoProyecto, String motivo) {
        return true;
    }

    public void configurarFechaLimite(Object periodo, Date fecha) {
        // implementación mínima
    }

    public Object consultarEstadisticas() {
        return null;
    }
}

