package services;

import core.Service;
import events.*;
import rendering.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * UIService - Microservice für die Benutzeroberfläche
 * Verwaltet alle UI-Komponenten im Retro-Stil
 */
public class UIService implements Service {
    private final EventBus eventBus;
    private boolean running;
    
    private JFrame frame;
    private RetroPanel mainPanel;
    private JTextField inputField;
    private JComboBox<String> typeSelector;
    
    // Aktueller Spielzustand (von Events)
    private int currentNum1 = 0, currentNum2 = 0;
    private String currentOperator = "+";
    private int currentScore = 0;
    private int currentLevel = 1;
    private int currentStreak = 0;
    private int progress = 0;
    private String feedbackText = "";
    private Color feedbackColor = Color.WHITE;
    
    private final RenderService renderService;
    private final TaskService taskService;
    private final AudioService audioService;
    
    public UIService(RenderService renderService, TaskService taskService, AudioService audioService) {
        this.eventBus = EventBus.getInstance();
        this.renderService = renderService;
        this.taskService = taskService;
        this.audioService = audioService;
    }
    
    @Override
    public void start() {
        running = true;
        
        // Event Subscriptions
        eventBus.subscribe(GameEvent.NEW_TASK, this::onNewTask);
        eventBus.subscribe(GameEvent.SCORE_CHANGED, this::onScoreChanged);
        eventBus.subscribe(GameEvent.LEVEL_UP, this::onLevelUp);
        eventBus.subscribe(GameEvent.ANSWER_CORRECT, e -> showFeedback("RICHTIG!", new Color(50, 255, 50)));
        eventBus.subscribe(GameEvent.ANSWER_WRONG, e -> showFeedback("FALSCH! = " + taskService.getCurrentResult(), new Color(255, 50, 50)));
        
        SwingUtilities.invokeLater(this::createUI);
        
        System.out.println("[UIService] Started");
    }
    
    @Override
    public void stop() {
        running = false;
        if (frame != null) {
            frame.dispose();
        }
        System.out.println("[UIService] Stopped");
    }
    
