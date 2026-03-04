package com.alfredo.ui;

import com.alfredo.api.ApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class MapaPanel extends JPanel {

    private WebEngine webEngine;
    private JavaBridge bridge;

    // ── Callback hacia el exterior ─────────────────────────────────────────
    // Cuando el usuario hace click en el mapa, avisa con lat/lng
    public interface OnMapClickListener {
        void onMapClick(String lat, String lng);
    }

    // Cuando el usuario hace click en "Ver detalle" en el popup
    public interface OnVerDetalleListener {
        void onVerDetalle(String eventId);
    }

    private OnMapClickListener mapClickListener;
    private OnVerDetalleListener verDetalleListener;

    public void setOnMapClickListener(OnMapClickListener l)     { this.mapClickListener = l; }
    public void setOnVerDetalleListener(OnVerDetalleListener l) { this.verDetalleListener = l; }

    // ── Constructor ────────────────────────────────────────────────────────
    public MapaPanel() {
        setLayout(new BorderLayout());

        // JFXPanel es el puente entre Swing y JavaFX
        JFXPanel jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);

        // La UI de JavaFX debe inicializarse en el hilo de JavaFX
        Platform.runLater(() -> {
            Platform.setImplicitExit(false);
            inicializarWebView(jfxPanel);
        });
    }

    // ── Inicializar WebView en el hilo JavaFX ──────────────────────────────
    private void inicializarWebView(JFXPanel jfxPanel) {
        WebView webView = new WebView();
        webEngine = webView.getEngine();

        // Cuando el HTML termine de cargar, inyectamos el bridge
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                inyectarBridge();
            }
        });

        // Cargar el HTML desde resources
        URL url = getClass().getClassLoader().getResource("mapa.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
        } else {
            System.err.println("No se encontró mapa.html en resources");
        }

        Scene scene = new Scene(webView);
        jfxPanel.setScene(scene);
    }

    // ── Inyectar el bridge Java ↔ JavaScript ───────────────────────────────
    private void inyectarBridge() {
        bridge = new JavaBridge();

        // Exponer el objeto Java como "javabridge" en el contexto JS
        JSObject window = (JSObject) webEngine.executeScript("window");
        window.setMember("javabridge", bridge);

        System.out.println("Bridge Java ↔ JS inyectado correctamente");
    }

    // ── Cargar eventos en el mapa ──────────────────────────────────────────
    // Llamar desde Swing para pintar los marcadores
    public void cargarEventos() {
        new Thread(() -> {
            try {
                String json = ApiClient.get("/api/eventos");

                // Escapar comillas para pasarlo a JS de forma segura
                String jsonEscapado = json.replace("\\", "\\\\").replace("'", "\\'");

                // Ejecutar en el hilo de JavaFX
                Platform.runLater(() -> {
                    try {
                        webEngine.executeScript("cargarEventos('" + jsonEscapado + "')");
                    } catch (Exception e) {
                        System.err.println("Error al ejecutar JS: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                System.err.println("Error al obtener eventos: " + e.getMessage());
            }
        }).start();
    }

    // ── Centrar el mapa en coordenadas específicas ─────────────────────────
    public void centrarEn(double lat, double lng) {
        Platform.runLater(() ->
                webEngine.executeScript("centrarEn(" + lat + ", " + lng + ", 15)")
        );
    }

    // ── Bridge: objeto Java expuesto a JavaScript ──────────────────────────
    // Los métodos de esta clase pueden ser llamados desde JS
    public class JavaBridge {

        // JS llama a esto cuando el usuario hace click en el mapa
        public void onMapClick(String lat, String lng) {
            System.out.println("Click en mapa: " + lat + ", " + lng);

            // Notificar en el hilo de Swing (no JavaFX)
            SwingUtilities.invokeLater(() -> {
                if (mapClickListener != null) {
                    mapClickListener.onMapClick(lat, lng);
                }
            });
        }

        // JS llama a esto cuando el usuario hace click en "Ver detalle"
        public void onVerDetalle(String eventId) {
            System.out.println("Ver detalle evento: " + eventId);

            SwingUtilities.invokeLater(() -> {
                if (verDetalleListener != null) {
                    verDetalleListener.onVerDetalle(eventId);
                }
            });
        }
    }
}