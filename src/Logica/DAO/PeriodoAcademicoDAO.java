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
    
    private PeriodoAcademico mapResultSet(ResultSet rs) throws SQLException {
        PeriodoAcademico periodo = new PeriodoAcademico();
        periodo.setCodigo(rs.getString("codigo"));
        periodo.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
        periodo.setFechaFin(rs.getDate("fecha_fin").toLocalDate());
        periodo.setFechaMitad(rs.getDate("fecha_mitad").toLocalDate());
        return periodo;
    }
}
