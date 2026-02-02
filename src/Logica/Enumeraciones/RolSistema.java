package Logica.Enumeraciones;

/**
 * Enum para roles del sistema
 * Alineado con el tipo rol_sistema de PostgreSQL
 */
public enum RolSistema {
    ADMIN("ADMIN"),
    DIRECTOR("DIRECTOR");

    private final String nombre;

    RolSistema(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public static RolSistema fromString(String nombre) {
        for (RolSistema rol : RolSistema.values()) {
            if (rol.nombre.equalsIgnoreCase(nombre)) {
                return rol;
            }
        }
        return DIRECTOR; // Por defecto
    }
}
