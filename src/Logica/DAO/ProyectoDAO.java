package Logica.DAO;

import Logica.Conexiones.ConexionBD;
import Logica.Entidades.*;
import Logica.Enumeraciones.EstadoProyecto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProyectoDAO {
    private Connection connection;
    private DirectorDAO directorDAO;
    private PeriodoAcademicoDAO periodoDAO;

    public ProyectoDAO() throws SQLException {
        this.connection = ConexionBD.conectar();
        this.directorDAO = new DirectorDAO();
        this.periodoDAO = new PeriodoAcademicoDAO();
    }

    // --- MÉTODOS ESTÁNDAR (Sin cargar la lista pesada) ---

    public List<Proyecto> obtenerTodos() throws SQLException {
        List<Proyecto> proyectos = new ArrayList<>();
        String sql = "SELECT * FROM Proyecto ORDER BY nombre";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                proyectos.add(mapResultSet(rs));
            }
        }
        return proyectos;
    }

    public List<Proyecto> obtenerPorDirector(String cedulaDirector) throws SQLException {
        List<Proyecto> proyectos = new ArrayList<>();
        String sql = "SELECT * FROM Proyecto WHERE cedula_director = ? ORDER BY nombre";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cedulaDirector);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                proyectos.add(mapResultSet(rs));
            }
        }
        return proyectos;
    }

    public Proyecto obtenerPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM Proyecto WHERE codigo_proyecto = ?";

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
     * MÉTODO CLAVE: Carga la lista de personal asociada al proyecto.
     * Funciona igual que 'cargarNotificaciones' en Director.
     * Debe llamarse explícitamente cuando se necesite ver el equipo del proyecto.
     */
    public void cargarPersonalDelProyecto(Proyecto proyecto) throws SQLException {
        if (proyecto == null) return;

        List<PersonalDeInvestigacion> equipo = new ArrayList<>();

        // Consultamos la tabla PersonalDeInvestigacion filtrando por la FK id_proyecto
        String sql = "SELECT * FROM PersonalDeInvestigacion WHERE id_proyecto = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, proyecto.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Instanciación Polimórfica (Factory manual)
                String tipo = rs.getString("tipo");
                PersonalDeInvestigacion personal;

                if ("Ayudante".equalsIgnoreCase(tipo)) {
                    personal = new Ayudante();
                } else if ("Asistente".equalsIgnoreCase(tipo)) {
                    personal = new Asistente();
                } else if ("Tecnico".equalsIgnoreCase(tipo)) {
                    personal = new Tecnico();
                } else {
                    personal = new Tecnico(); // Default fallback
                }

                // Mapeo de datos comunes
                personal.setCedula(rs.getString("cedula"));
                personal.setNombres(rs.getString("nombres"));
                personal.setApellidos(rs.getString("apellidos"));
                personal.setCorreo(rs.getString("correo"));
                // personal.setIdUsuario(...) // Si necesitas el usuario, mapealo aquí

                equipo.add(personal);
            }
        }

        // Asignamos la lista llena al objeto proyecto
        proyecto.setPersonalDeInvestigacion(equipo);
    }

    public boolean guardar(Proyecto proyecto) throws SQLException {
        String sql = "INSERT INTO Proyecto (codigo_proyecto, nombre, periodo_inicio, duracion_meses, estado, " +
                "cedula_director, tipo_proyecto, num_asistentes_planificados, " +
                "num_ayudantes_planificados, num_tecnico_planificados) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, proyecto.getCodigoProyecto());
            stmt.setString(2, proyecto.getNombre());
            stmt.setString(3, proyecto.getPeriodoInicio().getCodigo());
            stmt.setInt(4, proyecto.getDuracionMeses());
            stmt.setString(5, proyecto.getEstado().name());
            stmt.setString(6, proyecto.getDirector().getCedula());
            stmt.setString(7, proyecto.getTipoProyecto());
            stmt.setInt(8, proyecto.getNumAsistentesPlanificados());
            stmt.setInt(9, proyecto.getNumAyudantesPlanificados());
            stmt.setInt(10, proyecto.getNumTecnicosPlanificados());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Proyecto proyecto) throws SQLException {
        String sql = "UPDATE Proyecto SET nombre = ?, periodo_inicio = ?, duracion_meses = ?, " +
                "estado = ?, num_asistentes_planificados = ?, num_ayudantes_planificados = ?, " +
                "num_tecnico_planificados = ?, cedula_director = ? WHERE codigo_proyecto = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, proyecto.getNombre());
            stmt.setString(2, proyecto.getPeriodoInicio().getCodigo());
            stmt.setInt(3, proyecto.getDuracionMeses());
            stmt.setString(4, proyecto.getEstado().name());
            stmt.setInt(5, proyecto.getNumAsistentesPlanificados());
            stmt.setInt(6, proyecto.getNumAyudantesPlanificados());
            stmt.setInt(7, proyecto.getNumTecnicosPlanificados());
            stmt.setString(8, proyecto.getDirector().getCedula());
            stmt.setString(9, proyecto.getCodigoProyecto());

            return stmt.executeUpdate() > 0;
        }
    }

    private Proyecto mapResultSet(ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo_proyecto");
        Proyecto proyecto;

        if ("Semilla".equalsIgnoreCase(tipo)) {
            proyecto = new ProyectoSemilla();
        } else if ("Interno".equalsIgnoreCase(tipo)) {
            proyecto = new ProyectoInterno();
        } else {
            throw new SQLException("Tipo de proyecto desconocido: " + tipo);
        }

        proyecto.setIdProyecto(rs.getInt("id_proyecto"));
        proyecto.setCodigoProyecto(rs.getString("codigo_proyecto"));
        proyecto.setNombre(rs.getString("nombre"));
        proyecto.setDuracionMeses(rs.getInt("duracion_meses"));
        proyecto.setEstado(EstadoProyecto.valueOf(rs.getString("estado")));

        // Carga ansiosa (Eager) de objetos simples
        String cedulaDir = rs.getString("cedula_director");
        if (cedulaDir != null) {
            proyecto.setDirector(directorDAO.obtenerPorCedula(cedulaDir));
        }

        String codPeriodo = rs.getString("periodo_inicio");
        if (codPeriodo != null) {
            proyecto.setPeriodoInicio(periodoDAO.obtenerPorCodigo(codPeriodo));
        }

        proyecto.setNumAsistentesPlanificados(rs.getInt("num_asistentes_planificados"));
        proyecto.setNumAyudantesPlanificados(rs.getInt("num_ayudantes_planificados"));
        proyecto.setNumTecnicosPlanificados(rs.getInt("num_tecnico_planificados"));

        // NOTA: La lista 'personalDeInvestigacion' se queda vacía aquí intencionalmente.
        // Se debe llamar a cargarPersonalDelProyecto(p) solo si se necesita.

        return proyecto;
    }
}