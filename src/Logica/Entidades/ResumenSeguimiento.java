package Logica.Entidades;

public class ResumenSeguimiento {
    // --- ASISTENTES (Solicitado explícitamente) ---
    private int cantidadAsistentesPlanificados;
    private int cantidadAsistentesRegistrados;
    private int cantidadAsistentesActivos;
    private int cantidadAsistentesRetirados;

    // --- AYUDANTES (Necesario para el total global) ---
    private int cantidadAyudantesPlanificados;
    private int cantidadAyudantesRegistrados;
    private int cantidadAyudantesActivos;
    private int cantidadAyudantesRetirados;

    // --- TÉCNICOS (Necesario para el total global) ---
    private int cantidadTecnicosPlanificados;
    private int cantidadTecnicosRegistrados;
    private int cantidadTecnicosActivos;
    private int cantidadTecnicosRetirados;

    // --- TOTALES GLOBALES ---
    private int totalPlanificado;
    private int totalRegistrado;
    private boolean cumplePlanificacionGlobal;

    public ResumenSeguimiento() {
    }

    /**
     * Realiza la sumatoria de los tres tipos de personal
     * y determina si se cumple el plan.
     */
    public void calcularTotales() {
        // 1. Calcular Total Planificado (Suma de lo requerido en el Proyecto)
        this.totalPlanificado = cantidadAsistentesPlanificados +
                cantidadAyudantesPlanificados +
                cantidadTecnicosPlanificados;

        // 2. Calcular Total Registrado (Suma de los que están en BDD)
        // Nota: Se suman tanto activos como retirados porque todos fueron registrados alguna vez.
        this.totalRegistrado = cantidadAsistentesRegistrados +
                cantidadAyudantesRegistrados +
                cantidadTecnicosRegistrados;

        // 3. Verificar cumplimiento
        this.cumplePlanificacionGlobal = verificarCumplimiento();
    }

    public boolean verificarCumplimiento() {
        // La regla de negocio básica: ¿Tenemos al menos tantos registrados como se planificó?
        // (Puedes ajustar la lógica si solo cuentan los 'Activos')
        return totalRegistrado >= totalPlanificado;
    }

    // --- GETTERS Y SETTERS ---

    // Asistentes
    public int getCantidadAsistentesPlanificados() { return cantidadAsistentesPlanificados; }
    public void setCantidadAsistentesPlanificados(int n) { this.cantidadAsistentesPlanificados = n; }
    public int getCantidadAsistentesRegistrados() { return cantidadAsistentesRegistrados; }
    public void setCantidadAsistentesRegistrados(int n) { this.cantidadAsistentesRegistrados = n; }
    public int getCantidadAsistentesActivos() { return cantidadAsistentesActivos; }
    public void setCantidadAsistentesActivos(int n) { this.cantidadAsistentesActivos = n; }
    public int getCantidadAsistentesRetirados() { return cantidadAsistentesRetirados; }
    public void setCantidadAsistentesRetirados(int n) { this.cantidadAsistentesRetirados = n; }

    // Ayudantes
    public int getCantidadAyudantesPlanificados() { return cantidadAyudantesPlanificados; }
    public void setCantidadAyudantesPlanificados(int n) { this.cantidadAyudantesPlanificados = n; }
    public int getCantidadAyudantesRegistrados() { return cantidadAyudantesRegistrados; }
    public void setCantidadAyudantesRegistrados(int n) { this.cantidadAyudantesRegistrados = n; }
    public int getCantidadAyudantesActivos() { return cantidadAyudantesActivos; }
    public void setCantidadAyudantesActivos(int n) { this.cantidadAyudantesActivos = n; }
    public int getCantidadAyudantesRetirados() { return cantidadAyudantesRetirados; }
    public void setCantidadAyudantesRetirados(int n) { this.cantidadAyudantesRetirados = n; }

    // Técnicos
    public int getCantidadTecnicosPlanificados() { return cantidadTecnicosPlanificados; }
    public void setCantidadTecnicosPlanificados(int n) { this.cantidadTecnicosPlanificados = n; }
    public int getCantidadTecnicosRegistrados() { return cantidadTecnicosRegistrados; }
    public void setCantidadTecnicosRegistrados(int n) { this.cantidadTecnicosRegistrados = n; }
    public int getCantidadTecnicosActivos() { return cantidadTecnicosActivos; }
    public void setCantidadTecnicosActivos(int n) { this.cantidadTecnicosActivos = n; }
    public int getCantidadTecnicosRetirados() { return cantidadTecnicosRetirados; }
    public void setCantidadTecnicosRetirados(int n) { this.cantidadTecnicosRetirados = n; }

    // Totales
    public int getTotalPlanificado() { return totalPlanificado; }
    public int getTotalRegistrado() { return totalRegistrado; }
    public boolean isCumplePlanificacionGlobal() { return cumplePlanificacionGlobal; }
}