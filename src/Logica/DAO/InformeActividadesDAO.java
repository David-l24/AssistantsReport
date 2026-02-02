package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.*;
import Logica.Enumeraciones.EstadoReporte; // Importante

import java.sql.*;
import java.util.ArrayList;

public class InformeActividadesDAO {
    private Connection connection;

    public InformeActividadesDAO() throws SQLException {
        this.connection = ConexionBD.conectar();
    }

    public boolean guardar(InformeActividades informe) throws SQLException {
        boolean autoCommitOriginal = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            // 1. Insertar Cabecera
            String sqlInforme = "INSERT INTO informe_actividades (fecha_registro, cedula_personal, id_proyecto, estado) VALUES (?, ?, ?, ?) RETURNING id_informe";

            int idInformeGenerado;
            try (PreparedStatement stmt = connection.prepareStatement(sqlInforme)) {
                stmt.setDate(1, Date.valueOf(informe.getFechaRegistro()));
                stmt.setString(2, informe.getPersonalDeInvestigacion().getCedula());
                stmt.setInt(3, informe.getProyecto().getId());

                // CONVERSIÓN ENUM -> STRING (BDD)
                // Si el estado es null, usamos EN_EDICION por defecto para evitar errores
                String estadoStr = (informe.getEstado() != null) ? informe.getEstado().name() : EstadoReporte.EN_EDICION.name();
                stmt.setString(4, estadoStr);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    idInformeGenerado = rs.getInt(1);
                    informe.setIdInforme(idInformeGenerado);
                } else {
                    throw new SQLException("Error: No se generó ID para el informe.");
                }
            }

            // 2. Insertar Semanas (Sin cambios)
            String sqlSemana = "INSERT INTO semana_actividades (nro_semana, fechas, horas_inicio, horas_salida, actividad_semanal, observaciones) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
            String sqlRelacion = "INSERT INTO informe_semana (id_informe, id_semana) VALUES (?, ?)";

            int numSemanas = informe.getActividadesSemanales().size();

            for (int i = 0; i < numSemanas; i++) {
                int idSemanaGenerada;

                try (PreparedStatement stmtSem = connection.prepareStatement(sqlSemana)) {
                    stmtSem.setInt(1, i + 1);

                    java.util.Date[] utilDates = informe.getFechas()[i];
                    java.sql.Date[] sqlDates = new java.sql.Date[utilDates.length];
                    for(int d=0; d<utilDates.length; d++) {
                        if(utilDates[d] != null) sqlDates[d] = new java.sql.Date(utilDates[d].getTime());
                    }
                    stmtSem.setArray(2, connection.createArrayOf("DATE", sqlDates));
                    stmtSem.setArray(3, connection.createArrayOf("TIME", informe.getHorasInicio()[i]));
                    stmtSem.setArray(4, connection.createArrayOf("TIME", informe.getHorasFin()[i]));
                    stmtSem.setString(5, informe.getActividadesSemanales().get(i));

                    String obs = (informe.getObservacionesSemanales().size() > i) ? informe.getObservacionesSemanales().get(i) : "";
                    stmtSem.setString(6, obs);

                    ResultSet rsSem = stmtSem.executeQuery();
                    if (rsSem.next()) {
                        idSemanaGenerada = rsSem.getInt(1);
                    } else {
                        throw new SQLException("Error al guardar semana " + (i+1));
                    }
                }

                try (PreparedStatement stmtRel = connection.prepareStatement(sqlRelacion)) {
                    stmtRel.setInt(1, idInformeGenerado);
                    stmtRel.setInt(2, idSemanaGenerada);
                    stmtRel.executeUpdate();
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            connection.rollback();
            e.printStackTrace();
            return false;
        } finally {
            connection.setAutoCommit(autoCommitOriginal);
        }
    }

    public InformeActividades obtenerPorId(int idInforme) throws SQLException {
        InformeActividades informe = null;

        String sql = "SELECT i.*, " +
                "p.cedula, p.nombres, p.apellidos, p.tipo, " +
                "s.fechas, s.horas_inicio, s.horas_salida, s.actividad_semanal, s.observaciones " +
                "FROM informe_actividades i " +
                "JOIN personaldeinvestigacion p ON i.cedula_personal = p.cedula " +
                "JOIN informe_semana iso ON i.id_informe = iso.id_informe " +
                "JOIN semana_actividades s ON iso.id_semana = s.id " +
                "WHERE i.id_informe = ? ORDER BY s.nro_semana ASC";

        try (PreparedStatement stmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            stmt.setInt(1, idInforme);
            ResultSet rs = stmt.executeQuery();

            int numSemanas = 0;
            if (rs.last()) {
                numSemanas = rs.getRow();
                rs.beforeFirst();
            }

            if (numSemanas > 0) {
                informe = new InformeActividades(numSemanas);
                int i = 0;

                while (rs.next()) {
                    if (i == 0) {
                        informe.setIdInforme(rs.getInt("id_informe"));
                        informe.setFechaRegistro(rs.getDate("fecha_registro").toLocalDate());

                        // CONVERSIÓN STRING (BDD) -> ENUM
                        // Usamos tu método estático que ya maneja excepciones y valores por defecto
                        String estadoStr = rs.getString("estado");
                        informe.setEstado(EstadoReporte.fromString(estadoStr));

                        // Factory de Personal
                        String tipoPersonal = rs.getString("tipo");
                        PersonalDeInvestigacion personal;

                        if ("Ayudante".equalsIgnoreCase(tipoPersonal)) {
                            personal = new Ayudante();
                        } else if ("Asistente".equalsIgnoreCase(tipoPersonal)) {
                            personal = new Asistente();
                        } else if ("Tecnico".equalsIgnoreCase(tipoPersonal)) {
                            personal = new Tecnico();
                        } else {
                            personal = new Tecnico();
                        }

                        personal.setCedula(rs.getString("cedula"));
                        personal.setNombres(rs.getString("nombres"));
                        personal.setApellidos(rs.getString("apellidos"));

                        informe.setPersonalDeInvestigacion(personal);

                        Proyecto proy = new Proyecto() { @Override public String getTipoProyecto() { return ""; }};
                        proy.setIdProyecto(rs.getInt("id_proyecto"));
                        informe.setProyecto(proy);
                    }

                    informe.getActividadesSemanales().add(rs.getString("actividad_semanal"));
                    informe.getObservacionesSemanales().add(rs.getString("observaciones"));

                    Array sqlFechas = rs.getArray("fechas");
                    Array sqlInicio = rs.getArray("horas_inicio");
                    Array sqlFin = rs.getArray("horas_salida");

                    if (sqlFechas != null) {
                        java.sql.Date[] dbDates = (java.sql.Date[]) sqlFechas.getArray();
                        informe.getFechas()[i] = new java.util.Date[dbDates.length];
                        System.arraycopy(dbDates, 0, informe.getFechas()[i], 0, dbDates.length);
                    }
                    if (sqlInicio != null) informe.getHorasInicio()[i] = (Time[]) sqlInicio.getArray();
                    if (sqlFin != null) informe.getHorasFin()[i] = (Time[]) sqlFin.getArray();

                    i++;
                }
            }
        }
        return informe;
    }
}