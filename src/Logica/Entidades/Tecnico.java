package Logica.Entidades;

/**
 * Representa un Técnico de Investigación
 * Tipo de personal de investigación con responsabilidades técnicas especializadas
 */
public class Tecnico extends PersonalDeInvestigacion {

    /**
     * Constructor por defecto
     */
    public Tecnico() {
        super();
    }

    /**
     * Constructor con datos básicos
     */
    public Tecnico(String cedula, String nombres, String apellidos, String correo) {
        super(cedula, nombres, apellidos, correo);
    }

    /**
     * Retorna el tipo de personal para almacenar en la base de datos
     * @return "Tecnico"
     */
    @Override
    public String getTipo() {
        return "Tecnico";
    }
}
