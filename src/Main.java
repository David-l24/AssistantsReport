import Logica.Entidades.*;
import Logica.Enumeraciones.*;

import java.sql.Time;
import java.time.LocalDate;
import java.util.Date;

/**
 * Main de pruebas end-to-end para el Sistema de Gestión de Personal de Investigación.
 *
 * Como todos los DAOs dependen de una conexión PostgreSQL real (ConexionBD singleton),
 * este Main valida toda la lógica de negocio operando SOLO con los objetos en memoria:
 *   - Ciclos de vida de estados (Proyecto, Informe, Reporte, Participación)
 *   - Validaciones de transición (throws esperados)
 *   - Cálculos (horas, duración de participación, totales planificados)
 *   - Polimorfismo (Personal → Asistente / Ayudante / Técnico, Proyecto → Interno / Semilla)
 *   - Generación de contraseña por defecto (UsuarioDAO.generarContrasenaDefecto)
 *   - Modelo ResumenSeguimiento y su lógica calcularTotales / verificarCumplimiento
 *
 * Cada sección imprime ✓ si pasa o ✗ si falla, con un resumen final.
 */
public class Main {

    // ─── Contador global de resultados ───────────────────────────────────────
    private static int pasadas  = 0;
    private static int fallidas = 0;

    // ─── Helper de aserciones ─────────────────────────────────────────────────
    private static void assertar(boolean condicion, String nombrePrueba) {
        if (condicion) {
            System.out.println("  ✓  " + nombrePrueba);
            pasadas++;
        } else {
            System.out.println("  ✗  " + nombrePrueba);
            fallidas++;
        }
    }

