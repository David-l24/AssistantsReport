package Logica.Entidades;

/**
 * Representa un Asistente de Investigación
 * Tipo de personal de investigación con responsabilidades de asistencia técnica
 */
public class Asistente extends PersonalDeInvestigacion {

    /**
     * Constructor por defecto
     */
    public Asistente() {
        super();
    }

    /**
     * Constructor con datos básicos
     */
    public Asistente(String cedula, String nombres, String apellidos, String correo) {
        super(cedula, nombres, apellidos, correo);
    }

    /**
     * Retorna el tipo de personal para almacenar en la base de datos
     * @return "Asistente"
     */
    @Override
    public String getTipo() {
        return "Asistente";
    }
}