    @Override
    public String getName() {
        return "UIService";
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    private void createUI() {
        frame = new JFrame("🎮 RETRO MATH - 8-BIT EDITION");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 750);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.getContentPane().setBackground(Color.BLACK);
        
        mainPanel = new RetroPanel();
        frame.add(mainPanel, BorderLayout.CENTER);
        
        // Control Panel unten
        JPanel controlPanel = createControlPanel();
        frame.add(controlPanel, BorderLayout.SOUTH);
        
        frame.setVisible(true);
        
        // Render Loop starten
        Timer renderTimer = new Timer(16, e -> mainPanel.repaint()); // ~60 FPS
        renderTimer.start();
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        panel.setBackground(new Color(20, 20, 30));
        panel.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, new Color(100, 100, 150)));
        panel.setPreferredSize(new Dimension(1000, 80));
        
        // Typ-Auswahl
        JLabel typeLabel = new JLabel("MODUS:");
        typeLabel.setForeground(new Color(150, 150, 200));
        typeLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        
        String[] types = {"Addition", "Subtraktion", "Multiplikation", "Division", "Gemischt"};
        typeSelector = new JComboBox<>(types);
        typeSelector.setFont(new Font("Monospaced", Font.BOLD, 12));
        typeSelector.setBackground(new Color(40, 40, 60));
        typeSelector.setForeground(Color.WHITE);
        typeSelector.addActionListener(e -> {
            taskService.setTaskType((String) typeSelector.getSelectedItem());
            eventBus.publishSync(new GameEvent("REQUEST_NEW_TASK"));
            audioService.playSound("click");
        });
        
        // Eingabefeld
        JLabel inputLabel = new JLabel("ANTWORT:");
        inputLabel.setForeground(new Color(150, 150, 200));
        inputLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        
        inputField = new JTextField(8);
        inputField.setFont(new Font("Monospaced", Font.BOLD, 24));
        inputField.setBackground(new Color(30, 30, 50));
        inputField.setForeground(new Color(0, 255, 0));
        inputField.setCaretColor(new Color(0, 255, 0));
        inputField.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 0), 2));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.addActionListener(e -> submitAnswer());
        
        // Buttons
        JButton submitBtn = createRetroButton("PRÜFEN", new Color(0, 150, 0));
        submitBtn.addActionListener(e -> submitAnswer());
        
        JButton newTaskBtn = createRetroButton("NEU", new Color(0, 100, 150));
        newTaskBtn.addActionListener(e -> {
            eventBus.publishSync(new GameEvent("REQUEST_NEW_TASK"));
            audioService.playSound("click");
        });
        
        JButton soundBtn = createRetroButton("♪ SOUND", new Color(100, 50, 150));
        soundBtn.addActionListener(e -> {
            audioService.setMuted(!audioService.isMuted());
            soundBtn.setText(audioService.isMuted() ? "♪ AUS" : "♪ AN");
        });
        
        panel.add(typeLabel);
        panel.add(typeSelector);
        panel.add(Box.createHorizontalStrut(30));
        panel.add(inputLabel);
        panel.add(inputField);
        panel.add(submitBtn);
        panel.add(newTaskBtn);
        panel.add(soundBtn);
        
        return panel;
    }
    
    private JButton createRetroButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.brighter(), 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color);
            }
        });
        
        return btn;
    }
    
    private void submitAnswer() {
        String input = inputField.getText().trim();
        if (input.isEmpty()) return;
        
        try {
            int answer = Integer.parseInt(input);
            boolean correct = taskService.checkAnswer(answer);
            
            if (correct) {
                eventBus.publishSync(new GameEvent(GameEvent.ANSWER_CORRECT));
                // Neue Aufgabe nach kurzer Pause
                Timer timer = new Timer(1200, e -> {
                    eventBus.publishSync(new GameEvent("REQUEST_NEW_TASK"));
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                eventBus.publishSync(new GameEvent(GameEvent.ANSWER_WRONG));
            }
            
            inputField.setText("");
            inputField.requestFocus();
            
        } catch (NumberFormatException e) {
            showFeedback("NUR ZAHLEN!", new Color(255, 200, 0));
        }
    }
    
    private void onNewTask(GameEvent event) {
        currentNum1 = event.getInt("num1");
        currentNum2 = event.getInt("num2");
        currentOperator = event.getString("operator");
        feedbackText = "";
    }
    
    private void onScoreChanged(GameEvent event) {
        currentScore = event.getInt("score");
        currentLevel = event.getInt("level");
        currentStreak = event.getInt("streak");
        progress = event.getInt("progress");
    }
    
    private void onLevelUp(GameEvent event) {
        currentLevel = event.getInt("level");
        showFeedback("LEVEL UP! → " + currentLevel, new Color(255, 215, 0));
    }
    
    private void showFeedback(String text, Color color) {
        feedbackText = text;
        feedbackColor = color;
    }
    
    /**
     * Haupt-Render-Panel mit 8-bit Grafik
     */
    class RetroPanel extends JPanel {
        private int scanlineOffset = 0;
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Keine Antialiasing für Pixelart-Look
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            
            // Hintergrund
            g2d.setColor(new Color(15, 15, 25));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // 3D Szene rendern
            renderService.render(g2d);
            
            // UI Overlay
            drawUI(g2d);
            
            // CRT Scanline Effekt
            drawScanlines(g2d);
            
            // Vignette Effekt
            drawVignette(g2d);
        }
        
        private void drawUI(Graphics2D g2d) {
            int centerX = getWidth() / 2;
            
            // Titel
            RetroFont.drawGlowText(g2d, "RETRO MATH", centerX - RetroFont.getTextWidth("RETRO MATH", 4) / 2, 20, 4, new Color(0, 200, 255));
            
            // Score Panel (links oben)
            drawPanel(g2d, 20, 70, 180, 100);
            RetroFont.drawText(g2d, "PUNKTE", 35, 85, 2, new Color(150, 150, 200));
            RetroFont.drawGlowText(g2d, String.valueOf(currentScore), 35, 110, 3, new Color(255, 215, 0));
            RetroFont.drawText(g2d, "STREAK " + currentStreak, 35, 145, 2, new Color(200, 100, 255));
            
            // Level Panel (rechts oben)
            drawPanel(g2d, getWidth() - 200, 70, 180, 100);
            RetroFont.drawText(g2d, "LEVEL", getWidth() - 185, 85, 2, new Color(150, 150, 200));
            RetroFont.drawGlowText(g2d, String.valueOf(currentLevel), getWidth() - 185, 110, 4, new Color(0, 255, 100));
            
            // Progress Bar
            int barWidth = 140;
            int barX = getWidth() - 185;
            int barY = 150;
            g2d.setColor(new Color(50, 50, 70));
            g2d.fillRect(barX, barY, barWidth, 12);
            g2d.setColor(new Color(0, 200, 100));
            g2d.fillRect(barX, barY, (int)(barWidth * progress / 5.0), 12);
            g2d.setColor(new Color(100, 255, 150));
            g2d.drawRect(barX, barY, barWidth, 12);
            
            // Aufgabe (Mitte unten)
            String taskText = currentNum1 + " " + currentOperator + " " + currentNum2 + " = ?";
            int taskWidth = RetroFont.getTextWidth(taskText, 5);
            int taskY = getHeight() - 180;
            
            // Aufgaben-Hintergrund
            drawPanel(g2d, centerX - taskWidth/2 - 30, taskY - 20, taskWidth + 60, 70);
            
            // Aufgaben-Text
            RetroFont.drawGlowText(g2d, taskText, centerX - taskWidth/2, taskY, 5, Color.WHITE);
            
            // Feedback
            if (!feedbackText.isEmpty()) {
                int feedbackWidth = RetroFont.getTextWidth(feedbackText, 3);
                RetroFont.drawGlowText(g2d, feedbackText, centerX - feedbackWidth/2, taskY + 55, 3, feedbackColor);
            }
            
            // Hilfstext
            String helpText = getHelpText();
            int helpWidth = RetroFont.getTextWidth(helpText, 2);
            RetroFont.drawText(g2d, helpText, centerX - helpWidth/2, 200, 2, new Color(100, 100, 150));
        }
        
        private String getHelpText() {
            switch (currentOperator) {
                case "+": return "ZAEHLE ALLE BLOECKE";
                case "-": return "GRAUE BLOECKE ABZIEHEN";
                case "×": return "REIHEN MAL SPALTEN";
                case "÷": return "BLOECKE PRO GRUPPE";
                default: return "";
            }
        }
        
        private void drawPanel(Graphics2D g2d, int x, int y, int width, int height) {
            // Hintergrund
            g2d.setColor(new Color(20, 20, 35, 220));
            g2d.fillRect(x, y, width, height);
            
            // Rand mit Retro-Stil
            g2d.setColor(new Color(80, 80, 120));
            g2d.drawRect(x, y, width, height);
            g2d.setColor(new Color(60, 60, 90));
            g2d.drawRect(x + 2, y + 2, width - 4, height - 4);
        }
        
        private void drawScanlines(Graphics2D g2d) {
            g2d.setColor(new Color(0, 0, 0, 30));
            for (int y = scanlineOffset; y < getHeight(); y += 4) {
                g2d.drawLine(0, y, getWidth(), y);
            }
            scanlineOffset = (scanlineOffset + 1) % 4;
        }
        
        private void drawVignette(Graphics2D g2d) {
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            float radius = Math.max(getWidth(), getHeight()) * 0.8f;
            
            RadialGradientPaint vignette = new RadialGradientPaint(
                centerX, centerY, radius,
                new float[] {0.5f, 1.0f},
                new Color[] {new Color(0, 0, 0, 0), new Color(0, 0, 0, 150)}
            );
            g2d.setPaint(vignette);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    
    public JFrame getFrame() {
        return frame;
    }
}
