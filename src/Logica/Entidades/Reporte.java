package Logica.Entidades;

import Logica.Enumeraciones.EstadoReporte;
import java.time.LocalDate;

public class Reporte {
    private int idReporte;          // BD: id_reporte
    private String periodoAcademico;// BD: periodo_academico
    private int idProyecto;         // BD: id_proyecto (INTEGER)
    private EstadoReporte estado;   // BD: estado
    private LocalDate fechaInicio;  // BD: fecha_inicio (DATE)
    private LocalDate fechaCierre;  // BD: fecha_cierre (DATE)

    public Reporte() {
        // Valores por defecto al crear uno nuevo en Java
        this.estado = EstadoReporte.EN_EDICION;
        this.fechaInicio = LocalDate.now();
    }

    // Constructor opcional para facilitar la creación
    public Reporte(String periodoAcademico, int idProyecto) {
        this();
        this.periodoAcademico = periodoAcademico;
        this.idProyecto = idProyecto;
    }

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
}