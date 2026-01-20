import java.lang.reflect.Field;
import java.util.*;

public class Main {

    private static List<Proyecto> proyectos = new ArrayList<>();

    private static Map<String, List<Proyecto>> proyectosPorDirector = new HashMap<>();
    private static Map<Proyecto, Integer> ayudantesPorProyecto = new HashMap<>();
    private static Map<String, String> nombreDirectorPorId = new HashMap<>();
    private static Map<Proyecto, List<RegistroAyudantes>> enviosPorProyecto = new HashMap<>();

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        JefaturaDICC jefatura = new JefaturaDICC();

        boolean salir = false;

        while (!salir) {

            System.out.println("\n===== SISTEMA DE AVALES =====");
            System.out.println("1. Ingresar como Jefatura DICC");
            System.out.println("2. Ingresar como Director");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opcion: ");

            int opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1 -> menuJefatura(sc, jefatura);
                case 2 -> menuDirector(sc);
                case 0 -> salir = true;
                default -> System.out.println("Opcion invalida");
            }
        }

        sc.close();
    }

    private static void menuJefatura(Scanner sc, JefaturaDICC jefatura) {

        System.out.println("\n--- JEFATURA DICC ---");
        System.out.println("1. Registrar aval de proyecto");
        System.out.println("2. Ver proyectos registrados");
        System.out.println("3. Aprobar proyecto");
        System.out.println("4. Ver avances enviados");
        System.out.println("5. Aprobar avance");
        System.out.println("6. Rechazar avance");
        System.out.print("Opcion: ");

        int opcion = sc.nextInt();
        sc.nextLine();

        switch (opcion) {

            // ===== REGISTRAR AVAL =====
            case 1 -> {
                System.out.print("Nombre del proyecto: ");
                String nombreProyecto = sc.nextLine();

                System.out.print("ID del Director: ");
                String idDirector = sc.nextLine();

                System.out.print("Nombre completo del Director: ");
                String nombreDirector = sc.nextLine();

                System.out.print("Numero de ayudantes presupuestados: ");
                int numAyudantes = sc.nextInt();
                sc.nextLine();

                Proyecto proyecto = jefatura.registrarProyectoManual(
                        nombreProyecto,
                        idDirector,
                        numAyudantes
                );

                proyectos.add(proyecto);

                proyectosPorDirector
                        .computeIfAbsent(idDirector, k -> new ArrayList<>())
                        .add(proyecto);

                ayudantesPorProyecto.put(proyecto, numAyudantes);
                nombreDirectorPorId.put(idDirector, nombreDirector);

                System.out.println("Aval registrado correctamente");
            }

            case 2 -> {
                System.out.println("\n Proyectos registrados:");
                for (int i = 0; i < proyectos.size(); i++) {
                    System.out.println(
                            (i + 1) + ". " + proyectos.get(i).obtenerInformacion()
                    );
                }
            }

            case 3 -> {
                System.out.print("Ingrese numero del proyecto a aprobar: ");
                int index = sc.nextInt() - 1;
                sc.nextLine();

                if (index >= 0 && index < proyectos.size()) {
                    Proyecto proyecto = proyectos.get(index);
                    String codigo = proyecto.obtenerInformacion().split(" - ")[0];
                    jefatura.confirmarAprobacionProyecto(codigo);
                    System.out.println("Proyecto aprobado");
                } else {
                    System.out.println("Proyecto invalido");
                }
            }

            case 4 -> {
                System.out.println("\nAvances recibidos por proyecto:");
                if (enviosPorProyecto.isEmpty()) {
                    System.out.println("No existen avances enviados");
                    return;
                }

                for (Map.Entry<Proyecto, List<RegistroAyudantes>> entry : enviosPorProyecto.entrySet()) {
                    System.out.println("Proyecto: " + entry.getKey().obtenerInformacion());
                    for (RegistroAyudantes r : entry.getValue()) {
                        System.out.println("  - " + r.generarResumen());
                    }
                }
            }

            case 5 -> procesarAvance(sc, true);

            case 6 -> procesarAvance(sc, false);
        }
    }

    private static void menuDirector(Scanner sc) {

        System.out.print("Ingrese su identificacion: ");
        String idDirector = sc.nextLine();

        List<Proyecto> proyectosDirector = proyectosPorDirector.get(idDirector);

        if (proyectosDirector == null || proyectosDirector.isEmpty()) {
            System.out.println("No tiene proyectos asignados");
            return;
        }

        List<Proyecto> proyectosAprobados = new ArrayList<>();
        for (Proyecto p : proyectosDirector) {
            if (p.estaAprobado()) {
                proyectosAprobados.add(p);
            }
        }

        if (proyectosAprobados.isEmpty()) {
            System.out.println("No tiene proyectos aprobados");
            return;
        }

        System.out.println("\n--- PROYECTOS APROBADOS ---");
        for (int i = 0; i < proyectosAprobados.size(); i++) {
            System.out.println((i + 1) + ". " + proyectosAprobados.get(i).obtenerInformacion());
        }

        System.out.print("Seleccione un proyecto: ");
        int index = sc.nextInt() - 1;
        sc.nextLine();

        if (index < 0 || index >= proyectosAprobados.size()) {
            System.out.println("Seleccion invalida");
            return;
        }

        Proyecto proyecto = proyectosAprobados.get(index);
        boolean tieneAyudantes = ayudantesPorProyecto.getOrDefault(proyecto, 0) > 0;

        System.out.println("\n--- MENU DIRECTOR ---");
        System.out.println("Director: " + nombreDirectorPorId.get(idDirector));
        System.out.println("1. Subir avance semestral");
        if (tieneAyudantes) {
            System.out.println("2. Subir reporte de ayudantes");
        }

        System.out.print("Opcion: ");
        int opcion = sc.nextInt();
        sc.nextLine();

        if (opcion == 2 && !tieneAyudantes) {
            System.out.println("Este proyecto no tiene ayudantes presupuestados");
            return;
        }

        RegistroAyudantes registro = new RegistroAyudantes();

        try {
            Field idField = RegistroAyudantes.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(registro, "REG-" + UUID.randomUUID());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (opcion == 2 && tieneAyudantes) {

            int cantidad = ayudantesPorProyecto.get(proyecto);

            for (int i = 1; i <= cantidad; i++) {
                Ayudante ayudante = new Ayudante();

                try {
                    Field nombre = Ayudante.class.getDeclaredField("nombreCompleto");
                    Field identificacion = Ayudante.class.getDeclaredField("identificacion");
                    Field correo = Ayudante.class.getDeclaredField("correoElectronico");
                    Field estado = Ayudante.class.getDeclaredField("estado");

                    nombre.setAccessible(true);
                    identificacion.setAccessible(true);
                    correo.setAccessible(true);
                    estado.setAccessible(true);

                    nombre.set(ayudante, "Ayudante " + i);
                    identificacion.set(ayudante, "AYD-" + i);
                    correo.set(ayudante, "ayudante" + i + "@epn.edu.ec");
                    estado.set(ayudante, "ACTIVO");

                } catch (Exception e) {
                    e.printStackTrace();
                }

                registro.agregarAyudante(ayudante);
            }
        }

        enviosPorProyecto
                .computeIfAbsent(proyecto, k -> new ArrayList<>())
                .add(registro);

        System.out.println("✔ Envio registrado correctamente");
    }


    private static void procesarAvance(Scanner sc, boolean aprobar) {

        if (enviosPorProyecto.isEmpty()) {
            System.out.println("No hay avances pendientes");
            return;
        }

        List<Proyecto> lista = new ArrayList<>(enviosPorProyecto.keySet());

        for (int i = 0; i < lista.size(); i++) {
            System.out.println((i + 1) + ". " + lista.get(i).obtenerInformacion());
        }

        System.out.print("Seleccione proyecto: ");
        int index = sc.nextInt() - 1;
        sc.nextLine();

        if (index < 0 || index >= lista.size()) {
            System.out.println("Seleccion invalida");
            return;
        }

        Proyecto proyecto = lista.get(index);
        List<RegistroAyudantes> registros = enviosPorProyecto.get(proyecto);

        if (registros == null || registros.isEmpty()) {
            System.out.println("No hay registros para este proyecto");
            return;
        }

        registros.remove(0);
        if (registros.isEmpty()) {
            enviosPorProyecto.remove(proyecto);
        }

        System.out.println(aprobar ? " Avance aprobado" : " Avance rechazado");
    }
}
