package Logica.Entidades;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import Logica.Enumeraciones.EstadoParticipacion;
import Logica.DAO.*;


public class Participacion {
    private int idParticipacion;
    private PersonalDeInvestigacion personal; //
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaRetiro;
    private String motivoRetiro;
    private EstadoParticipacion estado;

    // Constructor vacío
    public Participacion() {

    }

    public Participacion(PersonalDeInvestigacion personal, LocalDate fechaInicio, LocalDate fechaFin, EstadoParticipacion estado) {
        this.personal = personal;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }


    public boolean esActivo() {
        return this.estado == EstadoParticipacion.ACTIVO;
    }
    public boolean esRetirado() {
        return this.estado == EstadoParticipacion.RETIRADO;
    }
    public boolean esFinalizado() {
        return this.estado == EstadoParticipacion.FINALIZADO;
    }

    // Registra la baja de un integrante.
    // Actualiza el objeto y llama a la BD.

    public void registrarRetiro(String motivo) throws SQLException {
        if (!esActivo()) {
            throw new IllegalStateException("El participante ya está retirado o finalizado.");
        }

        this.motivoRetiro = motivo;
        this.fechaRetiro = LocalDate.now();
        this.estado = EstadoParticipacion.RETIRADO;

        // Llamada al DAO para persistir el cambio
        ParticipacionDAO dao = new ParticipacionDAO();
        dao.actualizar(this);
    }


    // Calcula cuántos días ha trabajado la persona en el proyecto

    public long calcularDuracionDias() {
        LocalDate fin = (fechaRetiro != null) ? fechaRetiro : LocalDate.now();
        return ChronoUnit.DAYS.between(fechaInicio, fin);
    }


    public int getIdParticipacion() { return idParticipacion; }
    public void setIdParticipacion(int idParticipacion) { this.idParticipacion = idParticipacion; }

    public PersonalDeInvestigacion getPersonal() { return personal; }

    public void setPersonal(PersonalDeInvestigacion personal) { this.personal = personal; }

    public String getCedulaPersonal() {
        return (personal != null) ? personal.getCedula() : null;
    }

    public void setCedulaPersonal(String cedula) {
        if (this.personal == null) {
            this.personal = new Ayudante();
        }
        this.personal.setCedula(cedula);
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public LocalDate getFechaRetiro() { return fechaRetiro; }
    public void setFechaRetiro(LocalDate fechaRetiro) { this.fechaRetiro = fechaRetiro; }

    public String getMotivoRetiro() { return motivoRetiro; }
    public void setMotivoRetiro(String motivoRetiro) { this.motivoRetiro = motivoRetiro; }

    public EstadoParticipacion getEstado() { return estado; }
    public void setEstado(EstadoParticipacion estado) { this.estado = estado; }
}