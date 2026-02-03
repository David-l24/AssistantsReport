package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.PeriodoAcademico;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeriodoAcademicoDAO {
    private Connection connection;

    public PeriodoAcademicoDAO() throws SQLException {
        this.connection = ConexionBD.conectar();
    }

    public List<PeriodoAcademico> obtenerTodos() throws SQLException {
        List<PeriodoAcademico> periodos = new ArrayList<>();
        String sql = "SELECT * FROM periodo_academico ORDER BY fecha_inicio DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                periodos.add(mapResultSet(rs));
            }
        }
        return periodos;
    }

    public PeriodoAcademico obtenerPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM periodo_academico WHERE codigo = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null;
    }

    /**
     * Persiste un nuevo periodo académico.
     * fecha_mitad es opcional y puede ser null.
     * @return true si la inserción fue exitosa.
     */
    public boolean guardar(PeriodoAcademico periodo) throws SQLException {
        String sql = "INSERT INTO periodo_academico (codigo, fecha_inicio, fecha_fin, fecha_mitad) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, periodo.getCodigo());
            stmt.setDate(2, Date.valueOf(periodo.getFechaInicio()));
            stmt.setDate(3, Date.valueOf(periodo.getFechaFin()));

            if (periodo.getFechaMitad() != null) {
                stmt.setDate(4, Date.valueOf(periodo.getFechaMitad()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            return stmt.executeUpdate() > 0;
        }
    }

    private PeriodoAcademico mapResultSet(ResultSet rs) throws SQLException {
        PeriodoAcademico periodo = new PeriodoAcademico();
        periodo.setCodigo(rs.getString("codigo"));
        periodo.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
        periodo.setFechaFin(rs.getDate("fecha_fin").toLocalDate());
        Date fechaMitad = rs.getDate("fecha_mitad");
        if (fechaMitad != null) {
            periodo.setFechaMitad(fechaMitad.toLocalDate());
        }
        return periodo;
    }
}