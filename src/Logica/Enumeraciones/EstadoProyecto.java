package Logica.Enumeraciones;

public enum EstadoProyecto {
    EN_REVISION,
    APROBADO,
    NO_APROBADO,
    FINALIZADO;

    public static EstadoProyecto fromString(String estado) {
        try {
            return valueOf(estado.toUpperCase());
        } catch (Exception e) {
            return EN_REVISION;
        }
    }
}
