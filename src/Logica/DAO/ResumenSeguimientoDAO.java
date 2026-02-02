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
     * Genera el reporte de seguimiento para un proyecto específico.
     * @param idProyecto El ID del proyecto a analizar.
     * @return Objeto ResumenSeguimiento con los datos calculados.
     */
    public ResumenSeguimiento generarResumen(int idProyecto) throws SQLException {
        ResumenSeguimiento resumen = new ResumenSeguimiento();

        // PASO 1: Obtener lo PLANIFICADO desde la tabla Proyecto
        obtenerDatosPlanificados(idProyecto, resumen);

        // PASO 2: Obtener lo REGISTRADO (Real) desde las tablas Participacion y Personal
        obtenerDatosReales(idProyecto, resumen);

        // PASO 3: Ejecutar cálculos finales en la entidad
        resumen.calcularTotales();

        return resumen;
    }

    private void obtenerDatosPlanificados(int idProyecto, ResumenSeguimiento resumen) throws SQLException {
        String sql = "SELECT num_asistentes_planificados, num_ayudantes_planificados, num_tecnico_planificados " +
                "FROM Proyecto WHERE id_proyecto = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idProyecto);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                resumen.setCantidadAsistentesPlanificados(rs.getInt("num_asistentes_planificados"));
                resumen.setCantidadAyudantesPlanificados(rs.getInt("num_ayudantes_planificados"));
                resumen.setCantidadTecnicosPlanificados(rs.getInt("num_tecnico_planificados"));
            }
        }
    }

    private void obtenerDatosReales(int idProyecto, ResumenSeguimiento resumen) throws SQLException {
        // Esta consulta agrupa por TIPO y ESTADO para contar eficientemente
        // Personal -> obtiene el tipo (Asistente, Ayudante...)
        // Participacion -> obtiene el estado (Activo, Retirado...)
        String sql = "SELECT p.tipo, par.estado, COUNT(*) as total " +
                "FROM PersonalDeInvestigacion p " +
                "JOIN Participacion par ON p.cedula = par.cedula_personal " +
                "WHERE p.id_proyecto = ? " +
                "GROUP BY p.tipo, par.estado";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idProyecto);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tipo = rs.getString("tipo");     // Ej: "Asistente", "Ayudante"
                String estado = rs.getString("estado"); // Ej: "ACTIVO", "RETIRADO"
                int cantidad = rs.getInt("total");

                // Distribuimos los conteos según el tipo y estado
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

    // --- Métodos auxiliares para organizar el código ---

    private void acumularAsistente(ResumenSeguimiento r, String estado, int cantidad) {
        // Sumamos al total de registrados
        r.setCantidadAsistentesRegistrados(r.getCantidadAsistentesRegistrados() + cantidad);

        // Clasificamos por estado
        if ("ACTIVO".equalsIgnoreCase(estado)) {
            r.setCantidadAsistentesActivos(r.getCantidadAsistentesActivos() + cantidad);
        } else if ("RETIRADO".equalsIgnoreCase(estado)) {
            r.setCantidadAsistentesRetirados(r.getCantidadAsistentesRetirados() + cantidad);
        }
    }

    private void acumularAyudante(ResumenSeguimiento r, String estado, int cantidad) {
        r.setCantidadAyudantesRegistrados(r.getCantidadAyudantesRegistrados() + cantidad);

        if ("ACTIVO".equalsIgnoreCase(estado)) {
            r.setCantidadAyudantesActivos(r.getCantidadAyudantesActivos() + cantidad);
        } else if ("RETIRADO".equalsIgnoreCase(estado)) {
            r.setCantidadAyudantesRetirados(r.getCantidadAyudantesRetirados() + cantidad);
        }
    }

    private void acumularTecnico(ResumenSeguimiento r, String estado, int cantidad) {
        r.setCantidadTecnicosRegistrados(r.getCantidadTecnicosRegistrados() + cantidad);

        if ("ACTIVO".equalsIgnoreCase(estado)) {
            r.setCantidadTecnicosActivos(r.getCantidadTecnicosActivos() + cantidad);
        } else if ("RETIRADO".equalsIgnoreCase(estado)) {
            r.setCantidadTecnicosRetirados(r.getCantidadTecnicosRetirados() + cantidad);
        }
    }
}