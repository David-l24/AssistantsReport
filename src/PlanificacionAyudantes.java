public class PlanificacionAyudantes {

    private int cantidadSolicitada;
    private double presupuestoReferencial;
    private boolean tienePresupuestoAprobado;

    public boolean validarPlanificacion() {
        return cantidadSolicitada >= 0;
    }

    public void actualizarCantidad(int nuevaCantidad) {
        this.cantidadSolicitada = nuevaCantidad;
    }
}

