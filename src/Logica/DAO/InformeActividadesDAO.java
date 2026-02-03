package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.*;
import Logica.Enumeraciones.EstadoInforme;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InformeActividadesDAO {
    private Connection connection;

    public InformeActividadesDAO() throws SQLException {
        this.connection = ConexionBD.conectar();
    }

    /**
     * Guarda un informe completo con sus semanas
     */
    public boolean guardar(InformeActividades informe) throws SQLException {
        boolean autoCommitOriginal = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            // 1. Insertar cabecera del informe
            String sqlInforme = "INSERT INTO informe_actividades " +
                    "(fecha_registro, cedula_personal, id_proyecto, estado) " +
                    "VALUES (?, ?, ?, ?) RETURNING id_informe";

            int idInformeGenerado;
            try (PreparedStatement stmt = connection.prepareStatement(sqlInforme)) {
                stmt.setDate(1, Date.valueOf(informe.getFechaRegistro()));
                stmt.setString(2, informe.getPersonalDeInvestigacion().getCedula());
                stmt.setInt(3, informe.getProyecto().getId());

                String estadoStr = (informe.getEstado() != null) ?
                        informe.getEstado().name() :
                        EstadoInforme.EN_EDICION.name();
                stmt.setString(4, estadoStr);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    idInformeGenerado = rs.getInt(1);
                    informe.setIdInforme(idInformeGenerado);
                } else {
                    throw new SQLException("Error: No se generó ID para el informe.");
                }
            }

            // 2. Insertar cada semana
            for (SemanaActividades semana : informe.getSemanas()) {
                guardarSemana(semana, idInformeGenerado);
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(autoCommitOriginal);
        }
    }

    /**
     * Guarda una semana individual y la vincula al informe
     */
    private void guardarSemana(SemanaActividades semana, int idInforme) throws SQLException {
        // Insertar semana
        String sqlSemana = "INSERT INTO semana_actividades " +
                "(nro_semana, fechas, horas_inicio, horas_salida, " +
                "actividad_semanal, observaciones) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        int idSemanaGenerada;
        try (PreparedStatement stmt = connection.prepareStatement(sqlSemana)) {
            stmt.setInt(1, semana.getNumeroSemana());

            // Convertir arrays para PostgreSQL
            java.util.Date[] utilDates = semana.getFechas();
            java.sql.Date[] sqlDates = new java.sql.Date[utilDates.length];
            for (int i = 0; i < utilDates.length; i++) {
                if (utilDates[i] != null) {
                    sqlDates[i] = new java.sql.Date(utilDates[i].getTime());
                }
            }

            stmt.setArray(2, connection.createArrayOf("DATE", sqlDates));
            stmt.setArray(3, connection.createArrayOf("TIME", semana.getHorasInicio()));
            stmt.setArray(4, connection.createArrayOf("TIME", semana.getHorasSalida()));
            stmt.setString(5, semana.getActividadSemanal());
            stmt.setString(6, semana.getObservaciones());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                idSemanaGenerada = rs.getInt(1);
                semana.setId(idSemanaGenerada);
            } else {
                throw new SQLException("Error al guardar semana");
            }
        }

        // Crear relación informe-semana
        String sqlRelacion = "INSERT INTO informe_semana (id_informe, id_semana) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sqlRelacion)) {
            stmt.setInt(1, idInforme);
            stmt.setInt(2, idSemanaGenerada);
            stmt.executeUpdate();
        }
    }

    /**
     * Actualiza una semana existente
     */
    private void actualizarSemana(SemanaActividades semana) throws SQLException {
        String sql = "UPDATE semana_actividades SET " +
                "nro_semana = ?, fechas = ?, horas_inicio = ?, horas_salida = ?, " +
                "actividad_semanal = ?, observaciones = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, semana.getNumeroSemana());

            // Convertir arrays para PostgreSQL
            java.util.Date[] utilDates = semana.getFechas();
            java.sql.Date[] sqlDates = new java.sql.Date[utilDates.length];
            for (int i = 0; i < utilDates.length; i++) {
                if (utilDates[i] != null) {
                    sqlDates[i] = new java.sql.Date(utilDates[i].getTime());
                }
            }

            stmt.setArray(2, connection.createArrayOf("DATE", sqlDates));
            stmt.setArray(3, connection.createArrayOf("TIME", semana.getHorasInicio()));
            stmt.setArray(4, connection.createArrayOf("TIME", semana.getHorasSalida()));
            stmt.setString(5, semana.getActividadSemanal());
            stmt.setString(6, semana.getObservaciones());
            stmt.setInt(7, semana.getId());

            stmt.executeUpdate();
        }
    }

    /**
     * Actualiza el informe completo: estado y semanas
     */
    public boolean actualizar(InformeActividades informe) throws SQLException {
        boolean autoCommitOriginal = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            // 1. Actualizar estado del informe
            String sqlInforme = "UPDATE informe_actividades SET estado = ? WHERE id_informe = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sqlInforme)) {
                stmt.setString(1, informe.getEstado().name());
                stmt.setInt(2, informe.getIdInforme());
                stmt.executeUpdate();
            }

            // 2. Obtener IDs de semanas actuales en BD
            List<Integer> semanasExistentesIds = new ArrayList<>();
            String sqlGetSemanas = "SELECT id_semana FROM informe_semana WHERE id_informe = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sqlGetSemanas)) {
                stmt.setInt(1, informe.getIdInforme());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    semanasExistentesIds.add(rs.getInt("id_semana"));
                }
            }

            // 3. Actualizar o insertar cada semana
            List<Integer> semanasActualizadas = new ArrayList<>();
            for (SemanaActividades semana : informe.getSemanas()) {
                if (semana.getId() > 0 && semanasExistentesIds.contains(semana.getId())) {
                    // Actualizar semana existente
                    actualizarSemana(semana);
                    semanasActualizadas.add(semana.getId());
                } else {
                    // Insertar nueva semana
                    guardarSemana(semana, informe.getIdInforme());
                    semanasActualizadas.add(semana.getId());
                }
            }

            // 4. Eliminar semanas que ya no están en el informe
            for (Integer idSemanaExistente : semanasExistentesIds) {
                if (!semanasActualizadas.contains(idSemanaExistente)) {
                    // Eliminar relación
                    String sqlDelRelacion = "DELETE FROM informe_semana WHERE id_informe = ? AND id_semana = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sqlDelRelacion)) {
                        stmt.setInt(1, informe.getIdInforme());
                        stmt.setInt(2, idSemanaExistente);
                        stmt.executeUpdate();
                    }

                    // Eliminar semana
                    String sqlDelSemana = "DELETE FROM semana_actividades WHERE id = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sqlDelSemana)) {
                        stmt.setInt(1, idSemanaExistente);
                        stmt.executeUpdate();
                    }
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(autoCommitOriginal);
        }
    }

    /**
     * Obtiene un informe por ID con todas sus semanas
     */
    public InformeActividades obtenerPorId(int idInforme) throws SQLException {
        InformeActividades informe = null;

        // Primero obtener la cabecera
        String sqlCabecera = "SELECT i.*, " +
                "p.cedula, p.nombres, p.apellidos, p.tipo, p.id_proyecto, p.id_usuario " +
                "FROM informe_actividades i " +
                "JOIN personaldeinvestigacion p ON i.cedula_personal = p.cedula " +
                "WHERE i.id_informe = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sqlCabecera)) {
            stmt.setInt(1, idInforme);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                informe = new InformeActividades();
                informe.setIdInforme(rs.getInt("id_informe"));
                informe.setFechaRegistro(rs.getDate("fecha_registro").toLocalDate());
                informe.setEstado(EstadoInforme.fromString(rs.getString("estado")));

                // Crear personal
                PersonalDeInvestigacion personal = crearPersonalPorTipo(rs.getString("tipo"));
                personal.setIdUsuario(rs.getInt("id_usuario"));
                personal.setCedula(rs.getString("cedula"));
                personal.setNombres(rs.getString("nombres"));
                personal.setApellidos(rs.getString("apellidos"));
                personal.setIdProyecto(rs.getInt("id_proyecto"));
                informe.setPersonalDeInvestigacion(personal);

                // Cargar semanas
                List<SemanaActividades> semanas = cargarSemanasDelInforme(idInforme);
                informe.setSemanas(semanas);
            }
        }

        return informe;
    }

    /**
     * Carga todas las semanas de un informe
     */
    private List<SemanaActividades> cargarSemanasDelInforme(int idInforme) throws SQLException {
        List<SemanaActividades> semanas = new ArrayList<>();

        String sql = "SELECT s.* FROM semana_actividades s " +
                "JOIN informe_semana iso ON s.id = iso.id_semana " +
                "WHERE iso.id_informe = ? ORDER BY s.nro_semana";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idInforme);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SemanaActividades semana = new SemanaActividades();
                semana.setId(rs.getInt("id"));
                semana.setNumeroSemana(rs.getInt("nro_semana"));
                semana.setActividadSemanal(rs.getString("actividad_semanal"));
                semana.setObservaciones(rs.getString("observaciones"));

                // Convertir arrays de PostgreSQL
                Array sqlFechas = rs.getArray("fechas");
                Array sqlInicio = rs.getArray("horas_inicio");
                Array sqlFin = rs.getArray("horas_salida");

                if (sqlFechas != null) {
                    java.sql.Date[] dbDates = (java.sql.Date[]) sqlFechas.getArray();
                    java.util.Date[] utilDates = new java.util.Date[dbDates.length];
                    for (int i = 0; i < dbDates.length; i++) {
                        if (dbDates[i] != null) {
                            utilDates[i] = new java.util.Date(dbDates[i].getTime());
                        }
                    }
                    semana.setFechas(utilDates);
                }

                if (sqlInicio != null) {
                    semana.setHorasInicio((Time[]) sqlInicio.getArray());
                }

                if (sqlFin != null) {
                    semana.setHorasSalida((Time[]) sqlFin.getArray());
                }

                semanas.add(semana);
            }
        }

        return semanas;
    }

    /**
     * Factory para crear instancia correcta según el tipo
     */
    private PersonalDeInvestigacion crearPersonalPorTipo(String tipo) {
        if ("Ayudante".equalsIgnoreCase(tipo)) {
            return new Ayudante();
        } else if ("Asistente".equalsIgnoreCase(tipo)) {
            return new Asistente();
        } else if ("Tecnico".equalsIgnoreCase(tipo)) {
            return new Tecnico();
        } else {
            return new Tecnico(); // Default
        }
    }

    /**
     * Obtiene todos los informes de un personal específico
     */
    public List<InformeActividades> obtenerPorPersonal(String cedula) throws SQLException {
        List<InformeActividades> informes = new ArrayList<>();
        String sql = "SELECT id_informe FROM informe_actividades WHERE cedula_personal = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cedula);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int idInforme = rs.getInt("id_informe");
                InformeActividades informe = obtenerPorId(idInforme);
                if (informe != null) {
                    informes.add(informe);
                }
            }
        }

        return informes;
    }

    /**
     * Obtiene informes pendientes de revisión para un director
     */
    public List<InformeActividades> obtenerPendientesDeRevision(int idProyecto) throws SQLException {
        List<InformeActividades> informes = new ArrayList<>();
        String sql = "SELECT id_informe FROM informe_actividades " +
                "WHERE id_proyecto = ? AND estado = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idProyecto);
            stmt.setString(2, EstadoInforme.ENVIADO.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int idInforme = rs.getInt("id_informe");
                InformeActividades informe = obtenerPorId(idInforme);
                if (informe != null) {
                    informes.add(informe);
                }
            }
        }

        return informes;
    }
}