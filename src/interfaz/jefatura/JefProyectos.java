package interfaz.jefatura;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import Logica.DAO.*;
import Logica.Entidades.*;
import Logica.Enumeraciones.*;
import interfaz.comun.EstiloUI;

import java.sql.SQLException;
import java.util.List;

/**
 * Módulo de Proyectos para Jefatura.
 * – Listado de todos los proyectos con sus estados y directores
 * – Formulario para crear un nuevo proyecto (estado EN_REVISION por defecto)
 * – Botón "Resumen" que abre popup de seguimiento
 * – Botones de aprobar/rechazar inline
 * – El código del proyecto se asigna al momento de la aprobación
 */
public class JefProyectos extends VBox {

    private final Jefatura jefatura;
    private VBox listaProyectos;

    public JefProyectos(Jefatura jefatura) {
        super(18);
        this.jefatura = jefatura;
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";");

        construir();
    }

    private void construir() {
        getChildren().clear();

        // ── Título + botón crear ────────────────────────────────
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(EstiloUI.labelTitulo("Proyectos"));
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);

        Button btnCrear = EstiloUI.botonPrimario("+ Nuevo Proyecto");
        btnCrear.setOnAction(e -> mostrarFormularioCrear());
        header.getChildren().add(btnCrear);
        getChildren().add(header);

        // ── Listado ─────────────────────────────────────────────
        VBox tarjeta = EstiloUI.tarjeta();

        // Header de tabla
        HBox tableHeader = new HBox();
        tableHeader.setPadding(new Insets(10, 0, 10, 0));
        tableHeader.setStyle("-fx-border-color: " + EstiloUI.C_DARK + "; -fx-border-width: 0 0 2 0;");
        String[] cols = {"Proyecto", "Código", "Director", "Estado", "Tipo", "Acciones"};
        double[] widths = {260, 100, 200, 110, 100, 200};
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + EstiloUI.C_GRAY_DARK + ";");
            lbl.setMinWidth(widths[i]);
            lbl.setPrefWidth(widths[i]);
            tableHeader.getChildren().add(lbl);
        }
        tarjeta.getChildren().add(tableHeader);

        listaProyectos = new VBox(0);
        tarjeta.getChildren().add(listaProyectos);
        getChildren().add(tarjeta);

        // Cargar datos
        cargarProyectos();
    }

    private void cargarProyectos() {
        listaProyectos.getChildren().clear();
        try {
            ProyectoDAO pDAO = new ProyectoDAO();
            List<Proyecto> proyectos = pDAO.obtenerTodos();

            if (proyectos.isEmpty()) {
                listaProyectos.getChildren().add(EstiloUI.labelSmall("  No hay proyectos registrados."));
            }

            for (Proyecto p : proyectos) {
                listaProyectos.getChildren().add(crearFila(p));
            }
        } catch (SQLException e) {
            listaProyectos.getChildren().add(EstiloUI.labelSmall("  Error: " + e.getMessage()));
        }
    }

    private HBox crearFila(Proyecto p) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 0, 10, 0));
        fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

        // Nombre
        Label nombre = EstiloUI.labelBody(p.getNombre());
        nombre.setMinWidth(260); nombre.setPrefWidth(260);

        // Código (vacío si está en revisión)
        String codigoText = (p.getCodigoProyecto() != null && !p.getCodigoProyecto().isEmpty())
                ? p.getCodigoProyecto()
                : "—";
        Label codigo = EstiloUI.labelBody(codigoText);
        codigo.setMinWidth(100); codigo.setPrefWidth(100);

        // Director
        String dirNom = (p.getDirector() != null) ? p.getDirector().getNombresCompletos() : "Candidato";
        Label director = EstiloUI.labelBody(dirNom);
        director.setMinWidth(200); director.setPrefWidth(200);

        // Estado badge
        Label estado = EstiloUI.badgeEstadoProyecto(p.getEstado().name());
        estado.setMinWidth(110); estado.setPrefWidth(110);

        // Tipo
        Label tipo = EstiloUI.labelBody(p.getTipoProyecto());
        tipo.setMinWidth(100); tipo.setPrefWidth(100);

        // Acciones
        HBox acciones = new HBox(6);
        acciones.setAlignment(Pos.CENTER);

        if (p.getEstado() == EstadoProyecto.EN_REVISION) {
            Button btnApr = EstiloUI.botonSmall("Aprobar",  EstiloUI.C_MEDIUM);
            Button btnRec = EstiloUI.botonSmall("Rechazar", EstiloUI.C_RED);
            btnApr.setOnAction(e -> aprobar(p));
            btnRec.setOnAction(e -> rechazar(p));
            acciones.getChildren().addAll(btnApr, btnRec);
        }

        Button btnRes = EstiloUI.botonSmall("Resumen", EstiloUI.C_DARK);
        btnRes.setOnAction(e -> mostrarResumenSeguimiento(p));
        acciones.getChildren().add(btnRes);

        fila.getChildren().addAll(nombre, codigo, director, estado, tipo, acciones);
        return fila;
    }

    // ─── APROBAR / RECHAZAR ──────────────────────────────────────────────
    private void aprobar(Proyecto proyecto) {
        // Crear diálogo para solicitar el código del proyecto
        VBox form = new VBox(14);
        form.setPadding(new Insets(10));
        form.setMinWidth(400);

        Label instruccion = EstiloUI.labelBody("Ingrese el código para el proyecto \"" + proyecto.getNombre() + "\":");
        TextField txtCodigo = EstiloUI.crearTextField("Código del proyecto (ej. PRY001)");

        form.getChildren().addAll(instruccion, txtCodigo);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Aprobar Proyecto");
        dialog.setHeaderText("Asignar Código y Aprobar");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(450);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String codigo = txtCodigo.getText().trim();

                // Validar que se haya ingresado un código
                if (codigo.isEmpty()) {
                    EstiloUI.alertaError("Validación", "Debe ingresar un código para el proyecto.").showAndWait();
                    return;
                }

                try {
                    // Verificar que el código no esté duplicado
                    ProyectoDAO pDAO = new ProyectoDAO();
                    Proyecto existente = pDAO.obtenerPorCodigo(codigo);
                    if (existente != null && existente.getIdProyecto() != proyecto.getIdProyecto()) {
                        EstiloUI.alertaError("Error", "Ya existe un proyecto con el código: " + codigo).showAndWait();
                        return;
                    }

                    // Asignar el código al proyecto
                    proyecto.setCodigoProyecto(codigo);

                    // Aprobar el proyecto (esto creará el usuario del director)
                    jefatura.actualizarEstadoProyecto(proyecto, EstadoProyecto.APROBADO);
                    EstiloUI.alertaInfo("Éxito", "Proyecto aprobado con código: " + codigo).showAndWait();
                    construir(); // refrescar
                } catch (SQLException e) {
                    EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
                }
            }
        });
    }

    private void rechazar(Proyecto proyecto) {
        Alert c = EstiloUI.alertaConfirmacion("Rechazar",
                "¿Marcar como no aprobado \"" + proyecto.getNombre() + "\"?");
        c.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    jefatura.actualizarEstadoProyecto(proyecto, EstadoProyecto.NO_APROBADO);
                    EstiloUI.alertaInfo("Listo", "Proyecto no aprobado.").showAndWait();
                    construir();
                } catch (SQLException e) {
                    EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
                }
            }
        });
    }

    // ─── RESUMEN DE SEGUIMIENTO (popup) ──────────────────────────────────
    private void mostrarResumenSeguimiento(Proyecto proyecto) {
        try {
            ResumenSeguimiento resumen = jefatura.generarResumenSeguimiento(proyecto.getIdProyecto());

            // Construir contenido del popup
            GridPane grid = EstiloUI.crearGridFormulario();
            grid.setPadding(new Insets(16));

            // Header fila
            String[] headers = {"Tipo", "Planificado", "Registrado", "Activo", "Retirado"};
            for (int i = 0; i < headers.length; i++) {
                Label h = new Label(headers[i]);
                h.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + EstiloUI.C_VERY_DARK + ";");
                GridPane.setColumnIndex(h, i);
                GridPane.setRowIndex(h, 0);
                grid.getChildren().add(h);
            }

            // Asistentes
            agregarFila(grid, 1, "Asistentes",
                    resumen.getCantidadAsistentesPlanificados(),
                    resumen.getCantidadAsistentesRegistrados(),
                    resumen.getCantidadAsistentesActivos(),
                    resumen.getCantidadAsistentesRetirados());
            // Ayudantes
            agregarFila(grid, 2, "Ayudantes",
                    resumen.getCantidadAyudantesPlanificados(),
                    resumen.getCantidadAyudantesRegistrados(),
                    resumen.getCantidadAyudantesActivos(),
                    resumen.getCantidadAyudantesRetirados());
            // Técnicos
            agregarFila(grid, 3, "Técnicos",
                    resumen.getCantidadTecnicosPlanificados(),
                    resumen.getCantidadTecnicosRegistrados(),
                    resumen.getCantidadTecnicosActivos(),
                    resumen.getCantidadTecnicosRetirados());
            // Totales
            agregarFila(grid, 4, "TOTAL",
                    resumen.getTotalPlanificado(),
                    resumen.getTotalRegistrado(),
                    resumen.getTotalActivo(),
                    resumen.getTotalRetirado());

            // Cumplimiento
            Label cumple = new Label(resumen.isCumplePlanificacionGlobal() ? "Cumple planificación" : "No cumple planificación");
            cumple.setStyle("-fx-font-weight: bold; -fx-text-fill: " +
                    (resumen.isCumplePlanificacionGlobal() ? EstiloUI.C_VERY_DARK : EstiloUI.C_RED) + ";");
            GridPane.setColumnIndex(cumple, 0);
            GridPane.setRowIndex(cumple, 5);
            GridPane.setColumnSpan(cumple, 5);
            grid.getChildren().add(cumple);

            // Diálogo popup
            Alert popup = new Alert(Alert.AlertType.INFORMATION);
            popup.setTitle("Resumen de Seguimiento");
            popup.setHeaderText("Proyecto: " + proyecto.getNombre());
            popup.getDialogPane().setContent(grid);
            popup.getDialogPane().setMinWidth(480);
            popup.showAndWait();

        } catch (SQLException e) {
            EstiloUI.alertaError("Error", "No se pudo generar resumen: " + e.getMessage()).showAndWait();
        }
    }

    private void agregarFila(GridPane grid, int row, String tipo, int plan, int reg, int act, int ret) {
        Label lTipo = new Label(tipo);
        lTipo.setStyle("-fx-font-size: 13px; -fx-font-weight: " + (row == 4 ? "bold" : "normal") + ";");
        Label lPlan = new Label(String.valueOf(plan));
        Label lReg  = new Label(String.valueOf(reg));
        Label lAct  = new Label(String.valueOf(act));
        Label lRet  = new Label(String.valueOf(ret));

        for (Label l : new Label[]{lPlan, lReg, lAct, lRet}) {
            l.setStyle("-fx-font-size: 13px; -fx-alignment: center;");
        }

        GridPane.setColumnIndex(lTipo, 0); GridPane.setRowIndex(lTipo, row);
        GridPane.setColumnIndex(lPlan, 1); GridPane.setRowIndex(lPlan, row);
        GridPane.setColumnIndex(lReg,  2); GridPane.setRowIndex(lReg,  row);
        GridPane.setColumnIndex(lAct,  3); GridPane.setRowIndex(lAct,  row);
        GridPane.setColumnIndex(lRet,  4); GridPane.setRowIndex(lRet,  row);

        if (row == 4) {
            for (Label l : new Label[]{lTipo, lPlan, lReg, lAct, lRet}) {
                l.setStyle(l.getStyle() + "-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 1 0 0 0;");
            }
        }

        grid.getChildren().addAll(lTipo, lPlan, lReg, lAct, lRet);
    }

    // ─── FORMULARIO CREAR PROYECTO ───────────────────────────────────────
    private void mostrarFormularioCrear() {
        // Crear un diálogo modal con un formulario
        VBox form = new VBox(14);
        form.setPadding(new Insets(10));
        form.setMinWidth(500);

        // Campos (sin el código, ya que se asignará al aprobar)
        TextField txtNombre   = EstiloUI.crearTextField("Nombre del proyecto");
        TextField txtDurMeses = EstiloUI.crearTextField("Duración en meses");

        // Tipo proyecto
        ComboBox<String> cboTipo = new ComboBox<>();
        cboTipo.getItems().addAll("Interno", "Semilla");
        cboTipo.setValue("Interno");
        cboTipo.setStyle("-fx-font-size: 13px;");
        cboTipo.setPrefHeight(36);

        // Periodo académico
        ComboBox<String> cboPeriodo = new ComboBox<>();
        try {
            PeriodoAcademicoDAO paDAO = new PeriodoAcademicoDAO();
            paDAO.obtenerTodos().forEach(pa -> cboPeriodo.getItems().add(pa.getCodigo()));
        } catch (SQLException ignored) {}
        cboPeriodo.setStyle("-fx-font-size: 13px;");
        cboPeriodo.setPrefHeight(36);

        // Personal planificado
        TextField txtAsist  = EstiloUI.crearTextField("Asistentes planificados");
        TextField txtAyud   = EstiloUI.crearTextField("Ayudantes planificados");
        TextField txtTecn   = EstiloUI.crearTextField("Técnicos planificados");

        // Director candidato
        Label sepDir = EstiloUI.labelSeccion("Información del Director");
        TextField txtDirNombres    = EstiloUI.crearTextField("Nombres");
        TextField txtDirApellidos  = EstiloUI.crearTextField("Apellidos");
        TextField txtDirCedula     = EstiloUI.crearTextField("Cédula (10 dígitos)");
        TextField txtDirCorreo     = EstiloUI.crearTextField("Correo electrónico");

        HBox row1 = new HBox(12); row1.getChildren().addAll(
                wrapLabel("Nombre:", txtNombre));
        HBox row2 = new HBox(12); row2.getChildren().addAll(
                wrapLabel("Duración:", txtDurMeses),
                wrapLabel("Tipo:", cboTipo));
        HBox row3 = new HBox(12); row3.getChildren().addAll(
                wrapLabel("Periodo:", cboPeriodo));
        HBox row4 = new HBox(12); row4.getChildren().addAll(
                wrapLabel("Asistentes:", txtAsist),
                wrapLabel("Ayudantes:", txtAyud),
                wrapLabel("Técnicos:", txtTecn));
        HBox row5 = new HBox(12); row5.getChildren().addAll(
                wrapLabel("Nombres:", txtDirNombres),
                wrapLabel("Apellidos:", txtDirApellidos));
        HBox row6 = new HBox(12); row6.getChildren().addAll(
                wrapLabel("Cédula:", txtDirCedula),
                wrapLabel("Correo:", txtDirCorreo));

        form.getChildren().addAll(row1, row2, row3, row4, sepDir, row5, row6);

        // Diálogo
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Crear Proyecto");
        dialog.setHeaderText("Nuevo Proyecto (Estado: En Revisión)");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(560);
        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Guardar"),
                ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn.getText().equals("Guardar")) {
                guardarProyecto(txtNombre, txtDurMeses, cboTipo, cboPeriodo,
                        txtAsist, txtAyud, txtTecn,
                        txtDirNombres, txtDirApellidos, txtDirCedula, txtDirCorreo);
            }
        });
    }

    private void guardarProyecto(TextField nombre, TextField dur,
                                 ComboBox<String> tipo, ComboBox<String> periodo,
                                 TextField asist, TextField ayud, TextField tecn,
                                 TextField dirNom, TextField dirApe, TextField dirCed, TextField dirCorr) {
        // Validaciones básicas
        if (nombre.getText().trim().isEmpty()) {
            EstiloUI.alertaError("Validación", "El nombre es obligatorio.").showAndWait();
            return;
        }
        if (dirCed.getText().trim().length() != 10) {
            EstiloUI.alertaError("Validación", "La cédula del director debe tener 10 dígitos.").showAndWait();
            return;
        }
        if (periodo.getValue() == null) {
            EstiloUI.alertaError("Validación", "Seleccione un periodo académico.").showAndWait();
            return;
        }

        try {
            // Crear objeto Proyecto
            Proyecto proyecto;
            if ("Semilla".equals(tipo.getValue())) {
                proyecto = new Logica.Entidades.ProyectoSemilla();
            } else {
                proyecto = new Logica.Entidades.ProyectoInterno();
            }

            proyecto.setNombre(nombre.getText().trim());
            // NO se asigna código aquí - será asignado al aprobar
            proyecto.setCodigoProyecto(null);
            proyecto.setDuracionMeses(Integer.parseInt(dur.getText().trim().isEmpty() ? "0" : dur.getText().trim()));
            proyecto.setEstado(EstadoProyecto.EN_REVISION);

            // Periodo
            PeriodoAcademicoDAO paDAO = new PeriodoAcademicoDAO();
            proyecto.setPeriodoInicio(paDAO.obtenerPorCodigo(periodo.getValue()));

            // Personal planificado
            proyecto.setNumAsistentesPlanificados(parseInt(asist.getText()));
            proyecto.setNumAyudantesPlanificados(parseInt(ayud.getText()));
            proyecto.setNumTecnicosPlanificados(parseInt(tecn.getText()));

            // Director candidato (temporal)
            Director dirCandidato = new Director();
            dirCandidato.setNombres(dirNom.getText().trim());
            dirCandidato.setApellidos(dirApe.getText().trim());
            dirCandidato.setCedula(dirCed.getText().trim());
            dirCandidato.setCorreo(dirCorr.getText().trim());
            proyecto.setDirector(dirCandidato);

            // Guardar
            jefatura.registrarProyecto(proyecto);
            EstiloUI.alertaInfo("Éxito", "Proyecto creado en estado \"En Revisión\". El código se asignará al aprobar.").showAndWait();
            construir(); // refrescar

        } catch (Exception e) {
            EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
        }
    }

    // ─── Utilidades ──────────────────────────────────────────────────────
    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return 0; }
    }

    private HBox wrapLabel(String label, Control control) {
        HBox wrap = new HBox(6);
        wrap.setAlignment(Pos.CENTER_LEFT);
        Label lbl = EstiloUI.labelSmall(label);
        lbl.setMinWidth(80);
        control.setPrefWidth(180);
        wrap.getChildren().addAll(lbl, control);
        HBox.setHgrow(wrap, Priority.ALWAYS);
        return wrap;
    }
}