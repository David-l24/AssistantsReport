package Logica.Entidades;

/**
 * Representa un Ayudante de Investigación
 * Tipo de personal de investigación con responsabilidades de apoyo
 */
public class Ayudante extends PersonalDeInvestigacion {

    /**
     * Constructor por defecto
     */
    public Ayudante() {
        super();
    }

    /**
     * Constructor con datos básicos
     */
    public Ayudante(String cedula, String nombres, String apellidos, String correo) {
        super(cedula, nombres, apellidos, correo);
    }

    /**
     * Retorna el tipo de personal para almacenar en la base de datos
     * @return "Ayudante"
     */
    @Override
    public String getTipo() {
        return "Ayudante";
    }
}
