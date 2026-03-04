package com.alfredo.ui;

import com.alfredo.data.BorrarEvento;
import com.alfredo.data.Conector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class Principal extends JFrame {

    // ── Componentes de la tabla ────────────────────────────────────────────
    private JTable          tablaEventos;
    private JTextField      barraBusqueda;
    private JButton         buscarButton;
    private JButton         crearButton;
    private JButton         modificarButton;
    private JButton         abrirButton;
    private JButton         borrarButton;
    private JButton         verMapaButton;

    // ── Mapa ───────────────────────────────────────────────────────────────
    private MapaPanel       mapaPanel;
    private boolean         mapaVisible = false;

    // ── Datos ──────────────────────────────────────────────────────────────
    private final DefaultTableModel modelo  = new DefaultTableModel();
    private final Conector          db      = new Conector();

    // ── Constructor ────────────────────────────────────────────────────────
    public Principal() {
        super("Gestión de Eventos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(500, 400));

        construirUI();
        configurarEventos();
        cargarTabla();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Construcción de la UI ──────────────────────────────────────────────
    private void construirUI() {

        // Panel raíz
        JPanel root = new JPanel(new BorderLayout(0, 0));
        setContentPane(root);

        // ── Barra superior (búsqueda) ──────────────────────────────────────
        barraBusqueda  = new JTextField(20);
        buscarButton   = new JButton("Buscar");
        verMapaButton  = new JButton("🗺️ Ver Mapa");

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelBusqueda.add(new JLabel("🔍"));
        panelBusqueda.add(barraBusqueda);
        panelBusqueda.add(buscarButton);
        panelBusqueda.add(verMapaButton);
        root.add(panelBusqueda, BorderLayout.NORTH);

        // ── Panel central (tabla + mapa) ───────────────────────────────────
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.4);       // 40% tabla, 60% mapa
        splitPane.setDividerSize(5);

        // Tabla
        modelo.addColumn("ID");
        modelo.addColumn("Nombre");
        modelo.addColumn("Valor");
        modelo.addColumn("Lugar");

        tablaEventos = new JTable(modelo);
        tablaEventos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaEventos.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scrollTabla = new JScrollPane(tablaEventos);
        scrollTabla.setMinimumSize(new Dimension(300, 200));

        // Mapa
        mapaPanel = new MapaPanel();
        mapaPanel.setPreferredSize(new Dimension(500, 400));
        mapaPanel.setVisible(false); // oculto por defecto

        splitPane.setLeftComponent(scrollTabla);
        splitPane.setRightComponent(mapaPanel);
        root.add(splitPane, BorderLayout.CENTER);

        // ── Barra inferior (botones CRUD) ──────────────────────────────────
        crearButton     = new JButton("➕ Crear");
        modificarButton = new JButton("✏️ Modificar");
        abrirButton     = new JButton("👁️ Abrir");
        borrarButton    = new JButton("🗑️ Borrar");

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        panelBotones.add(crearButton);
        panelBotones.add(modificarButton);
        panelBotones.add(abrirButton);
        panelBotones.add(borrarButton);
        root.add(panelBotones, BorderLayout.SOUTH);
    }

    // ── Configurar eventos ─────────────────────────────────────────────────
    private void configurarEventos() {

        // Refrescar tabla al ganar foco
        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                cargarTabla();
                if (mapaVisible) mapaPanel.cargarEventos();
            }
        });

        // Buscar con Enter
        String actionKey = "Buscar";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), actionKey);
        getRootPane().getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accionBuscar(barraBusqueda.getText());
            }
        });

        // Botón buscar
        buscarButton.addActionListener(e ->
                accionBuscar(barraBusqueda.getText())
        );

        // Botón ver mapa
        verMapaButton.addActionListener(e -> toggleMapa());

        // Botón crear
        crearButton.addActionListener(e ->
                new ManipularEventos("Crear Evento")
        );

        // Botón modificar
        modificarButton.addActionListener(e ->
                desplegarVentanaEditar("Modificar Evento")
        );

        // Botón abrir
        abrirButton.addActionListener(e ->
                desplegarVentanaEditar("Evento:")
        );

        // Botón borrar
        borrarButton.addActionListener(e -> accionBorrar());

        // ── Callbacks del mapa ─────────────────────────────────────────────

        // Click en el mapa → no hace nada en Principal (se usa en ManipularEventos)
        mapaPanel.setOnMapClickListener((lat, lng) ->
                System.out.println("Click en mapa desde Principal: " + lat + ", " + lng)
        );

        // Click en "Ver detalle" en el popup del mapa
        mapaPanel.setOnVerDetalleListener(eventId -> {
            ManipularEventos ventana = new ManipularEventos("Evento:", eventId);
            ventana.setId(eventId);
        });
    }

    // ── Mostrar / ocultar mapa ─────────────────────────────────────────────
    private void toggleMapa() {
        mapaVisible = !mapaVisible;
        mapaPanel.setVisible(mapaVisible);

        if (mapaVisible) {
            verMapaButton.setText("❌ Ocultar Mapa");
            mapaPanel.cargarEventos();
            setSize(1100, 600);
        } else {
            verMapaButton.setText("🗺️ Ver Mapa");
            setSize(550, 500);
        }
        revalidate();
        repaint();
    }

    // ── Cargar tabla desde la API ──────────────────────────────────────────
    private void cargarTabla() {
        db.cargarDatosTabla(modelo);
    }

    // ── Buscar ─────────────────────────────────────────────────────────────
    private void accionBuscar(String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            cargarTabla();
            return;
        }
        if (db.buscarEvento(busqueda, modelo) == 0) {
            JOptionPane.showMessageDialog(this, "No se encontraron resultados.");
        }
        if (mapaVisible) mapaPanel.cargarEventos();
    }

    // ── Borrar ─────────────────────────────────────────────────────────────
    private void accionBorrar() {
        int fila = tablaEventos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un evento de la tabla.");
            return;
        }

        String id = String.valueOf(tablaEventos.getValueAt(fila, 0));
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Seguro que deseas eliminar el evento?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            new BorrarEvento(id);
            cargarTabla();
            if (mapaVisible) mapaPanel.cargarEventos();
        }
    }

    // ── Abrir ventana editar / ver ─────────────────────────────────────────
    private void desplegarVentanaEditar(String operacion) {
        int fila = tablaEventos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un evento de la tabla.");
            return;
        }
        String id = String.valueOf(tablaEventos.getValueAt(fila, 0));
        ManipularEventos ventana = new ManipularEventos(operacion, id);
        ventana.setId(id);
    }
}