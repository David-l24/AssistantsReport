package Logica.Entidades;

import Logica.DAO.NotificacionDAO;
import Logica.Enumeraciones.EstadoInforme;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InformeActividades {
    private int idInforme;
    private PersonalDeInvestigacion personalDeInvestigacion;
    private Proyecto proyecto;
    private LocalDate fechaRegistro;
    private EstadoInforme estado;

    // CAMBIO PRINCIPAL: Lista de semanas en lugar de arrays
    private List<SemanaActividades> semanas;

    public InformeActividades() {
        this.semanas = new ArrayList<>();
        this.estado = EstadoInforme.EN_EDICION;
        this.fechaRegistro = LocalDate.now();
    }

    // Métodos de lógica de negocio

    /**
     * Agrega una semana de actividades al informe
     */
    public void agregarSemana(SemanaActividades semana) {
        semana.setNumeroSemana(this.semanas.size() + 1);
        this.semanas.add(semana);
    }

    /**
     * Envía el informe al director para revisión
     */
    public void enviarParaRevision() throws SQLException {
        if (this.estado != EstadoInforme.EN_EDICION) {
            throw new IllegalStateException("Solo se pueden enviar informes en edición");
        }
        if (this.semanas.isEmpty()) {
            throw new IllegalStateException("El informe debe tener al menos una semana");
        }

        this.estado = EstadoInforme.ENVIADO;

        // Notificar al director
        if (proyecto != null && proyecto.getDirector() != null) {
            NotificacionDAO notifDAO = new NotificacionDAO();
            Notificacion notif = new Notificacion();
            notif.setIdUsuario(proyecto.getDirector().getIdUsuario());
            notif.setContenido(personalDeInvestigacion.getNombresCompletos() +
                    " ha enviado un informe de actividades para revisión.");
            notifDAO.guardar(notif);
        }
    }

    /**
     * El director aprueba el informe
     */
    public void aprobar() throws SQLException {
        if (this.estado != EstadoInforme.ENVIADO) {
            throw new IllegalStateException("Solo se pueden aprobar informes enviados");
        }

        this.estado = EstadoInforme.APROBADO;

        // Notificar al personal
        NotificacionDAO notifDAO = new NotificacionDAO();
        Notificacion notif = new Notificacion();
        notif.setIdUsuario(personalDeInvestigacion.getIdUsuario());
        notif.setContenido("Su informe de actividades ha sido aprobado.");
        notifDAO.guardar(notif);
    }

    /**
     * El director rechaza el informe con motivo
     */
    public void rechazar(String motivo) throws SQLException {
        if (this.estado != EstadoInforme.ENVIADO) {
            throw new IllegalStateException("Solo se pueden rechazar informes enviados");
        }

        this.estado = EstadoInforme.RECHAZADO;

        // Notificar al personal con el motivo
        NotificacionDAO notifDAO = new NotificacionDAO();
        Notificacion notif = new Notificacion();
        notif.setIdUsuario(personalDeInvestigacion.getIdUsuario());
        notif.setContenido("Su informe de actividades ha sido rechazado. Motivo: " + motivo);
        notifDAO.guardar(notif);
    }

    /**
     * Calcula total de horas trabajadas en todo el informe
     */
    public double calcularHorasTotales() {
        return semanas.stream()
                .mapToDouble(SemanaActividades::calcularHorasTotales)
                .sum();
    }

    // Getters y Setters

    public int getIdInforme() { return idInforme; }
    public void setIdInforme(int idInforme) { this.idInforme = idInforme; }

    public PersonalDeInvestigacion getPersonalDeInvestigacion() {
        return personalDeInvestigacion;
    }
    public void setPersonalDeInvestigacion(PersonalDeInvestigacion personal) {
        this.personalDeInvestigacion = personal;
    }

    public Proyecto getProyecto() { return proyecto; }
    public void setProyecto(Proyecto proyecto) { this.proyecto = proyecto; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public EstadoInforme getEstado() { return estado; }
    public void setEstado(EstadoInforme estado) { this.estado = estado; }

    public List<SemanaActividades> getSemanas() { return semanas; }
    public void setSemanas(List<SemanaActividades> semanas) {
        this.semanas = semanas;
    }
}