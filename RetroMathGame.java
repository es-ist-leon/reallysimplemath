import core.*;
import services.*;
import events.*;
import javax.swing.*;

/**
 * RetroMathGame - Hauptklasse
 * 
 * 8-BIT MATHE-LERNSPIEL mit Microservices-Architektur
 * 
 * Architektur:
 * ============
 * 
 *                    ┌─────────────────────┐
 *                    │   ServiceManager    │
 *                    └──────────┬──────────┘
 *                               │
 *          ┌────────────────────┼────────────────────┐
 *          │                    │                    │
 *    ┌─────┴─────┐        ┌─────┴─────┐        ┌─────┴─────┐
 *    │ TaskService│        │ ScoreService│        │AudioService│
 *    │ (Aufgaben) │        │  (Punkte)   │        │  (Sound)   │
 *    └─────┬─────┘        └─────┬─────┘        └─────┬─────┘
 *          │                    │                    │
 *          └────────────────────┼────────────────────┘
 *                               │
 *                    ┌──────────┴──────────┐
 *                    │      EventBus       │
 *                    │  (Message Broker)   │
 *                    └──────────┬──────────┘
 *                               │
 *          ┌────────────────────┴────────────────────┐
 *          │                                         │
 *    ┌─────┴─────┐                            ┌─────┴─────┐
 *    │RenderService│                            │ UIService │
 *    │  (3D 8-bit) │                            │   (GUI)   │
 *    └───────────┘                            └───────────┘
 * 
 * Features:
 * - Isometrische 3D-Grafik im 8-bit Stil
 * - Event-driven Kommunikation zwischen Services
 * - Chiptune Sound-Effekte
 * - Level- und Punktesystem
 * - CRT-Scanline Effekte
 */
public class RetroMathGame {
    
    private final ServiceManager serviceManager;
    private final EventBus eventBus;
    
    public RetroMathGame() {
        this.eventBus = EventBus.getInstance();
        this.serviceManager = new ServiceManager();
        
        initializeServices();
    }
    
    private void initializeServices() {
        System.out.println("\n" +
            "╔══════════════════════════════════════════════════════════╗\n" +
            "║                                                          ║\n" +
            "║   ██████╗ ███████╗████████╗██████╗  ██████╗              ║\n" +
            "║   ██╔══██╗██╔════╝╚══██╔══╝██╔══██╗██╔═══██╗             ║\n" +
            "║   ██████╔╝█████╗     ██║   ██████╔╝██║   ██║             ║\n" +
            "║   ██╔══██╗██╔══╝     ██║   ██╔══██╗██║   ██║             ║\n" +
            "║   ██║  ██║███████╗   ██║   ██║  ██║╚██████╔╝             ║\n" +
            "║   ╚═╝  ╚═╝╚══════╝   ╚═╝   ╚═╝  ╚═╝ ╚═════╝              ║\n" +
            "║                                                          ║\n" +
            "║   ███╗   ███╗ █████╗ ████████╗██╗  ██╗                   ║\n" +
            "║   ████╗ ████║██╔══██╗╚══██╔══╝██║  ██║                   ║\n" +
            "║   ██╔████╔██║███████║   ██║   ███████║                   ║\n" +
            "║   ██║╚██╔╝██║██╔══██║   ██║   ██╔══██║                   ║\n" +
            "║   ██║ ╚═╝ ██║██║  ██║   ██║   ██║  ██║                   ║\n" +
            "║   ╚═╝     ╚═╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝                   ║\n" +
            "║                                                          ║\n" +
            "║              8-BIT EDITION - MICROSERVICES               ║\n" +
            "╚══════════════════════════════════════════════════════════╝\n"
        );
        
        System.out.println("[RetroMathGame] Initializing services...\n");
        
        // Core Services erstellen
        TaskService taskService = new TaskService();
        ScoreService scoreService = new ScoreService();
        AudioService audioService = new AudioService();
        RenderService renderService = new RenderService(960, 500);
        
        // UI Service (benötigt andere Services)
        UIService uiService = new UIService(renderService, taskService, audioService);
        
        // Services registrieren (Reihenfolge wichtig!)
        serviceManager.registerService(taskService);
        serviceManager.registerService(scoreService);
        serviceManager.registerService(audioService);
        serviceManager.registerService(renderService);
        serviceManager.registerService(uiService);
    }
    
    public void start() {
        serviceManager.startAll();
        serviceManager.printStatus();
        
        // Spiel starten
        eventBus.publishSync(new GameEvent("REQUEST_NEW_TASK"));
        
        // Startup Sound
        AudioService audioService = serviceManager.getService("AudioService");
        if (audioService != null) {
            audioService.playSound("start");
        }
    }
    
    public void stop() {
        eventBus.shutdown();
        serviceManager.stopAll();
    }
    
    public static void main(String[] args) {
        // Look & Feel für dunkleres Theme
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Panel.background", new java.awt.Color(30, 30, 40));
            UIManager.put("Button.background", new java.awt.Color(50, 50, 70));
            UIManager.put("Button.foreground", java.awt.Color.WHITE);
            UIManager.put("TextField.background", new java.awt.Color(40, 40, 60));
            UIManager.put("TextField.foreground", java.awt.Color.WHITE);
            UIManager.put("ComboBox.background", new java.awt.Color(40, 40, 60));
            UIManager.put("ComboBox.foreground", java.awt.Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            RetroMathGame game = new RetroMathGame();
            game.start();
            
            // Shutdown Hook
            Runtime.getRuntime().addShutdownHook(new Thread(game::stop));
        });
    }
}
