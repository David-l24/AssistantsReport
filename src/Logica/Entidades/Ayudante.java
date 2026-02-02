package Logica.Entidades;

public class Ayudante extends PersonalDeInvestigacion {

    public Ayudante() {

    }

    public Ayudante(String cedula, String nombres, String apellidos, String correo) {
        // Llamamos al constructor de PersonalDeInvestigacion
        super(cedula, nombres, apellidos, correo);
    }
}
