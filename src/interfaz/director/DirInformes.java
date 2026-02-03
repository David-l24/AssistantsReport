package interfaz.director;

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
import java.util.Date;
import java.util.List;

/**
 * Módulo de Informes de Actividades para el Director.
 *  – Filtro por estado (todos / ENVIADO / APROBADO / RECHAZADO)
 *  – Lista informes del proyecto con nombre del personal y fecha
 *  – Seleccionar informe ENVIADO → panel detalle con semanas y botones Aprobar / Rechazar
 *  – Rechazar exige ingreso de motivo
 */
public class DirInformes extends VBox {

    private final Director director;
    private final Proyecto proyecto;

    private VBox contenidoTabla;
    private VBox panelDetalle;

    // Filtro activo
    private EstadoInforme filtroEstado = null; // null = todos

    public DirInformes(Director director, Proyecto proyecto) {
        super(18);
        this.director = director;
        this.proyecto = proyecto;
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";");
        construir();
    }

    // ─── CONSTRUCCIÓN ────────────────────────────────────────────────────────
    private void construir() {
        getChildren().clear();

        // Header + filtro
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(EstiloUI.labelTitulo("Informes de Actividades"));
        getChildren().add(header);

        // Barra de filtros
        HBox barraFiltro = new HBox(10);
        barraFiltro.setAlignment(Pos.CENTER_LEFT);
        barraFiltro.setPadding(new Insets(0, 0, 4, 0));

        Button[] filtros = {
                crearBtnFiltro("Todos",       null),
                crearBtnFiltro("Enviados",    EstadoInforme.ENVIADO),
                crearBtnFiltro("Aprobados",   EstadoInforme.APROBADO),
                crearBtnFiltro("Rechazados",  EstadoInforme.RECHAZADO)
        };
        for (Button b : filtros) barraFiltro.getChildren().add(b);
        getChildren().add(barraFiltro);

        if (proyecto == null) {
            getChildren().add(EstiloUI.labelSmall("No hay proyecto asignado."));
            return;
        }

        // Tabla de informes
        VBox tarjeta = EstiloUI.tarjeta();

        HBox tableHeader = new HBox();
        tableHeader.setPadding(new Insets(10, 0, 10, 0));
        tableHeader.setStyle("-fx-border-color: " + EstiloUI.C_DARK + "; -fx-border-width: 0 0 2 0;");
        String[] cols   = {"Personal", "Fecha Registro", "Estado", "Semanas", "Horas Total", "Acciones"};
        double[]  widths = {180,         140,              110,      80,        100,            140};
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + EstiloUI.C_GRAY_DARK + ";");
            lbl.setMinWidth(widths[i]); lbl.setPrefWidth(widths[i]);
            tableHeader.getChildren().add(lbl);
        }
        tarjeta.getChildren().add(tableHeader);

        contenidoTabla = new VBox(0);
        tarjeta.getChildren().add(contenidoTabla);
        getChildren().add(tarjeta);

        // Panel detalle (se muestra al seleccionar un informe ENVIADO)
        panelDetalle = new VBox(12);
        getChildren().add(panelDetalle);

        cargarInformes();
    }

    // ─── FILTRO ─────────────────────────────────────────────────────────────
    private Button crearBtnFiltro(String texto, EstadoInforme estado) {
        Button btn = new Button(texto);
        btn.setPrefHeight(30);
        btn.setStyle("-fx-background-color: " + EstiloUI.C_GRAY_LIGHT + ";" +
                     "-fx-text-fill: " + EstiloUI.C_GRAY_DARK + ";" +
                     "-fx-border-radius: 5;" +
                     "-fx-background-radius: 5;" +
                     "-fx-font-size: 12px;" +
                     "-fx-cursor: hand;");
        btn.setOnAction(e -> {
            filtroEstado = estado;
            // Highlight activo
            btn.setStyle("-fx-background-color: " + EstiloUI.C_MEDIUM + ";" +
                         "-fx-text-fill: white;" +
                         "-fx-border-radius: 5;" +
                         "-fx-background-radius: 5;" +
                         "-fx-font-size: 12px;" +
                         "-fx-font-weight: 600;" +
                         "-fx-cursor: hand;");
            cargarInformes();
        });
        return btn;
    }

    // ─── CARGA DE DATOS ─────────────────────────────────────────────────────
    private void cargarInformes() {
        contenidoTabla.getChildren().clear();
        panelDetalle.getChildren().clear();

        try {
            InformeActividadesDAO iaDAO = new InformeActividadesDAO();

            // Obtener todos los informes del proyecto (pendientes + resto)
            // Usamos obtenerPendientesDeRevision para ENVIADO, y obtenerPorPersonal para el resto
            // Más limpio: obtener todos los personales del proyecto y luego sus informes
            PersonalDeInvestigacionDAO piDAO = new PersonalDeInvestigacionDAO();
            List<PersonalDeInvestigacion> personales = piDAO.obtenerTodos().stream()
                    .filter(p -> p.getIdProyecto() == proyecto.getIdProyecto())
                    .collect(java.util.stream.Collectors.toList());

            boolean hayAlguno = false;
            for (PersonalDeInvestigacion p : personales) {
                List<InformeActividades> informes = iaDAO.obtenerPorPersonal(p.getCedula());
                for (InformeActividades inf : informes) {
                    // Aplicar filtro
                    if (filtroEstado != null && inf.getEstado() != filtroEstado) continue;
                    contenidoTabla.getChildren().add(crearFilaInforme(inf));
                    hayAlguno = true;
                }
            }

            if (!hayAlguno) {
                contenidoTabla.getChildren().add(EstiloUI.labelSmall("  No hay informes" +
                        (filtroEstado != null ? " con estado " + filtroEstado.name() : "") + "."));
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

        String nombre = (inf.getPersonalDeInvestigacion() != null)
                ? inf.getPersonalDeInvestigacion().getNombresCompletos() : "—";
        Label lblNombre  = txt(nombre, 180);
        Label lblFecha   = txt(inf.getFechaRegistro() != null ? inf.getFechaRegistro().toString() : "—", 140);
        Label lblEstado  = EstiloUI.badgeEstadoInforme(inf.getEstado().name());
        lblEstado.setMinWidth(110);
        Label lblSemanas = txt(String.valueOf(inf.getSemanas().size()), 80);
        Label lblHoras   = txt(String.format("%.1f h", inf.calcularHorasTotales()), 100);

        // Acciones
        HBox acciones = new HBox(6);
        if (inf.getEstado() == EstadoInforme.ENVIADO) {
            Button btnVer = EstiloUI.botonSmall("Ver", EstiloUI.C_MEDIUM);
            btnVer.setOnAction(e -> mostrarDetalle(inf));
            acciones.getChildren().add(btnVer);

            Button btnAprobar = EstiloUI.botonSmall("Aprobar", EstiloUI.C_DARK);
            btnAprobar.setOnAction(e -> aprobarInforme(inf));
            acciones.getChildren().add(btnAprobar);

            Button btnRechazar = EstiloUI.botonSmall("Rechazar", EstiloUI.C_RED);
            btnRechazar.setOnAction(e -> rechazarInforme(inf));
            acciones.getChildren().add(btnRechazar);
        } else {
            // Solo ver detalle para otros estados
            Button btnVer = EstiloUI.botonSmall("Ver", EstiloUI.C_GRAY_DARK);
            btnVer.setOnAction(e -> mostrarDetalle(inf));
            acciones.getChildren().add(btnVer);
        }

        fila.getChildren().addAll(lblNombre, lblFecha, lblEstado, lblSemanas, lblHoras, acciones);
        return fila;
    }

    // ─── PANEL DETALLE – SEMANAS ────────────────────────────────────────────
    private void mostrarDetalle(InformeActividades inf) {
        panelDetalle.getChildren().clear();

        VBox card = EstiloUI.tarjeta();

        String personNom = (inf.getPersonalDeInvestigacion() != null)
                ? inf.getPersonalDeInvestigacion().getNombresCompletos() : "—";
        card.getChildren().add(EstiloUI.labelSubtitulo("Detalle – " + personNom));
        card.getChildren().add(EstiloUI.labelSmall("Estado: " + inf.getEstado().name() +
                "  |  Fecha: " + (inf.getFechaRegistro() != null ? inf.getFechaRegistro() : "—") +
                "  |  Horas totales: " + String.format("%.1f", inf.calcularHorasTotales())));

        // Separador
        card.getChildren().add(EstiloUI.separador());

        // Cada semana como sub-tarjeta
        if (inf.getSemanas().isEmpty()) {
            card.getChildren().add(EstiloUI.labelSmall("  (sin semanas registradas)"));
        } else {
            for (SemanaActividades semana : inf.getSemanas()) {
                card.getChildren().add(crearCardSemana(semana));
            }
        }

        // Botones aprobar/rechazar si es ENVIADO
        if (inf.getEstado() == EstadoInforme.ENVIADO) {
            HBox btnBar = new HBox(12);
            btnBar.setPadding(new Insets(10, 0, 0, 0));

            Button btnAprobar  = EstiloUI.botonPrimario("Aprobar");
            btnAprobar.setOnAction(e -> aprobarInforme(inf));

            Button btnRechazar = EstiloUI.botonPeligro("Rechazar");
            btnRechazar.setOnAction(e -> rechazarInforme(inf));

            btnBar.getChildren().addAll(btnAprobar, btnRechazar);
            card.getChildren().add(btnBar);
        }

        // Cerrar
        Button btnCerrar = EstiloUI.botonSecundario("Cerrar");
        btnCerrar.setOnAction(e -> panelDetalle.getChildren().clear());
        card.getChildren().add(btnCerrar);

        panelDetalle.getChildren().add(card);
    }

    /**
     * Genera una sub-tarjeta con la información de una SemanaActividades:
     *  – Número de semana, descripción, observaciones
     *  – Tabla de días: fecha | hora inicio | hora salida
     */
    private VBox crearCardSemana(SemanaActividades s) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: " + EstiloUI.C_OFF_WHITE + ";" +
                      "-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + ";" +
                      "-fx-border-width: 1;" +
                      "-fx-border-radius: 6;" +
                      "-fx-background-radius: 6;");

        // Título semana
        HBox semHeader = new HBox(12);
        semHeader.setAlignment(Pos.CENTER_LEFT);
        semHeader.getChildren().add(EstiloUI.labelSeccion("Semana " + s.getNumeroSemana()));
        semHeader.getChildren().add(EstiloUI.labelSmall(
                s.contarDiasTrabajados() + " día(s) | " + String.format("%.1f h", s.calcularHorasTotales())));
        card.getChildren().add(semHeader);

        // Actividad
        if (s.getActividadSemanal() != null && !s.getActividadSemanal().trim().isEmpty()) {
            card.getChildren().add(EstiloUI.labelSmall("Actividad: " + s.getActividadSemanal()));
        }
        // Observaciones
        if (s.getObservaciones() != null && !s.getObservaciones().trim().isEmpty()) {
            card.getChildren().add(EstiloUI.labelSmall("Observaciones: " + s.getObservaciones()));
        }

        // Tabla de días
        Date[]  fechas     = s.getFechas();
        Time[]  horasInicio = s.getHorasInicio();
        Time[]  horasSalida = s.getHorasSalida();

        // Header mini-tabla
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

        // Filas de días
        for (int i = 0; i < fechas.length; i++) {
            if (fechas[i] == null) continue;
            HBox fila = new HBox();
            fila.setPadding(new Insets(3, 0, 3, 0));
            fila.setStyle("-fx-border-color: " + EstiloUI.C_GRAY_LIGHT + "; -fx-border-width: 0 0 1 0;");

            Label lFecha   = miniTxt(fechas[i].toString(),                               130);
            Label lInicio  = miniTxt(horasInicio[i] != null ? horasInicio[i].toString() : "—", 120);
            Label lSalida  = miniTxt(horasSalida[i]  != null ? horasSalida[i].toString()  : "—", 120);

            fila.getChildren().addAll(lFecha, lInicio, lSalida);
            card.getChildren().add(fila);
        }

        return card;
    }

    // ─── APROBAR ────────────────────────────────────────────────────────────
    private void aprobarInforme(InformeActividades inf) {
        Alert confirm = EstiloUI.alertaConfirmacion("Aprobar Informe",
                "¿Desea aprobar el informe de " +
                (inf.getPersonalDeInvestigacion() != null ? inf.getPersonalDeInvestigacion().getNombresCompletos() : "este personal") + "?");
        confirm.showAndWait().ifPresent(res -> {
            if (res == javafx.scene.control.ButtonType.OK) {
                try {
                    director.aprobarInformeDeActividades(inf);
                    EstiloUI.alertaInfo("Éxito", "Informe aprobado.").showAndWait();
                    cargarInformes();
                } catch (SQLException e) {
                    EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
                }
            }
        });
    }

    // ─── RECHAZAR ───────────────────────────────────────────────────────────
    private void rechazarInforme(InformeActividades inf) {
        TextInputDialog dialog = EstiloUI.dialogoEntrada("Rechazar Informe",
                "Indique el motivo del rechazo para " +
                (inf.getPersonalDeInvestigacion() != null ? inf.getPersonalDeInvestigacion().getNombresCompletos() : "este personal"));
        dialog.getDialogPane().setMinWidth(440);

        dialog.showAndWait().ifPresent(motivo -> {
            if (motivo.trim().isEmpty()) {
                EstiloUI.alertaError("Validación", "El motivo es obligatorio.").showAndWait();
                return;
            }
            try {
                director.rechazarInformeDeActividades(inf, motivo.trim());
                EstiloUI.alertaInfo("Éxito", "Informe rechazado. Se notificó al personal.").showAndWait();
                cargarInformes();
            } catch (SQLException e) {
                EstiloUI.alertaError("Error", e.getMessage()).showAndWait();
            }
        });
    }

    // ─── UTILIDADES ─────────────────────────────────────────────────────────
    private Label txt(String valor, double ancho) {
        Label l = EstiloUI.labelBody(valor);
        l.setMinWidth(ancho); l.setPrefWidth(ancho);
        return l;
    }

    private Label miniTxt(String valor, double ancho) {
        Label l = EstiloUI.labelSmall(valor);
        l.setMinWidth(ancho); l.setPrefWidth(ancho);
        return l;
    }
}
