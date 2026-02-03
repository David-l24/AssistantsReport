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
        // Si el código es null o vacío, retornar null
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }

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
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_proyecto";

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

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                proyecto.setIdProyecto(rs.getInt("id_proyecto"));
                return true;
            }
            return false;
        }
    }

    /**
     * Guarda un proyecto en estado EN_REVISION SIN código.
     * Como el director aún NO existe en la tabla director, cedula_director se deja NULL
     * y los datos del director se almacenan en los campos candidato_* de la BD.
     * Cuando el proyecto se aprueba, se usa actualizarCodigoYDirector() para setear
     * tanto el código como la cedula_director.
     */
    public boolean guardarEnRevision(Proyecto proyecto, String candidato_nombres,
                                     String candidato_apellidos, String candidato_cedula,
                                     String candidato_correo) throws SQLException {
        String sql = "INSERT INTO Proyecto (codigo_proyecto, nombre, periodo_inicio, duracion_meses, estado, " +
                "cedula_director, tipo_proyecto, num_asistentes_planificados, " +
                "num_ayudantes_planificados, num_tecnico_planificados, " +
                "candidato_nombres, candidato_apellidos, candidato_cedula, candidato_correo) " +
                "VALUES (NULL, ?, ?, ?, ?, NULL, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_proyecto";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, proyecto.getNombre());
            stmt.setString(2, proyecto.getPeriodoInicio().getCodigo());
            stmt.setInt(3, proyecto.getDuracionMeses());
            stmt.setString(4, proyecto.getEstado().name());
            stmt.setString(5, proyecto.getTipoProyecto());
            stmt.setInt(6, proyecto.getNumAsistentesPlanificados());
            stmt.setInt(7, proyecto.getNumAyudantesPlanificados());
            stmt.setInt(8, proyecto.getNumTecnicosPlanificados());
            stmt.setString(9, candidato_nombres);
            stmt.setString(10, candidato_apellidos);
            stmt.setString(11, candidato_cedula);
            stmt.setString(12, candidato_correo);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                proyecto.setIdProyecto(rs.getInt("id_proyecto"));
                return true;
            }
            return false;
        }
    }

    /**
     * Actualiza el código del proyecto y asigna el director real.
     * Se usa en el flujo de aprobación cuando se pasa de EN_REVISION a APROBADO.
     */
    public boolean actualizarCodigoYDirector(int idProyecto, String codigoProyecto, String cedulaDirector) throws SQLException {
        String sql = "UPDATE Proyecto SET codigo_proyecto = ?, cedula_director = ? WHERE id_proyecto = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigoProyecto);
            stmt.setString(2, cedulaDirector);
            stmt.setInt(3, idProyecto);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Asigna el cedula_director real al proyecto DESPUÉS de que el director ya existe
     * en la tabla director. Se mantiene por compatibilidad.
     */
    public boolean actualizarDirector(int idProyecto, String cedulaDirector) throws SQLException {
        String sql = "UPDATE Proyecto SET cedula_director = ? WHERE id_proyecto = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cedulaDirector);
            stmt.setInt(2, idProyecto);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Proyecto proyecto) throws SQLException {
        String sql = "UPDATE Proyecto SET nombre = ?, periodo_inicio = ?, duracion_meses = ?, " +
                "estado = ?, num_asistentes_planificados = ?, num_ayudantes_planificados = ?, " +
                "num_tecnico_planificados = ?, cedula_director = ?, codigo_proyecto = ? WHERE id_proyecto = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, proyecto.getNombre());
            stmt.setString(2, proyecto.getPeriodoInicio().getCodigo());
            stmt.setInt(3, proyecto.getDuracionMeses());
            stmt.setString(4, proyecto.getEstado().name());
            stmt.setInt(5, proyecto.getNumAsistentesPlanificados());
            stmt.setInt(6, proyecto.getNumAyudantesPlanificados());
            stmt.setInt(7, proyecto.getNumTecnicosPlanificados());
            stmt.setString(8, proyecto.getDirector() != null ? proyecto.getDirector().getCedula() : null);
            stmt.setString(9, proyecto.getCodigoProyecto());
            stmt.setInt(10, proyecto.getIdProyecto());

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

        // Si el proyecto aún no tiene director real (EN_REVISION), construir uno temporal
        // con los datos candidato para que Jefatura pueda usarlos en la aprobación.
        if (proyecto.getDirector() == null) {
            String candCedula = rs.getString("candidato_cedula");
            if (candCedula != null) {
                Director candidato = new Director();
                candidato.setCedula(candCedula);
                candidato.setNombres(rs.getString("candidato_nombres"));
                candidato.setApellidos(rs.getString("candidato_apellidos"));
                candidato.setCorreo(rs.getString("candidato_correo"));
                proyecto.setDirector(candidato);
            }
        }

        // NOTA: La lista 'personalDeInvestigacion' se queda vacía aquí intencionalmente.
        // Se debe llamar a cargarPersonalDelProyecto(p) solo si se necesita.

        return proyecto;
    }

    /**
     * Obtiene un proyecto por su ID
     */
    public Proyecto obtenerPorId(int idProyecto) throws SQLException {
        String sql = "SELECT * FROM Proyecto WHERE id_proyecto = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idProyecto);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null;
    }

    public boolean actualizarEstado(Proyecto proyecto) throws SQLException {
        String sql = "UPDATE Proyecto SET nombre = ?, periodo_inicio = ?, duracion_meses = ?, " +
                "estado = ?, num_asistentes_planificados = ?, num_ayudantes_planificados = ?, " +
                "num_tecnico_planificados = ?, codigo_proyecto = ? WHERE id_proyecto = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, proyecto.getNombre());
            stmt.setString(2, proyecto.getPeriodoInicio().getCodigo());
            stmt.setInt(3, proyecto.getDuracionMeses());
            stmt.setString(4, proyecto.getEstado().name());
            stmt.setInt(5, proyecto.getNumAsistentesPlanificados());
            stmt.setInt(6, proyecto.getNumAyudantesPlanificados());
            stmt.setInt(7, proyecto.getNumTecnicosPlanificados());
            stmt.setString(8, proyecto.getCodigoProyecto());
            stmt.setInt(9, proyecto.getIdProyecto());

            return stmt.executeUpdate() > 0;
        }
    }
}