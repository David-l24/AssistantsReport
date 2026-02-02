import org.mindrot.jbcrypt.BCrypt;

public class Main {
    public static void main(String[] args) {


        String passwordOriginal = "miPasswordSeguro123";

        // 1. HASHEAR: Se genera un salt automático y se aplica el hash
        // El segundo parámetro (log_rounds) define la complejidad (default es 10)
        String hashed = BCrypt.hashpw(passwordOriginal, BCrypt.gensalt(12));

        System.out.println("Password Original: " + passwordOriginal);
        System.out.println("Hash generado: " + hashed);

        // 2. VERIFICAR: Comprobamos si las contraseñas coinciden
        String passwordPrueba = "miPasswordSeguro123";
        String passwordErroneo = "passwordIncorrecto";

        boolean esCorrecto = BCrypt.checkpw(passwordPrueba, hashed);
        boolean esIncorrecto = BCrypt.checkpw(passwordErroneo, hashed);

        System.out.println("\n--- Resultados de la verificación ---");
        System.out.println("¿Coincide '" + passwordPrueba + "'?: " + esCorrecto);
        System.out.println("¿Coincide '" + passwordErroneo + "'?: " + esIncorrecto);

    }
}