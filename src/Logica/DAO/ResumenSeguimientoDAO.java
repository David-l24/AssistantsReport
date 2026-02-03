package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.ResumenSeguimiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResumenSeguimientoDAO {

    private Connection connection;

    public ResumenSeguimientoDAO() throws SQLException {
        this.connection = ConexionBD.conectar();
    }

    /**
     * Genera un resumen de seguimiento completo para un proyecto.
     * Usa consultas SQL eficientes en lugar de loops N+1 desde Java.
     */
    public ResumenSeguimiento generarResumen(int idProyecto) throws SQLException {
        ResumenSeguimiento resumen = new ResumenSeguimiento();

        // 1. Obtener datos planificados del proyecto
        obtenerDatosPlanificados(idProyecto, resumen);

        // 2. Obtener datos reales (registrados, activos, retirados) agrupados por tipo y estado
        obtenerDatosReales(idProyecto, resumen);

        // 3. Calcular totales y verificar cumplimiento
        resumen.calcularTotales();

        return resumen;
    }

    /**
     * Obtiene los conteos planificados directamente del proyecto
     */
    private void obtenerDatosPlanificados(int idProyecto, ResumenSeguimiento resumen) throws SQLException {
        String sql = "SELECT num_asistentes_planificados, num_ayudantes_planificados, num_tecnico_planificados " +
                "FROM proyecto WHERE id_proyecto = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idProyecto);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    resumen.setCantidadAsistentesPlanificados(rs.getInt("num_asistentes_planificados"));
                    resumen.setCantidadAyudantesPlanificados(rs.getInt("num_ayudantes_planificados"));
                    resumen.setCantidadTecnicosPlanificados(rs.getInt("num_tecnico_planificados"));
                }
            }
        }
    }

    /**
     * Obtiene datos reales agrupados por tipo de personal y estado de participación.
     * Una sola consulta con GROUP BY reemplaza el loop N+1 anterior.
     *
     * NOTA sobre "registrados": se cuenta cada personal que tiene al menos una participación,
     * independiente de su estado actual. Un personal puede tener participaciones ACTIVO
     * y RETIRADO simultáneamente (ej: retirado de un periodo, activo en otro), por lo que
     * "registrados" = COUNT DISTINCT sobre cedula.
     */
    private void obtenerDatosReales(int idProyecto, ResumenSeguimiento resumen) throws SQLException {
        // Contar registrados por tipo (personas únicas con participación, sin importar estado)
        String sqlRegistrados = "SELECT p.tipo, COUNT(DISTINCT p.cedula) as total " +
                "FROM personaldeinvestigacion p " +
                "JOIN participacion par ON p.cedula = par.cedula_personal " +
                "WHERE p.id_proyecto = ? " +
                "GROUP BY p.tipo";

        try (PreparedStatement stmt = connection.prepareStatement(sqlRegistrados)) {
            stmt.setInt(1, idProyecto);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    int cantidad = rs.getInt("total");
                    if ("Asistente".equalsIgnoreCase(tipo)) {
                        resumen.setCantidadAsistentesRegistrados(cantidad);
                    } else if ("Ayudante".equalsIgnoreCase(tipo)) {
                        resumen.setCantidadAyudantesRegistrados(cantidad);
                    } else if ("Tecnico".equalsIgnoreCase(tipo)) {
                        resumen.setCantidadTecnicosRegistrados(cantidad);
                    }
                }
            }
        }

        // Contar activos y retirados por tipo y estado
        String sqlPorEstado = "SELECT p.tipo, par.estado, COUNT(*) as total " +
                "FROM personaldeinvestigacion p " +
                "JOIN participacion par ON p.cedula = par.cedula_personal " +
                "WHERE p.id_proyecto = ? " +
                "GROUP BY p.tipo, par.estado";

        try (PreparedStatement stmt = connection.prepareStatement(sqlPorEstado)) {
            stmt.setInt(1, idProyecto);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    String estado = rs.getString("estado");
                    int cantidad = rs.getInt("total");

                    if ("Asistente".equalsIgnoreCase(tipo)) {
                        acumularAsistente(resumen, estado, cantidad);
                    } else if ("Ayudante".equalsIgnoreCase(tipo)) {
                        acumularAyudante(resumen, estado, cantidad);
                    } else if ("Tecnico".equalsIgnoreCase(tipo)) {
                        acumularTecnico(resumen, estado, cantidad);
                    }
                }
            }
        }
    }

    // --- Métodos auxiliares para clasificar conteos por estado ---

    private void acumularAsistente(ResumenSeguimiento r, String estado, int cantidad) {
        if ("ACTIVO".equalsIgnoreCase(estado)) {
            r.setCantidadAsistentesActivos(r.getCantidadAsistentesActivos() + cantidad);
        } else if ("RETIRADO".equalsIgnoreCase(estado)) {
            r.setCantidadAsistentesRetirados(r.getCantidadAsistentesRetirados() + cantidad);
        }
    }

    private void acumularAyudante(ResumenSeguimiento r, String estado, int cantidad) {
        if ("ACTIVO".equalsIgnoreCase(estado)) {
            r.setCantidadAyudantesActivos(r.getCantidadAyudantesActivos() + cantidad);
        } else if ("RETIRADO".equalsIgnoreCase(estado)) {
            r.setCantidadAyudantesRetirados(r.getCantidadAyudantesRetirados() + cantidad);
        }
    }

    private void acumularTecnico(ResumenSeguimiento r, String estado, int cantidad) {
        if ("ACTIVO".equalsIgnoreCase(estado)) {
            r.setCantidadTecnicosActivos(r.getCantidadTecnicosActivos() + cantidad);
        } else if ("RETIRADO".equalsIgnoreCase(estado)) {
            r.setCantidadTecnicosRetirados(r.getCantidadTecnicosRetirados() + cantidad);
        }
    }
}
