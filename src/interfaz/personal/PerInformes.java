package interfaz.personal;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import Logica.DAO.*;
import Logica.Entidades.*;
import Logica.Enumeraciones.*;
import interfaz.comun.EstiloUI;

import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Módulo de Informes de Actividades para el Personal de Investigación.
 *
 * Flujo:
 *  1. Lista de informes existentes con estado y acciones según contexto.
 *  2. "+ Nuevo Informe" abre un panel de edición en blanco.
 *  3. Seleccionar informe EN_EDICION (o RECHAZADO → devuelto) abre el mismo panel con datos.
 *  4. Dentro del panel se puede agregar / eliminar semanas.
 *     Cada semana tiene: 5 filas (fecha + hora inicio + hora salida), actividad, observaciones.
 *  5. "Guardar" persiste el informe + semanas.
 *  6. "Enviar" valida y cambia estado a ENVIADO (notifica al Director).
 *
 * Restricciones mostradas en la UI:
 *  – Solo se puede editar un informe EN_EDICION.
 *  – Un informe RECHAZADO puede ser devuelto a EN_EDICION desde aquí.
 *  – Se necesita al menos 1 semana para enviar.
 */
public class PerInformes extends BorderPane {

    private final PersonalDeInvestigacion personal;
    private final Proyecto                proyecto;

    // ── Áreas intercambiables ──
    private final VBox contenidoTabla;   // filas de la lista superior
    private final VBox panelEdicion;     // formulario de edición (bajo la tabla)

    // Estado de edición activo
    private InformeActividades informeEnEdicion;   // null = nuevo
    private final List<SemanaActividades> semanasEnMemoria = new ArrayList<>();

    // Contenedor principal con scroll
    private final VBox contenedorPrincipal;
    private final ScrollPane scrollPane;

    public PerInformes(PersonalDeInvestigacion personal, Proyecto proyecto) {
        super();
        this.personal = personal;
        this.proyecto = proyecto;
        setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";");

        // Crear contenedor principal que irá dentro del ScrollPane
        contenedorPrincipal = new VBox(18);
        contenedorPrincipal.setPadding(new Insets(24));

