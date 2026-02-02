package Logica.Entidades;

import Logica.Enumeraciones.EstadoReporte;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Reporte {
    private int idReporte;
    private String periodoAcademico;
    private int idProyecto;
    private EstadoReporte estado;
    private LocalDate fechaInicio;
    private LocalDate fechaCierre;

    // LISTA: Participaciones vinculadas a este reporte
    private List<Participacion> participacionesIncluidas;

    public Reporte() {
        // Inicializamos la lista para evitar errores
        this.participacionesIncluidas = new ArrayList<>();
    }

    public Reporte(String periodoAcademico, int idProyecto, EstadoReporte estado) {
        this();
        this.periodoAcademico = periodoAcademico;
        this.idProyecto = idProyecto;
        this.estado = estado;
        this.fechaInicio = LocalDate.now();
    }

    // --- Getters y Setters ---

    public int getIdReporte() { return idReporte; }
    public void setIdReporte(int idReporte) { this.idReporte = idReporte; }

    public String getPeriodoAcademico() { return periodoAcademico; }
    public void setPeriodoAcademico(String periodoAcademico) { this.periodoAcademico = periodoAcademico; }

    public int getIdProyecto() { return idProyecto; }
    public void setIdProyecto(int idProyecto) { this.idProyecto = idProyecto; }

    public EstadoReporte getEstado() { return estado; }
    public void setEstado(EstadoReporte estado) { this.estado = estado; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDate fechaCierre) { this.fechaCierre = fechaCierre; }

    // Getters y Setters de la Lista
    public List<Participacion> getParticipacionesIncluidas() { return participacionesIncluidas; }
    public void setParticipacionesIncluidas(List<Participacion> participacionesIncluidas) { this.participacionesIncluidas = participacionesIncluidas; }

    // Método helper
    public void agregarParticipacion(Participacion p) {
        this.participacionesIncluidas.add(p);
    }
}