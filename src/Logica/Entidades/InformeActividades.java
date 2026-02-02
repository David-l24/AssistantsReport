package Logica.Entidades;

import Logica.Enumeraciones.EstadoReporte; // Importamos tu Enum
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InformeActividades {

    private int id;
    private int idInforme;
    private PersonalDeInvestigacion personalDeInvestigacion;
    private Proyecto proyecto;
    private LocalDate fechaRegistro;

    // CAMBIO: Ahora usamos el Enum fuertemente tipado
    private EstadoReporte estado;

    // Matrices y Listas (Se mantienen igual)
    private Date[][] fechas;
    private Time[][] horasInicio;
    private Time[][] horasFin;
    private List<String> actividadesSemanales;
    private List<String> observacionesSemanales;

    public InformeActividades() {
        this.actividadesSemanales = new ArrayList<>();
        this.observacionesSemanales = new ArrayList<>();
        this.estado = EstadoReporte.EN_EDICION;
    }

    public InformeActividades(int numeroSemanas) {
        this();
        this.fechas = new Date[numeroSemanas][];
        this.horasInicio = new Time[numeroSemanas][];
        this.horasFin = new Time[numeroSemanas][];
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdInforme() { return idInforme; }
    public void setIdInforme(int idInforme) {
        this.idInforme = idInforme;
        this.id = idInforme;
    }

    public PersonalDeInvestigacion getPersonalDeInvestigacion() { return personalDeInvestigacion; }
    public void setPersonalDeInvestigacion(PersonalDeInvestigacion personalDeInvestigacion) { this.personalDeInvestigacion = personalDeInvestigacion; }

    public Proyecto getProyecto() { return proyecto; }
    public void setProyecto(Proyecto proyecto) { this.proyecto = proyecto; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    // GETTER Y SETTER TIPADOS CON EL ENUM
    public EstadoReporte getEstado() { return estado; }
    public void setEstado(EstadoReporte estado) { this.estado = estado; }

    // (Resto de getters de matrices igual...)
    public Date[][] getFechas() { return fechas; }
    public void setFechas(Date[][] fechas) { this.fechas = fechas; }
    public Time[][] getHorasInicio() { return horasInicio; }
    public void setHorasInicio(Time[][] horasInicio) { this.horasInicio = horasInicio; }
    public Time[][] getHorasFin() { return horasFin; }
    public void setHorasFin(Time[][] horasFin) { this.horasFin = horasFin; }
    public List<String> getActividadesSemanales() { return actividadesSemanales; }
    public void setActividadesSemanales(List<String> actividadesSemanales) { this.actividadesSemanales = actividadesSemanales; }
    public List<String> getObservacionesSemanales() { return observacionesSemanales; }
    public void setObservacionesSemanales(List<String> observacionesSemanales) { this.observacionesSemanales = observacionesSemanales; }
}