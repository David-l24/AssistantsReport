package Logica.Entidades;

import java.sql.Time;
import java.util.Date;

/**
 * Representa una semana de actividades dentro de un Informe de Actividades
 * Cada semana puede tener hasta 7 días de trabajo con sus respectivas horas
 */
public class SemanaActividades {
    private int id;
    private int numeroSemana;

    /** Array de fechas trabajadas en la semana (máximo 7 días) */
    private Date[] fechas;

    /** Array de horas de entrada correspondientes a cada fecha */
    private Time[] horasInicio;

    /** Array de horas de salida correspondientes a cada fecha */
    private Time[] horasSalida;

    /** Descripción general de las actividades realizadas en la semana */
    private String actividadSemanal;

    /** Observaciones adicionales o comentarios de la semana */
    private String observaciones;

    /**
     * Constructor por defecto
     * Inicializa arrays para 5 días laborables
     */
    public SemanaActividades() {
        this.fechas = new Date[5];
        this.horasInicio = new Time[5];
        this.horasSalida = new Time[5];
    }

    /**
     * Constructor con número de semana
     * @param numeroSemana Número secuencial de la semana (1, 2, 3...)
     */
    public SemanaActividades(int numeroSemana) {
        this();
        this.numeroSemana = numeroSemana;
    }

    /**
     * Calcula el total de horas trabajadas en la semana
     * @return Total de horas como decimal
     */
    public double calcularHorasTotales() {
        double total = 0.0;

        for (int i = 0; i < fechas.length && fechas[i] != null; i++) {
            if (horasInicio[i] != null && horasSalida[i] != null) {
                long diferenciaMilisegundos = horasSalida[i].getTime() - horasInicio[i].getTime();
                double horas = diferenciaMilisegundos / (1000.0 * 60 * 60);
                total += horas;
            }
        }

        return total;
    }

    /**
     * Cuenta cuántos días se trabajó en la semana
     * @return Número de días con fecha registrada
     */
    public int contarDiasTrabajados() {
        int count = 0;
        for (Date fecha : fechas) {
            if (fecha != null) count++;
        }
        return count;
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumeroSemana() {
        return numeroSemana;
    }

    public void setNumeroSemana(int numeroSemana) {
        this.numeroSemana = numeroSemana;
    }

    public Date[] getFechas() {
        return fechas;
    }

    public void setFechas(Date[] fechas) {
        this.fechas = fechas;
    }

    public Time[] getHorasInicio() {
        return horasInicio;
    }

    public void setHorasInicio(Time[] horasInicio) {
        this.horasInicio = horasInicio;
    }

    public Time[] getHorasSalida() {
        return horasSalida;
    }

    public void setHorasSalida(Time[] horasSalida) {
        this.horasSalida = horasSalida;
    }

    public String getActividadSemanal() {
        return actividadSemanal;
    }

    public void setActividadSemanal(String actividadSemanal) {
        this.actividadSemanal = actividadSemanal;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "Semana " + numeroSemana +
                " (" + contarDiasTrabajados() + " días, " +
                String.format("%.1f", calcularHorasTotales()) + " horas)";
    }
}
