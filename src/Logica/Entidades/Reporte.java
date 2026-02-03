package Logica.Entidades;

import Logica.Enumeraciones.EstadoReporte;

import java.sql.SQLException;
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

    /**
     * Determina si este es el primer o segundo reporte del periodo
     * Basado en la fecha de inicio
     */
    public int getNumeroReporte(PeriodoAcademico periodo) {
        if (periodo == null || periodo.getFechaMitad() == null) {
            return 1;
        }

        // Si la fecha de inicio es antes de la mitad, es el primer reporte
        if (this.fechaInicio.isBefore(periodo.getFechaMitad())) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * Obtiene la fecha límite de este reporte según el periodo
     */
    public LocalDate getFechaLimite(PeriodoAcademico periodo) {
        if (periodo == null) return null;

        if (getNumeroReporte(periodo) == 1) {
            // Primer reporte: fecha límite es la mitad del periodo
            return periodo.getFechaMitad();
        } else {
            // Segundo reporte: fecha límite es 7 días antes del fin
            return periodo.getFechaFin().minusDays(7);
        }
    }

    /**
     * Verifica si el reporte está atrasado
     */
    public boolean estaAtrasado(PeriodoAcademico periodo) {
        if (this.estado == EstadoReporte.CERRADO || this.estado == EstadoReporte.APROBADO) {
            return false; // Ya fue enviado
        }

        LocalDate limite = getFechaLimite(periodo);
        return limite != null && LocalDate.now().isAfter(limite);
    }

    /**
     * Cierra el reporte y lo envía a jefatura
     */
    public void cerrarYEnviar() throws SQLException {
        if (this.estado != EstadoReporte.EN_EDICION) {
            throw new IllegalStateException("Solo se pueden cerrar reportes en edición");
        }

        if (participacionesIncluidas.isEmpty()) {
            throw new IllegalStateException("El reporte debe incluir al menos una participación");
        }

        this.estado = EstadoReporte.CERRADO;
        this.fechaCierre = LocalDate.now();

        // Notificar a jefatura (necesitarás obtener el id_usuario de jefatura de alguna forma)
        // Esto se puede mejorar con una tabla de configuración o búsqueda de usuarios con rol JEFATURA
    }

    // Getters y Setters de la Lista
    public List<Participacion> getParticipacionesIncluidas() { return participacionesIncluidas; }
    public void setParticipacionesIncluidas(List<Participacion> participacionesIncluidas) { this.participacionesIncluidas = participacionesIncluidas; }

    // Método helper
    public void agregarParticipacion(Participacion p) {
        this.participacionesIncluidas.add(p);
    }
}