package Logica.Enumeraciones;

public enum EstadoParticipacion {
    ACTIVO,
    RETIRADO,
    FINALIZADO;

    public static EstadoParticipacion fromString(String estado) {
        try {
            return valueOf(estado.toUpperCase());
        } catch (Exception e) {
            return ACTIVO;
        }
    }
}
