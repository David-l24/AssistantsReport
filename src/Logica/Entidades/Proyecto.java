package Logica.Entidades;

import Logica.Enumeraciones.EstadoProyecto;

import java.util.ArrayList;
import java.util.List;

public abstract class Proyecto {
    private int idProyecto;
    private String codigoProyecto;
    private String nombre;
    private PeriodoAcademico periodoInicio;
    private int duracionMeses;
    private EstadoProyecto estado;
    private Director director;

    // Contadores planificados
    private int numAsistentesPlanificados;
    private int numAyudantesPlanificados;
    private int numTecnicosPlanificados;

    // LISTA DE PERSONAL (Relación inversa con la BDD)
    private List<PersonalDeInvestigacion> personalDeInvestigacion;

    public Proyecto() {
        // IMPORTANTE: Inicializar la lista para evitar NullPointerException
        this.personalDeInvestigacion = new ArrayList<>();
    }

    // Método abstracto para obligar a las hijas a definir su tipo
    public abstract String getTipoProyecto();

    // Métodos de lógica de negocio
    public void actualizarEstado(){
        // Lógica pendiente
    }

    public int getTotalPersonalPlanificado(){
        return numAsistentesPlanificados + numAyudantesPlanificados + numTecnicosPlanificados;
    }

    // --- Getters y Setters ---

    public int getId() { return idProyecto; } // Alias para getIdProyecto
    public int getIdProyecto() { return idProyecto; }
    public void setIdProyecto(int id) { this.idProyecto = id; }

    public String getCodigoProyecto() { return codigoProyecto; }
    public void setCodigoProyecto(String codigoProyecto) { this.codigoProyecto = codigoProyecto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public PeriodoAcademico getPeriodoInicio() { return periodoInicio; }
    public void setPeriodoInicio(PeriodoAcademico periodoInicio) { this.periodoInicio = periodoInicio; }

    public int getDuracionMeses() { return duracionMeses; }
    public void setDuracionMeses(int duracionMeses) { this.duracionMeses = duracionMeses; }

    public EstadoProyecto getEstado() { return estado; }
    public void setEstado(EstadoProyecto estado) { this.estado = estado; }

    public Director getDirector() { return director; }
    public void setDirector(Director director) { this.director = director; }

    public int getNumAsistentesPlanificados() { return numAsistentesPlanificados; }
    public void setNumAsistentesPlanificados(int num) { this.numAsistentesPlanificados = num; }

    public int getNumAyudantesPlanificados() { return numAyudantesPlanificados; }
    public void setNumAyudantesPlanificados(int num) { this.numAyudantesPlanificados = num; }

    public int getNumTecnicosPlanificados() { return numTecnicosPlanificados; }
    public void setNumTecnicosPlanificados(int num) { this.numTecnicosPlanificados = num; }

    // GETTER Y SETTER DE LA LISTA
    public List<PersonalDeInvestigacion> getPersonalDeInvestigacion() {
        return personalDeInvestigacion;
    }

    public void setPersonalDeInvestigacion(List<PersonalDeInvestigacion> personalDeInvestigacion) {
        this.personalDeInvestigacion = personalDeInvestigacion;
    }

    // Método helper para añadir uno a uno
    public void agregarPersonal(PersonalDeInvestigacion personal) {
        this.personalDeInvestigacion.add(personal);
    }
}