        // Configurar ScrollPane
        scrollPane = new ScrollPane(contenedorPrincipal);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + "; -fx-background: " + EstiloUI.C_OFF_WHITE + ";");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Agregar el ScrollPane al centro del BorderPane
        setCenter(scrollPane);

        contenidoTabla = new VBox(0);
        panelEdicion   = new VBox(14);

        construir();
    }

    // ─── CONSTRUCCIÓN INICIAL ──────────────────────────────────────────────
    private void construir() {
        contenedorPrincipal.getChildren().clear();

        // ── Header + botón nuevo ──
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(EstiloUI.labelTitulo("Informes de Actividades"));
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);

        Button btnNuevo = EstiloUI.botonPrimario("+ Nuevo Informe");
        btnNuevo.setOnAction(e -> abrirPanelNuevo());
        header.getChildren().add(btnNuevo);
        contenedorPrincipal.getChildren().add(header);

        if (proyecto == null) {
            contenedorPrincipal.getChildren().add(EstiloUI.labelSmall("No hay proyecto asignado."));
            return;
        }

        // ── Tabla de informes ──
        VBox tarjeta = EstiloUI.tarjeta();

        HBox tableHeader = new HBox();
        tableHeader.setPadding(new Insets(10, 0, 10, 0));
        tableHeader.setStyle("-fx-border-color: " + EstiloUI.C_DARK + "; -fx-border-width: 0 0 2 0;");
        String[] cols   = {"Fecha Registro", "Estado", "Semanas", "Horas Total", "Acciones"};
        double[]  widths = {150,             120,      80,        110,           220};
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + EstiloUI.C_GRAY_DARK + ";");
            lbl.setMinWidth(widths[i]);
            lbl.setPrefWidth(widths[i]);
            tableHeader.getChildren().add(lbl);
        }
        tarjeta.getChildren().add(tableHeader);
        tarjeta.getChildren().add(contenidoTabla);
        contenedorPrincipal.getChildren().add(tarjeta);

        // ── Panel de edición (aparece debajo) ──
        contenedorPrincipal.getChildren().add(panelEdicion);

        cargarInformes();
    }

    // ─── CARGA DE LISTA ─────────────────────────────────────────────────────
    private void cargarInformes() {
        contenidoTabla.getChildren().clear();
        panelEdicion.getChildren().clear();
        informeEnEdicion = null;
        semanasEnMemoria.clear();

        try {
            InformeActividadesDAO iaDAO = new InformeActividadesDAO();
            List<InformeActividades> informes = iaDAO.obtenerPorPersonal(personal.getCedula());

            if (informes.isEmpty()) {
                contenidoTabla.getChildren().add(EstiloUI.labelSmall("  No hay informes aún. Cree uno nuevo."));
                return;
            }

            for (InformeActividades inf : informes) {
                contenidoTabla.getChildren().add(crearFilaInforme(inf));
            }

        } catch (SQLException e) {
            contenidoTabla.getChildren().add(EstiloUI.labelSmall("  Error: " + e.getMessage()));
        }
    }

    // ─── FILA DE INFORME ────────────────────────────────────────────────────
    private HBox crearFilaInforme(InformeActividades inf) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(9, 0, 9, 0));
        fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

        Label lblFecha  = txt(inf.getFechaRegistro() != null ? inf.getFechaRegistro().toString() : "—", 150);
        Label lblEstado = EstiloUI.badgeEstadoInforme(inf.getEstado().name());
        lblEstado.setMinWidth(120);
        Label lblSemanas = txt(String.valueOf(inf.getSemanas().size()), 80);
        Label lblHoras   = txt(String.format("%.1f h", inf.calcularHorasTotales()), 110);

        // Acciones según estado
        HBox acciones = new HBox(6);

        switch (inf.getEstado()) {
            case EN_EDICION:
                Button btnEditar = EstiloUI.botonSmall("Editar", EstiloUI.C_MEDIUM);
                btnEditar.setOnAction(e -> abrirPanelEditar(inf));
                acciones.getChildren().add(btnEditar);
                break;

            case RECHAZADO:
                // Puede devolver a edición y re-enviar
                Button btnDevolver = EstiloUI.botonSmall("Corregir", EstiloUI.C_ORANGE);
                btnDevolver.setOnAction(e -> devolverParaEdicion(inf));
                acciones.getChildren().add(btnDevolver);
                // También "Ver" para revisar contenido
                Button btnVerR = EstiloUI.botonSmall("Ver", EstiloUI.C_GRAY_DARK);
                btnVerR.setOnAction(e -> mostrarDetalleReadOnly(inf));
                acciones.getChildren().add(btnVerR);
                break;

            case ENVIADO:
            case APROBADO:
                Button btnVer = EstiloUI.botonSmall("Ver", EstiloUI.C_GRAY_DARK);
                btnVer.setOnAction(e -> mostrarDetalleReadOnly(inf));
                acciones.getChildren().add(btnVer);
                break;
        }

        fila.getChildren().addAll(lblFecha, lblEstado, lblSemanas, lblHoras, acciones);
        return fila;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PANEL DE EDICIÓN
    // ═══════════════════════════════════════════════════════════════════════

    /** Abre el panel vacío para crear un nuevo informe */
    private void abrirPanelNuevo() {
        informeEnEdicion = null;
        semanasEnMemoria.clear();
        // Agregar una semana vacía por defecto
        semanasEnMemoria.add(new SemanaActividades(1));
        renderizarPanelEdicion();
    }

    /** Abre el panel con datos de un informe EN_EDICION existente */
    private void abrirPanelEditar(InformeActividades inf) {
        informeEnEdicion = inf;
        semanasEnMemoria.clear();
        semanasEnMemoria.addAll(inf.getSemanas());
        if (semanasEnMemoria.isEmpty()) {
            semanasEnMemoria.add(new SemanaActividades(1));
        }
        renderizarPanelEdicion();
    }

    /** Renderiza el panel de edición completo */
    private void renderizarPanelEdicion() {
        panelEdicion.getChildren().clear();

        VBox card = EstiloUI.tarjeta();
        String titulo = (informeEnEdicion == null) ? "Nuevo Informe de Actividades"
                : "Editar Informe – " + informeEnEdicion.getFechaRegistro();
        card.getChildren().add(EstiloUI.labelSubtitulo(titulo));
        card.getChildren().add(EstiloUI.separador());

        // ── ScrollPane con las semanas ──
        VBox semanasContainer = new VBox(12);
        for (int i = 0; i < semanasEnMemoria.size(); i++) {
            semanasContainer.getChildren().add(
                    crearCardSemanaEditable(semanasEnMemoria.get(i), i));
        }
        card.getChildren().add(semanasContainer);

        // Botón "+ Agregar Semana"
        Button btnAgrSemana = EstiloUI.botonSecundario("+ Agregar Semana");
        btnAgrSemana.setOnAction(e -> {
            int numero = semanasEnMemoria.size() + 1;
            semanasEnMemoria.add(new SemanaActividades(numero));
            renderizarPanelEdicion();   // re-render completo
        });
        card.getChildren().add(btnAgrSemana);

        card.getChildren().add(EstiloUI.separador());

        // ── Botones acción ──
        HBox btnBar = new HBox(12);
        btnBar.setAlignment(Pos.CENTER_LEFT);

        Button btnGuardar = EstiloUI.botonPrimario("Guardar");
        btnGuardar.setOnAction(e -> guardarInforme());

        Button btnEnviar = EstiloUI.botonPrimario("Enviar al Director");
        btnEnviar.setStyle(btnEnviar.getStyle().replace(EstiloUI.C_MEDIUM, EstiloUI.C_DARK));
        btnEnviar.setOnAction(e -> enviarInforme());

        Button btnCancelar = EstiloUI.botonSecundario("Cancelar");
        btnCancelar.setOnAction(e -> {
            panelEdicion.getChildren().clear();
            informeEnEdicion = null;
            semanasEnMemoria.clear();
        });

        btnBar.getChildren().addAll(btnGuardar, btnEnviar, btnCancelar);
        card.getChildren().add(btnBar);

        panelEdicion.getChildren().add(card);
    }

    // ─── CARD EDITABLE DE UNA SEMANA ────────────────────────────────────────
    /**
     * Genera la tarjeta de edición de una semana:
     *   – Header: "Semana N" + botón eliminar
     *   – TextArea actividad semanal
     *   – Tabla de 5 filas: DatePicker | TimePicker inicio | TimePicker salida
     *   – TextArea observaciones
     *
     * Los controles escriben directamente sobre el objeto SemanaActividades en memoria.
     */
    private VBox crearCardSemanaEditable(SemanaActividades semana, int indexEnLista) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setStyle(
                "-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";" +
                        "-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;");

        // ── Header semana ──
        HBox semHeader = new HBox(12);
        semHeader.setAlignment(Pos.CENTER_LEFT);
        semHeader.getChildren().add(EstiloUI.labelSeccion("Semana " + semana.getNumeroSemana()));
        HBox.setHgrow(semHeader.getChildren().get(0), Priority.ALWAYS);

        // Botón eliminar (no se permite eliminar la última semana)
        Button btnElim = EstiloUI.botonSmall("✕", EstiloUI.C_RED);
        btnElim.setOnAction(e -> {
            if (semanasEnMemoria.size() <= 1) {
                EstiloUI.alertaError("Validación", "El informe debe tener al menos una semana.").showAndWait();
                return;
            }
            semanasEnMemoria.remove(indexEnLista);
            // Re-numerar
            for (int i = 0; i < semanasEnMemoria.size(); i++) {
                semanasEnMemoria.get(i).setNumeroSemana(i + 1);
            }
            renderizarPanelEdicion();
        });
        semHeader.getChildren().add(btnElim);
        card.getChildren().add(semHeader);

        // ── Actividad semanal ──
        TextArea taActividad = EstiloUI.crearTextArea("Descripción de actividades realizadas esta semana...");
        taActividad.setPrefHeight(70);
        if (semana.getActividadSemanal() != null) {
            taActividad.setText(semana.getActividadSemanal());
        }
        taActividad.textProperty().addListener((obs, oldV, newV) -> semana.setActividadSemanal(newV));
        card.getChildren().add(taActividad);

        // ── Tabla de días (5 filas) ──
        // Header mini-tabla
        HBox miniHeader = new HBox();
        miniHeader.setPadding(new Insets(5, 0, 5, 0));
        miniHeader.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_MID + "; -fx-border-width: 0 0 1 0;");
        String[] miniCols   = {"Día", "Fecha", "Hora Inicio", "Hora Salida"};
        double[] miniWidths = {35,    155,     140,            140};
        for (int i = 0; i < miniCols.length; i++) {
            Label l = new Label(miniCols[i]);
            l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + EstiloUI.C_GRAY_DARK + ";");
            l.setMinWidth(miniWidths[i]);
            miniHeader.getChildren().add(l);
        }
        card.getChildren().add(miniHeader);

        Date[] fechas     = semana.getFechas();
        Time[] horasInicio = semana.getHorasInicio();
        Time[] horasSalida = semana.getHorasSalida();

        for (int i = 0; i < 5; i++) {
            final int idx = i;
            HBox fila = new HBox(8);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(4, 0, 4, 0));

            // Número de día
            Label lblDia = new Label(String.valueOf(i + 1));
            lblDia.setStyle("-fx-font-size: 12px; -fx-text-fill: " + EstiloUI.C_GRAY_DARK + ";");
            lblDia.setMinWidth(35);

            // DatePicker
            DatePicker dp = new DatePicker();
            dp.setPrefWidth(155);
            dp.setStyle("-fx-font-size: 12px;");
            if (fechas[i] != null) {
                // Convertir java.util.Date a java.time.LocalDate
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(fechas[i]);
                dp.setValue(java.time.LocalDate.of(
                        cal.get(java.util.Calendar.YEAR),
                        cal.get(java.util.Calendar.MONTH) + 1, // Calendar.MONTH es 0-based
                        cal.get(java.util.Calendar.DAY_OF_MONTH)
                ));
            }
            dp.valueProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.set(newV.getYear(), newV.getMonthValue() - 1, newV.getDayOfMonth(), 0, 0, 0);
                    cal.set(java.util.Calendar.MILLISECOND, 0);
                    fechas[idx] = cal.getTime();
                } else {
                    fechas[idx] = null;
                }
            });

            // Hora Inicio – ComboBox con horas en formato HH:mm (cada 30 min)
            ComboBox<String> cboInicio = crearComboHoras();
            if (horasInicio[i] != null) {
                cboInicio.setValue(horasInicio[i].toString().substring(0, 5));
            }
            cboInicio.valueProperty().addListener((obs, oldV, newV) -> {
                horasInicio[idx] = parseTime(newV);
            });

            // Hora Salida
            ComboBox<String> cboSalida = crearComboHoras();
            if (horasSalida[i] != null) {
                cboSalida.setValue(horasSalida[i].toString().substring(0, 5));
            }
            cboSalida.valueProperty().addListener((obs, oldV, newV) -> {
                horasSalida[idx] = parseTime(newV);
            });

            fila.getChildren().addAll(lblDia, dp, cboInicio, cboSalida);
            card.getChildren().add(fila);
        }

        // ── Observaciones ──
        TextArea taObs = EstiloUI.crearTextArea("Observaciones opcionales...");
        taObs.setPrefHeight(55);
        if (semana.getObservaciones() != null) {
            taObs.setText(semana.getObservaciones());
        }
        taObs.textProperty().addListener((obs, oldV, newV) -> semana.setObservaciones(newV));
        card.getChildren().add(taObs);

        return card;
    }

    // ─── COMBO DE HORAS ─────────────────────────────────────────────────────
    /** ComboBox con horas desde 06:00 a 22:00 en intervalos de 30 minutos */
    private ComboBox<String> crearComboHoras() {
        ComboBox<String> cbo = new ComboBox<>();
        cbo.setPrefWidth(140);
        cbo.setStyle("-fx-font-size: 12px;");
        cbo.setEditable(true);  // permite escribir hora personalizada
        cbo.setPromptText("--:--");

        for (int h = 6; h <= 22; h++) {
            cbo.getItems().add(String.format("%02d:00", h));
            if (h < 22) {
                cbo.getItems().add(String.format("%02d:30", h));
            }
        }
        return cbo;
    }

    /** Parsea "HH:mm" a java.sql.Time, retorna null si es inválido */
    private Time parseTime(String valor) {
        if (valor == null || valor.trim().isEmpty() || valor.equals("--:--")) return null;
        try {
            LocalTime lt = LocalTime.parse(valor.trim());
            return Time.valueOf(lt);
        } catch (Exception e) {
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  GUARDAR / ENVIAR
    // ═══════════════════════════════════════════════════════════════════════

    /** Guarda el informe (nuevo o existente) con sus semanas */
    private void guardarInforme() {
        // Validar que al menos una semana tenga datos
        boolean hayDatos = false;
        for (SemanaActividades s : semanasEnMemoria) {
            if (s.contarDiasTrabajados() > 0 ||
                    (s.getActividadSemanal() != null && !s.getActividadSemanal().trim().isEmpty())) {
                hayDatos = true;
                break;
            }
        }
        if (!hayDatos) {
            EstiloUI.alertaError("Validación",
                    "Debe registrar al menos un día o describir una actividad en alguna semana.").showAndWait();
            return;
        }

        try {
            InformeActividadesDAO iaDAO = new InformeActividadesDAO();

            if (informeEnEdicion == null) {
                // ── Crear nuevo ──
                InformeActividades nuevo = personal.iniciarInformeActividades();
                for (SemanaActividades s : semanasEnMemoria) {
                    nuevo.agregarSemana(s);
                }
                iaDAO.guardar(nuevo);
                informeEnEdicion = nuevo;   // ahora tiene ID

                EstiloUI.alertaInfo("Éxito", "Informe guardado exitosamente.").showAndWait();
            } else {
                // ── Actualizar existente (re-guardar semanas no soportado en DAO actual,
                //     pero se persiste el estado) ──
                iaDAO.actualizar(informeEnEdicion);
                EstiloUI.alertaInfo("Éxito", "Informe actualizado.").showAndWait();
            }

            cargarInformes();   // refrescar lista

        } catch (SQLException e) {
            EstiloUI.alertaError("Error al guardar", e.getMessage()).showAndWait();
        }
    }

    /** Guarda y envía el informe al Director */
    private void enviarInforme() {
        // Primero guardar si es nuevo
        if (informeEnEdicion == null) {
            guardarInforme();
            if (informeEnEdicion == null) return; // guardar falló
        }

        // Validar que tenga semanas
        if (semanasEnMemoria.isEmpty() || semanasEnMemoria.stream().allMatch(s -> s.contarDiasTrabajados() == 0)) {
            EstiloUI.alertaError("Validación",
                    "El informe debe tener al menos una semana con datos antes de enviarlo.").showAndWait();
            return;
        }

        Alert confirm = EstiloUI.alertaConfirmacion("Enviar Informe",
                "¿Desea enviar este informe al Director para revisión?\n" +
                        "Una vez enviado no podrá editarlo hasta que sea aprobado o rechazado.");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    personal.cerrarInformeActividades(informeEnEdicion);
                    EstiloUI.alertaInfo("Éxito",
                            "Informe enviado exitosamente. El Director será notificado.").showAndWait();
                    cargarInformes();
                } catch (SQLException e) {
                    EstiloUI.alertaError("Error al enviar", e.getMessage()).showAndWait();
                } catch (IllegalStateException e) {
                    EstiloUI.alertaError("Validación", e.getMessage()).showAndWait();
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  DEVOLVER RECHAZADO → EN_EDICION
    // ═══════════════════════════════════════════════════════════════════════
    private void devolverParaEdicion(InformeActividades inf) {
        Alert confirm = EstiloUI.alertaConfirmacion("Corregir Informe",
                "¿Desea devolver este informe a estado de edición para corregirlo y reenviarlo?");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    inf.devolverParaEdicion();                          // cambia estado en memoria
                    InformeActividadesDAO dao = new InformeActividadesDAO();
                    dao.actualizar(inf);                                // persiste

                    // Abre directamente en modo edición
                    abrirPanelEditar(inf);
                    cargarInformes();                                   // actualiza lista
                } catch (SQLException e) {
                    EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
                } catch (IllegalStateException e) {
                    EstiloUI.alertaError("Validación", e.getMessage()).showAndWait();
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  DETALLE READ-ONLY (para informes ENVIADO / APROBADO / RECHAZADO)
    // ═══════════════════════════════════════════════════════════════════════
    private void mostrarDetalleReadOnly(InformeActividades inf) {
        panelEdicion.getChildren().clear();
        informeEnEdicion = null;
        semanasEnMemoria.clear();

        VBox card = EstiloUI.tarjeta();
        card.getChildren().add(EstiloUI.labelSubtitulo("Detalle del Informe"));

        // Info cabecera
        HBox infoBar = new HBox(16);
        infoBar.setAlignment(Pos.CENTER_LEFT);
        infoBar.getChildren().add(EstiloUI.labelSmall(
                "Fecha: " + (inf.getFechaRegistro() != null ? inf.getFechaRegistro() : "—")));
        infoBar.getChildren().add(EstiloUI.badgeEstadoInforme(inf.getEstado().name()));
        infoBar.getChildren().add(EstiloUI.labelSmall(
                "Horas totales: " + String.format("%.1f h", inf.calcularHorasTotales())));
        card.getChildren().add(infoBar);
        card.getChildren().add(EstiloUI.separador());

        // Semanas read-only
        if (inf.getSemanas().isEmpty()) {
            card.getChildren().add(EstiloUI.labelSmall("  (sin semanas registradas)"));
        } else {
            for (SemanaActividades semana : inf.getSemanas()) {
                card.getChildren().add(crearCardSemanaReadOnly(semana));
            }
        }

        // Cerrar
        Button btnCerrar = EstiloUI.botonSecundario("Cerrar");
        btnCerrar.setOnAction(e -> panelEdicion.getChildren().clear());
        card.getChildren().add(btnCerrar);

        panelEdicion.getChildren().add(card);
    }

    /**
     * Card read-only de una semana (mismo esquema que en DirInformes)
     */
    private VBox crearCardSemanaReadOnly(SemanaActividades s) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";" +
                        "-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;");

        // Título
        HBox semHeader = new HBox(12);
        semHeader.setAlignment(Pos.CENTER_LEFT);
        semHeader.getChildren().add(EstiloUI.labelSeccion("Semana " + s.getNumeroSemana()));
        semHeader.getChildren().add(EstiloUI.labelSmall(
                s.contarDiasTrabajados() + " día(s) | " + String.format("%.1f h", s.calcularHorasTotales())));
        card.getChildren().add(semHeader);

        if (s.getActividadSemanal() != null && !s.getActividadSemanal().trim().isEmpty()) {
            card.getChildren().add(EstiloUI.labelSmall("Actividad: " + s.getActividadSemanal()));
        }
        if (s.getObservaciones() != null && !s.getObservaciones().trim().isEmpty()) {
            card.getChildren().add(EstiloUI.labelSmall("Observaciones: " + s.getObservaciones()));
        }

        // Mini-tabla días
        HBox miniHeader = new HBox();
        miniHeader.setPadding(new Insets(4, 0, 4, 0));
        miniHeader.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_MID + "; -fx-border-width: 0 0 1 0;");
        String[] miniCols   = {"Fecha", "Hora Inicio", "Hora Salida"};
        double[] miniWidths = {130,      120,            120};
        for (int i = 0; i < miniCols.length; i++) {
            Label l = new Label(miniCols[i]);
            l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + EstiloUI.C_GRAY_DARK + ";");
            l.setMinWidth(miniWidths[i]);
            miniHeader.getChildren().add(l);
        }
        card.getChildren().add(miniHeader);

        Date[] fechas      = s.getFechas();
        Time[] horasInicio = s.getHorasInicio();
        Time[] horasSalida = s.getHorasSalida();

        for (int i = 0; i < fechas.length; i++) {
            if (fechas[i] == null) continue;
            HBox fila = new HBox();
            fila.setPadding(new Insets(3, 0, 3, 0));
            fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

            Label lFecha  = miniTxt(fechas[i].toString(), 130);
            Label lInicio = miniTxt(horasInicio[i] != null ? horasInicio[i].toString().substring(0, 5) : "—", 120);
            Label lSalida = miniTxt(horasSalida[i]  != null ? horasSalida[i].toString().substring(0, 5)  : "—", 120);

            fila.getChildren().addAll(lFecha, lInicio, lSalida);
            card.getChildren().add(fila);
        }

        return card;
    }

    // ─── UTILIDADES ─────────────────────────────────────────────────────────
    private Label txt(String valor, double ancho) {
        Label l = EstiloUI.labelBody(valor);
        l.setMinWidth(ancho);
        l.setPrefWidth(ancho);
        return l;
    }

    private Label miniTxt(String valor, double ancho) {
        Label l = EstiloUI.labelSmall(valor);
        l.setMinWidth(ancho);
        l.setPrefWidth(ancho);
        return l;
    }
}