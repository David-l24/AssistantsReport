package Logica.Enumeraciones;

public enum EstadoReporte {
    APROBADO,
    EN_EDICION,
    CERRADO,
    RECHAZADO;

    public static EstadoReporte fromString(String estado) {
        try {
            return valueOf(estado.toUpperCase());
        } catch (Exception e) {
            return EN_EDICION;
        }
    }
}