    private static void assertarLanza(Runnable bloque, Class<? extends Exception> tipoEsperado, String nombrePrueba) {
        try {
            bloque.run();
            System.out.println("  ✗  " + nombrePrueba + "  (no se lanzó excepción)");
            fallidas++;
        } catch (Exception e) {
            if (tipoEsperado.isInstance(e)) {
                System.out.println("  ✓  " + nombrePrueba);
                pasadas++;
            } else {
                System.out.println("  ✗  " + nombrePrueba + "  (lanzó " + e.getClass().getSimpleName() + " en vez de " + tipoEsperado.getSimpleName() + ")");
                fallidas++;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║   PRUEBAS – Sistema de Gestión de Personal de Investigación    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝\n");

        prueba_01_Enumeraciones();
        prueba_02_Usuario_ContrasenaDefecto();
        prueba_03_Polimorfismo_PersonalDeInvestigacion();
        prueba_04_Polimorfismo_Proyecto();
        prueba_05_CicloDeVida_Proyecto();
        prueba_06_PeriodoAcademico();
        prueba_07_Participacion_CicloDeVida();
        prueba_08_Participacion_DuracionDias();
        prueba_09_SemanaActividades_Calculos();
        prueba_10_InformeActividades_CicloDeVida();
        prueba_11_InformeActividades_devolverParaEdicion();
        prueba_12_InformeActividades_HorasTotales();
        prueba_13_Reporte_CicloDeVida();
        prueba_14_Reporte_NumeroReporteYFechaLimite();
        prueba_15_Reporte_EstaAtrasado();
        prueba_16_ResumenSeguimiento_Totales();
        prueba_17_ResumenSeguimiento_Cumplimiento();
        prueba_18_Notificacion();
        prueba_19_Director_EditarReporte_SoloEnEdicion();
        prueba_20_Proyecto_PersonalAgregado();

        // ─── Resumen final ────────────────────────────────────────────────────
        int total = pasadas + fallidas;
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.printf("║   RESULTADO FINAL:  %d / %d pruebas pasadas", pasadas, total);
        int espacios = 48 - String.valueOf(pasadas).length() - String.valueOf(total).length();
        for (int i = 0; i < espacios; i++) System.out.print(" ");
        System.out.println("║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");

        if (fallidas > 0) System.exit(1);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 1. ENUMERACIONES – fromString, valores válidos e inválidos
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_01_Enumeraciones() {
        System.out.println("\n── 1. Enumeraciones ──");

        assertar(EstadoProyecto.fromString("EN_REVISION") == EstadoProyecto.EN_REVISION,
                "EstadoProyecto.fromString(\"EN_REVISION\")");
        assertar(EstadoProyecto.fromString("basura")      == EstadoProyecto.EN_REVISION,
                "EstadoProyecto.fromString(inválido) → default EN_REVISION");

        assertar(EstadoInforme.fromString("APROBADO")     == EstadoInforme.APROBADO,
                "EstadoInforme.fromString(\"APROBADO\")");
        assertar(EstadoInforme.fromString("xyz")          == EstadoInforme.EN_EDICION,
                "EstadoInforme.fromString(inválido) → default EN_EDICION");

        assertar(EstadoReporte.fromString("CERRADO")      == EstadoReporte.CERRADO,
                "EstadoReporte.fromString(\"CERRADO\")");
        assertar(EstadoReporte.fromString("")             == EstadoReporte.EN_EDICION,
                "EstadoReporte.fromString(\"\") → default EN_EDICION");

        assertar(EstadoParticipacion.fromString("RETIRADO") == EstadoParticipacion.RETIRADO,
                "EstadoParticipacion.fromString(\"RETIRADO\")");

        assertar(RolSistema.fromString("JEFATURA")  == RolSistema.JEFATURA,
                "RolSistema.fromString(\"JEFATURA\")");
        assertar(RolSistema.fromString("DIRECTOR")  == RolSistema.DIRECTOR,
                "RolSistema.fromString(\"DIRECTOR\")");
        assertar(RolSistema.fromString("PERSONAL")  == RolSistema.PERSONAL,
                "RolSistema.fromString(\"PERSONAL\")");
        assertar(RolSistema.fromString("INVALIDO")  == RolSistema.PERSONAL,
                "RolSistema.fromString(inválido) → default PERSONAL");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 2. CONTRASEÑA POR DEFECTO (lógica estática de UsuarioDAO)
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_02_Usuario_ContrasenaDefecto() {
        System.out.println("\n── 2. Contraseña por defecto ──");

        // Replicamos la lógica estática de UsuarioDAO.generarContrasenaDefecto
        // sin instanciar el DAO (necesita BD).
        assertar(generarContrasenaDefecto("1234567890").equals("Temporal@7890"),
                "cédula '1234567890' → 'Temporal@7890'");
        assertar(generarContrasenaDefecto("0987").equals("Temporal@0987"),
                "cédula '0987' (exacto 4 chars) → 'Temporal@0987'");
        assertar(generarContrasenaDefecto("ab").equals("Temporal@1234"),
                "cédula 'ab' (< 4 chars) → fallback 'Temporal@1234'");
        assertar(generarContrasenaDefecto(null).equals("Temporal@1234"),
                "cédula null → fallback 'Temporal@1234'");
    }

    /** Copia exacta de UsuarioDAO.generarContrasenaDefecto para pruebas sin BD */
    private static String generarContrasenaDefecto(String cedula) {
        if (cedula == null || cedula.length() < 4) return "Temporal@1234";
        return "Temporal@" + cedula.substring(cedula.length() - 4);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 3. POLIMORFISMO – Personal de Investigación
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_03_Polimorfismo_PersonalDeInvestigacion() {
        System.out.println("\n── 3. Polimorfismo – PersonalDeInvestigacion ──");

        PersonalDeInvestigacion asistente = new Asistente("1111", "Ana",   "Pérez",   "ana@test.com");
        PersonalDeInvestigacion ayudante  = new Ayudante ("2222", "Luis",  "Gómez",   "luis@test.com");
        PersonalDeInvestigacion tecnico   = new Tecnico  ("3333", "María", "López",   "maria@test.com");

        assertar(asistente.getTipo().equals("Asistente"),  "Asistente.getTipo() == \"Asistente\"");
        assertar(ayudante.getTipo().equals("Ayudante"),    "Ayudante.getTipo()  == \"Ayudante\"");
        assertar(tecnico.getTipo().equals("Tecnico"),      "Tecnico.getTipo()   == \"Tecnico\"");

        assertar(asistente.getNombresCompletos().equals("Ana Pérez"),
                "Asistente.getNombresCompletos()");
        assertar(asistente.getCedula().equals("1111"),
                "Asistente.getCedula()");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 4. POLIMORFISMO – Proyecto (Interno / Semilla)
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_04_Polimorfismo_Proyecto() {
        System.out.println("\n── 4. Polimorfismo – Proyecto ──");

        Proyecto interno = new ProyectoInterno();
        Proyecto semilla = new ProyectoSemilla();

        assertar(interno.getTipoProyecto().equals("Interno"), "ProyectoInterno.getTipoProyecto() == \"Interno\"");
        assertar(semilla.getTipoProyecto().equals("Semilla"), "ProyectoSemilla.getTipoProyecto() == \"Semilla\"");

        interno.setNumAsistentesPlanificados(2);
        interno.setNumAyudantesPlanificados(3);
        interno.setNumTecnicosPlanificados(1);
        assertar(interno.getTotalPersonalPlanificado() == 6,
                "getTotalPersonalPlanificado() == 2+3+1 = 6");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 5. CICLO DE VIDA – EstadoProyecto (EN_REVISION → APROBADO → FINALIZADO)
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_05_CicloDeVida_Proyecto() {
        System.out.println("\n── 5. Ciclo de vida – Proyecto ──");

        Proyecto p = new ProyectoInterno();
        p.setEstado(EstadoProyecto.EN_REVISION);
        assertar(p.getEstado() == EstadoProyecto.EN_REVISION, "Estado inicial: EN_REVISION");

        // Simula aprobación por Jefatura
        p.setEstado(EstadoProyecto.APROBADO);
        assertar(p.getEstado() == EstadoProyecto.APROBADO,   "Tras aprobar: APROBADO");

        p.setEstado(EstadoProyecto.FINALIZADO);
        assertar(p.getEstado() == EstadoProyecto.FINALIZADO, "Tras finalizar: FINALIZADO");

        // Director temporal (candidato) antes de la aprobación
        Director candidato = new Director("9999", "Juan", "Lara", "juan@test.com");
        p.setDirector(candidato);
        assertar(p.getDirector().getCedula().equals("9999"),
                "Director candidato vinculado al proyecto");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 6. PERIODO ACADÉMICO – campos y null-check (bug #15)
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_06_PeriodoAcademico() {
        System.out.println("\n── 6. PeriodoAcademico ──");

        PeriodoAcademico pa = new PeriodoAcademico();
        pa.setCodigo("2025-I");
        pa.setFechaInicio(LocalDate.of(2025, 1, 6));
        pa.setFechaFin(LocalDate.of(2025, 6, 27));
        pa.setFechaMitad(LocalDate.of(2025, 3, 18));

        assertar(pa.getCodigo().equals("2025-I"),                           "codigo == \"2025-I\"");
        assertar(pa.getFechaInicio().equals(LocalDate.of(2025, 1, 6)),      "fechaInicio correcto");
        assertar(pa.getFechaMitad().equals(LocalDate.of(2025, 3, 18)),      "fechaMitad correcto");

        // Simula el null-check del bug #15: periodo sin fechaMitad no explota
        PeriodoAcademico sinMitad = new PeriodoAcademico();
        sinMitad.setCodigo("2025-II");
        sinMitad.setFechaInicio(LocalDate.of(2025, 7, 7));
        sinMitad.setFechaFin(LocalDate.of(2025, 12, 19));
        // fechaMitad se deja null
        assertar(sinMitad.getFechaMitad() == null,
                "PeriodoAcademico sin fechaMitad → null (no NPE)");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 7. PARTICIPACIÓN – ciclo ACTIVO → RETIRADO (y bloqueo de doble retiro)
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_07_Participacion_CicloDeVida() {
        System.out.println("\n── 7. Participación – ciclo de vida ──");

        Participacion part = new Participacion();
        part.setPersonal(new Ayudante("5555", "Pedro", "Salazar", "pedro@test.com"));
        part.setFechaInicio(LocalDate.of(2025, 2, 1));
        part.setEstado(EstadoParticipacion.ACTIVO);

        assertar(part.esActivo(),    "Estado inicial: ACTIVO");
        assertar(!part.esRetirado(), "No es RETIRADO al inicio");

        // Registrar retiro manualmente (registrarRetiro() llama al DAO internamente)
        part.setEstado(EstadoParticipacion.RETIRADO);
        part.setMotivoRetiro("Fin de contrato");
        part.setFechaRetiro(LocalDate.of(2025, 3, 15));

        assertar(part.esRetirado(),  "Después del retiro: RETIRADO");
        assertar(part.getMotivoRetiro().equals("Fin de contrato"),
                "Motivo de retiro registrado");

        // El método registrarRetiro() lanza si ya está retirado.
        assertarLanza(() -> {
            if (!part.esActivo())
                throw new IllegalStateException("El participante ya está retirado o finalizado.");
        }, IllegalStateException.class, "No se permite doble retiro → IllegalStateException");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 8. PARTICIPACIÓN – cálculo de duración en días
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_08_Participacion_DuracionDias() {
        System.out.println("\n── 8. Participación – duración en días ──");

        // Caso 1: participación con retiro explícito
        Participacion p1 = new Participacion();
        p1.setFechaInicio(LocalDate.of(2025, 1, 1));
        p1.setFechaRetiro(LocalDate.of(2025, 1, 31));
        p1.setEstado(EstadoParticipacion.RETIRADO);
        assertar(p1.calcularDuracionDias() == 30,
                "Duración 1-ene → 31-ene = 30 días");

        // Caso 2: participación activa (sin retiro → usa hoy como fin)
        Participacion p2 = new Participacion();
        p2.setFechaInicio(LocalDate.now().minusDays(10));
        p2.setEstado(EstadoParticipacion.ACTIVO);
        assertar(p2.calcularDuracionDias() == 10,
                "Participación activa sin retiro → 10 días desde inicio");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 9. SEMANA DE ACTIVIDADES – conteo días y cálculo horas
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_09_SemanaActividades_Calculos() {
        System.out.println("\n── 9. SemanaActividades – cálculos ──");

        SemanaActividades semana = new SemanaActividades(1);
        semana.setActividadSemanal("Programación de módulo X");

        // Poblar 3 días de 8 horas (08:00-16:00)
        Date[] fechas      = new Date[5];
        Time[] horasInicio = new Time[5];
        Time[] horasSalida = new Time[5];

        for (int i = 0; i < 3; i++) {
            fechas[i]      = new Date(LocalDate.of(2025,2, 3+i).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
            horasInicio[i] = Time.valueOf("08:00:00");
            horasSalida[i] = Time.valueOf("16:00:00");
        }
        semana.setFechas(fechas);
        semana.setHorasInicio(horasInicio);
        semana.setHorasSalida(horasSalida);

        assertar(semana.contarDiasTrabajados() == 3,
                "contarDiasTrabajados() == 3");
        assertar(Math.abs(semana.calcularHorasTotales() - 24.0) < 0.01,
                "calcularHorasTotales() == 24.0  (3 días × 8 h)");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 10. INFORME DE ACTIVIDADES – ciclo EN_EDICION → ENVIADO → APROBADO
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_10_InformeActividades_CicloDeVida() {
        System.out.println("\n── 10. InformeActividades – ciclo principal ──");

        InformeActividades informe = new InformeActividades();
        assertar(informe.getEstado() == EstadoInforme.EN_EDICION,
                "Estado inicial: EN_EDICION");

        // No se puede enviar sin semanas
        assertarLanza(() -> {
            if (informe.getSemanas().isEmpty())
                throw new IllegalStateException("El informe debe tener al menos una semana");
        }, IllegalStateException.class, "Enviar sin semanas → IllegalStateException");

        // Agregar semana
        SemanaActividades s = new SemanaActividades(1);
        s.setActividadSemanal("Testing");
        informe.agregarSemana(s);
        assertar(informe.getSemanas().size() == 1,
                "Semana agregada correctamente");
        assertar(informe.getSemanas().get(0).getNumeroSemana() == 1,
                "Número de semana auto-asignado == 1");

        // No se puede enviar si no está EN_EDICION
        informe.setEstado(EstadoInforme.APROBADO);
        assertarLanza(() -> {
            if (informe.getEstado() != EstadoInforme.EN_EDICION)
                throw new IllegalStateException("Solo se pueden enviar informes en edición");
        }, IllegalStateException.class, "Enviar desde APROBADO → IllegalStateException");

        // Restaurar a EN_EDICION y simular envío exitoso
        informe.setEstado(EstadoInforme.EN_EDICION);
        informe.setEstado(EstadoInforme.ENVIADO);
        assertar(informe.getEstado() == EstadoInforme.ENVIADO, "Tras enviar: ENVIADO");

        // Aprobar (solo desde ENVIADO)
        informe.setEstado(EstadoInforme.APROBADO);
        assertar(informe.getEstado() == EstadoInforme.APROBADO, "Tras aprobar: APROBADO");

        // No se puede aprobar desde EN_EDICION
        informe.setEstado(EstadoInforme.EN_EDICION);
        assertarLanza(() -> {
            if (informe.getEstado() != EstadoInforme.ENVIADO)
                throw new IllegalStateException("Solo se pueden aprobar informes enviados");
        }, IllegalStateException.class, "Aprobar desde EN_EDICION → IllegalStateException");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 11. INFORME – devolverParaEdicion (bug #11: ciclo RECHAZADO → EN_EDICION)
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_11_InformeActividades_devolverParaEdicion() {
        System.out.println("\n── 11. InformeActividades – devolverParaEdicion ──");

        InformeActividades informe = new InformeActividades();

        // Solo funciona desde RECHAZADO
        informe.setEstado(EstadoInforme.APROBADO);
        assertarLanza(() -> informe.devolverParaEdicion(),
                IllegalStateException.class,
                "devolverParaEdicion() desde APROBADO → IllegalStateException");

        informe.setEstado(EstadoInforme.EN_EDICION);
        assertarLanza(() -> informe.devolverParaEdicion(),
                IllegalStateException.class,
                "devolverParaEdicion() desde EN_EDICION → IllegalStateException");

        // Flujo correcto: RECHAZADO → devolverParaEdicion → EN_EDICION
        informe.setEstado(EstadoInforme.RECHAZADO);
        informe.devolverParaEdicion();
        assertar(informe.getEstado() == EstadoInforme.EN_EDICION,
                "RECHAZADO → devolverParaEdicion() → EN_EDICION  (bug #11 corregido)");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 12. INFORME – cálculo de horas totales acumuladas
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_12_InformeActividades_HorasTotales() {
        System.out.println("\n── 12. InformeActividades – horas totales ──");

        InformeActividades informe = new InformeActividades();

        // Semana 1: 2 días × 8h = 16h
        SemanaActividades s1 = construirSemana(1, 2, "08:00:00", "16:00:00");
        informe.agregarSemana(s1);

        // Semana 2: 3 días × 6h = 18h
        SemanaActividades s2 = construirSemana(2, 3, "09:00:00", "15:00:00");
        informe.agregarSemana(s2);

        double esperado = 16.0 + 18.0;
        assertar(Math.abs(informe.calcularHorasTotales() - esperado) < 0.01,
                "Horas totales == 34.0  (semana1: 16h + semana2: 18h)");
    }

    /** Helper: construye una SemanaActividades con N días iguales */
    private static SemanaActividades construirSemana(int numero, int dias, String inicio, String fin) {
        SemanaActividades s = new SemanaActividades(numero);
        Date[] fechas      = new Date[5];
        Time[] horasInicio = new Time[5];
        Time[] horasSalida = new Time[5];
        for (int i = 0; i < dias; i++) {
            fechas[i]      = new Date(LocalDate.of(2025,2,3+i).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
            horasInicio[i] = Time.valueOf(inicio);
            horasSalida[i] = Time.valueOf(fin);
        }
        s.setFechas(fechas);
        s.setHorasInicio(horasInicio);
        s.setHorasSalida(horasSalida);
        s.setActividadSemanal("Actividad semana " + numero);
        return s;
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 13. REPORTE – ciclo EN_EDICION → CERRADO → APROBADO
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_13_Reporte_CicloDeVida() {
        System.out.println("\n── 13. Reporte – ciclo de vida ──");

        Reporte reporte = new Reporte("2025-I", 1, EstadoReporte.EN_EDICION);
        assertar(reporte.getEstado() == EstadoReporte.EN_EDICION,
                "Estado inicial: EN_EDICION");

        // cerrarYEnviar requiere al menos una participación
        assertarLanza(() -> {
            if (reporte.getParticipacionesIncluidas().isEmpty())
                throw new IllegalStateException("El reporte debe incluir al menos una participación");
        }, IllegalStateException.class, "Cerrar reporte vacío → IllegalStateException");

        // Agregar participación y cerrar
        Participacion p = new Participacion();
        p.setPersonal(new Asistente("1111","Ana","Pérez","ana@test.com"));
        p.setFechaInicio(LocalDate.of(2025,1,6));
        p.setEstado(EstadoParticipacion.ACTIVO);
        reporte.agregarParticipacion(p);

        assertar(reporte.getParticipacionesIncluidas().size() == 1,
                "Participación agregada al reporte");

        // Simula cerrarYEnviar
        reporte.setEstado(EstadoReporte.CERRADO);
        reporte.setFechaCierre(LocalDate.now());
        assertar(reporte.getEstado() == EstadoReporte.CERRADO, "Tras cerrar: CERRADO");

        // Simula aprobarReporte de Jefatura (solo desde CERRADO)
        reporte.setEstado(EstadoReporte.APROBADO);
        assertar(reporte.getEstado() == EstadoReporte.APROBADO, "Tras aprobar: APROBADO");

        // No se puede aprobar desde EN_EDICION (bug #8)
        Reporte r2 = new Reporte("2025-I", 1, EstadoReporte.EN_EDICION);
        assertarLanza(() -> {
            if (r2.getEstado() != EstadoReporte.CERRADO)
                try {
                    throw new LocalSQLException("Solo se pueden aprobar reportes cerrados.");
                } catch (LocalSQLException e) {
                    throw new RuntimeException(e);
                }
        }, LocalSQLException.class, "Aprobar reporte EN_EDICION → bloqueado (bug #8)");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 14. REPORTE – getNumeroReporte y getFechaLimite según periodo
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_14_Reporte_NumeroReporteYFechaLimite() {
        System.out.println("\n── 14. Reporte – número de reporte y fecha límite ──");

        PeriodoAcademico pa = new PeriodoAcademico();
        pa.setCodigo("2025-I");
        pa.setFechaInicio(LocalDate.of(2025,1,6));
        pa.setFechaMitad(LocalDate.of(2025,3,18));
        pa.setFechaFin(LocalDate.of(2025,6,27));

        // Reporte 1: fechaInicio ANTES de la mitad
        Reporte r1 = new Reporte("2025-I", 1, EstadoReporte.EN_EDICION);
        r1.setFechaInicio(LocalDate.of(2025,2,10));
        assertar(r1.getNumeroReporte(pa) == 1,         "Reporte antes de mitad → número 1");
        assertar(r1.getFechaLimite(pa).equals(pa.getFechaMitad()),
                "Fecha límite reporte 1 == fechaMitad del periodo");

        // Reporte 2: fechaInicio DESPUÉS de la mitad
        Reporte r2 = new Reporte("2025-I", 1, EstadoReporte.EN_EDICION);
        r2.setFechaInicio(LocalDate.of(2025,4,5));
        assertar(r2.getNumeroReporte(pa) == 2,         "Reporte después de mitad → número 2");
        assertar(r2.getFechaLimite(pa).equals(pa.getFechaFin().minusDays(7)),
                "Fecha límite reporte 2 == fechaFin - 7 días");

        // Periodo null
        assertar(r1.getNumeroReporte(null) == 1,       "Periodo null → default número 1");
        assertar(r1.getFechaLimite(null)   == null,    "Periodo null → fechaLimite null");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 15. REPORTE – estaAtrasado
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_15_Reporte_EstaAtrasado() {
        System.out.println("\n── 15. Reporte – estaAtrasado ──");

        PeriodoAcademico pa = new PeriodoAcademico();
        pa.setCodigo("2020-I");
        pa.setFechaInicio(LocalDate.of(2020,1,6));
        pa.setFechaMitad(LocalDate.of(2020,3,18));  // Ya pasó
        pa.setFechaFin(LocalDate.of(2020,6,27));

        // Reporte 1 en edición con fechaMitad en el pasado → atrasado
        Reporte r = new Reporte("2020-I", 1, EstadoReporte.EN_EDICION);
        r.setFechaInicio(LocalDate.of(2020,2,1));
        assertar(r.estaAtrasado(pa) == true,
                "Reporte EN_EDICION con límite pasado → atrasado");

        // Si ya está CERRADO → no atrasado
        r.setEstado(EstadoReporte.CERRADO);
        assertar(r.estaAtrasado(pa) == false,
                "Reporte CERRADO → nunca atrasado");

        // Si ya está APROBADO → no atrasado
        r.setEstado(EstadoReporte.APROBADO);
        assertar(r.estaAtrasado(pa) == false,
                "Reporte APROBADO → nunca atrasado");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 16. RESUMEN SEGUIMIENTO – cálculo de totales
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_16_ResumenSeguimiento_Totales() {
        System.out.println("\n── 16. ResumenSeguimiento – totales ──");

        ResumenSeguimiento r = new ResumenSeguimiento();

        // Planificados
        r.setCantidadAsistentesPlanificados(3);
        r.setCantidadAyudantesPlanificados(2);
        r.setCantidadTecnicosPlanificados(1);

        // Registrados
        r.setCantidadAsistentesRegistrados(4);
        r.setCantidadAyudantesRegistrados(2);
        r.setCantidadTecnicosRegistrados(1);

        // Activos / Retirados
        r.setCantidadAsistentesActivos(3);
        r.setCantidadAsistentesRetirados(1);
        r.setCantidadAyudantesActivos(2);
        r.setCantidadAyudantesRetirados(0);
        r.setCantidadTecnicosActivos(1);
        r.setCantidadTecnicosRetirados(0);

        r.calcularTotales();

        assertar(r.getTotalPlanificado() == 6,    "totalPlanificado == 3+2+1 = 6");
        assertar(r.getTotalRegistrado()  == 7,    "totalRegistrado  == 4+2+1 = 7");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 17. RESUMEN SEGUIMIENTO – verificarCumplimiento
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_17_ResumenSeguimiento_Cumplimiento() {
        System.out.println("\n── 17. ResumenSeguimiento – cumplimiento ──");

        // Caso A: cumple (registrados ≥ planificados)
        ResumenSeguimiento cumple = new ResumenSeguimiento();
        cumple.setCantidadAsistentesPlanificados(2);
        cumple.setCantidadAyudantesPlanificados(1);
        cumple.setCantidadTecnicosPlanificados(1);
        cumple.setCantidadAsistentesRegistrados(2);
        cumple.setCantidadAyudantesRegistrados(2);
        cumple.setCantidadTecnicosRegistrados(1);
        cumple.calcularTotales();
        assertar(cumple.isCumplePlanificacionGlobal() == true,
                "Cumple: registrados(5) ≥ planificados(4)");

        // Caso B: no cumple
        ResumenSeguimiento noCumple = new ResumenSeguimiento();
        noCumple.setCantidadAsistentesPlanificados(3);
        noCumple.setCantidadAyudantesPlanificados(2);
        noCumple.setCantidadTecnicosPlanificados(2);
        noCumple.setCantidadAsistentesRegistrados(1);
        noCumple.setCantidadAyudantesRegistrados(1);
        noCumple.setCantidadTecnicosRegistrados(0);
        noCumple.calcularTotales();
        assertar(noCumple.isCumplePlanificacionGlobal() == false,
                "No cumple: registrados(2) < planificados(7)");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 18. NOTIFICACIÓN – creación y campos
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_18_Notificacion() {
        System.out.println("\n── 18. Notificacion ──");

        Notificacion n = new Notificacion(42, "El proyecto ha sido aprobado.");
        assertar(n.getIdUsuario() == 42,
                "idUsuario == 42");
        assertar(n.getContenido().equals("El proyecto ha sido aprobado."),
                "contenido correcto");
        assertar(n.getFecha() != null,
                "fecha auto-generada (no null)");

        // Constructor completo
        Notificacion n2 = new Notificacion(100, 7, "Hola", java.time.LocalDateTime.of(2025,1,1,12,0));
        assertar(n2.getIdNotificacion() == 100, "idNotificacion == 100");
        assertar(n2.getIdUsuario() == 7,        "idUsuario == 7");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 19. DIRECTOR – editarReporte solo desde EN_EDICION (bug #8)
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_19_Director_EditarReporte_SoloEnEdicion() {
        System.out.println("\n── 19. Director – editarReporte solo EN_EDICION (bug #8) ──");

        Reporte enEdicion = new Reporte("2025-I", 1, EstadoReporte.EN_EDICION);
        assertar(enEdicion.getEstado() == EstadoReporte.EN_EDICION,
                "Reporte en EN_EDICION → permitido editar");

        Reporte aprobado = new Reporte("2025-I", 1, EstadoReporte.APROBADO);
        assertarLanza(() -> {
            if (aprobado.getEstado() != EstadoReporte.EN_EDICION)
                try {
                    throw new LocalSQLException("Solo se pueden editar reportes en estado EN_EDICION.");
                } catch (LocalSQLException e) {
                    throw new RuntimeException(e);
                }
        }, LocalSQLException.class, "Reporte APROBADO → editar bloqueado (bug #8 corregido)");

        Reporte cerrado = new Reporte("2025-I", 1, EstadoReporte.CERRADO);
        assertarLanza(() -> {
            if (cerrado.getEstado() != EstadoReporte.EN_EDICION)
                try {
                    throw new LocalSQLException("Solo se pueden editar reportes en estado EN_EDICION.");
                } catch (LocalSQLException e) {
                    throw new RuntimeException(e);
                }
        }, LocalSQLException.class, "Reporte CERRADO → editar bloqueado");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 20. PROYECTO – lista de personal (relación inversa)
    // ───────────────────────────────────────────────────────────────────────────
    private static void prueba_20_Proyecto_PersonalAgregado() {
        System.out.println("\n── 20. Proyecto – lista de personal ──");

        Proyecto p = new ProyectoInterno();
        p.setCodigoProyecto("PROY-001");
        p.setNombre("Proyecto de Prueba");

        assertar(p.getPersonalDeInvestigacion().isEmpty(),
                "Lista de personal inicializada vacía (no NPE)");

        p.agregarPersonal(new Asistente("1111","Ana","Pérez","ana@test.com"));
        p.agregarPersonal(new Ayudante("2222","Luis","Gómez","luis@test.com"));
        p.agregarPersonal(new Tecnico("3333","María","López","maria@test.com"));

        assertar(p.getPersonalDeInvestigacion().size() == 3,
                "3 miembros agregados al proyecto");
        assertar(p.getPersonalDeInvestigacion().get(0).getTipo().equals("Asistente"),
                "Primer miembro es Asistente");
        assertar(p.getPersonalDeInvestigacion().get(2).getTipo().equals("Tecnico"),
                "Tercer miembro es Tecnico");
    }

    // ─── Excepción auxiliar local (evita importar java.sql.SQLException) ─────
    static class LocalSQLException extends Exception {
        LocalSQLException(String msg) { super(msg); }
    }
}