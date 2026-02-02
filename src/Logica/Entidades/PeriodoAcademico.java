package Logica.Entidades;

import java.time.LocalDate;

public class PeriodoAcademico {
    private String codigo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaMitad;

    public PeriodoAcademico() {}

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    
    public LocalDate getFechaMitad() { return fechaMitad; }
    public void setFechaMitad(LocalDate fecha) { this.fechaMitad = fecha; }
}
