package Logica.Enumeraciones;

/**
 * Estados posibles para un Informe de Actividades
 *
 * Ciclo de vida:
 * EN_EDICION -> ENVIADO -> APROBADO
 *                      └-> RECHAZADO -> EN_EDICION (puede volver a editarse)
 */
public enum EstadoInforme {
    /** El personal está creando o editando el informe */
    EN_EDICION,

    /** El personal envió el informe al director para revisión */
    ENVIADO,

    /** El director aprobó el informe */
    APROBADO,

    /** El director rechazó el informe (puede volver a editarse) */
    RECHAZADO;

    /**
     * Convierte un string a EstadoInforme de forma segura
     * @param estado String con el nombre del estado
     * @return EstadoInforme correspondiente, o EN_EDICION si no se puede convertir
     */
    public static EstadoInforme fromString(String estado) {
        try {
            return valueOf(estado.toUpperCase());
        } catch (Exception e) {
            return EN_EDICION;
        }
    }
}
