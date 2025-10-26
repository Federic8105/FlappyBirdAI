/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.controller.GameController;
import flappyBirdAI.controller.GameClock;
import flappyBirdAI.controller.GameStats;
import flappyBirdAI.model.AbstractGameObject;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;

public class SwingGameView extends JFrame implements GameView, KeyListener {

	private static final long serialVersionUID = 1L;
	
	// Window and UI Constants
	
	private static final String WINDOW_TITLE = "Flappy Bird AI";
    private static final String ICON_PATH = "/res/FB_ICON.png";
    private static final String GAME_BACKGROUND_IMAGE_PATH = "/res/BACKGROUND.png";
    
    // Panel Minimum Dimensions Constants
    private static final int MIN_STATS_PANEL_WIDTH = 1000;
    private static final int MIN_STATS_PANEL_HEIGHT = 40;
    private static final int MIN_CONTROLS_PANEL_HEIGHT = 150;
    private static final int MIN_IMPORT_EXPORT_PANEL_WIDTH = 250;
    private static final int MIN_CHRONOMETER_PANEL_WIDTH = MIN_IMPORT_EXPORT_PANEL_WIDTH;
    private static final int MIN_CHRONOMETER_PANEL_HEIGHT = MIN_CONTROLS_PANEL_HEIGHT;
    private static final int MIN_GAME_PANEL_WIDTH = MIN_STATS_PANEL_WIDTH;
    private static final int MIN_GAME_PANEL_HEIGHT = 500;
    private static final int MIN_IMPORT_EXPORT_PANEL_HEIGHT = MIN_GAME_PANEL_HEIGHT + MIN_STATS_PANEL_HEIGHT;
    private static final int MIN_WINDOW_WIDTH = MIN_STATS_PANEL_WIDTH + MIN_IMPORT_EXPORT_PANEL_WIDTH;
    private static final int MIN_WINDOW_HEIGHT = MIN_GAME_PANEL_HEIGHT + MIN_STATS_PANEL_HEIGHT + MIN_CONTROLS_PANEL_HEIGHT;
    
    // Colors
    private static final Color GAME_BACKGROUND_COLOR = Color.CYAN;
    private static final Color STATS_BACKGROUND_COLOR = Color.DARK_GRAY;
    private static final Color CONTROLS_BACKGROUND_COLOR = Color.decode("#800020");
    private static final Color IMPORT_EXPORT_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    private static final Color CHRONOMETER_BACKGROUND_COLOR = Color.decode("#F0E68C");
    private static final Color PAUSE_OVERLAY_COLOR = new Color(0, 0, 0, 150);
    private static final Color PAUSE_SYMBOL_COLOR = new Color(150, 150, 150);
    
    // Pause Symbol Ratios compared to symbol size
    private static final double BAR_WIDTH_RATIO = 1.0 / 4.5;
    private static final double BAR_HEIGHT_RATIO = 0.8;
    private static final double BAR_GAP_RATIO = 1.0 / 3.0;
    
    // Utility Functions
    
    protected static JFileChooser createJsonFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON File", "json"));
        return fileChooser;
    }
    
    // Initial Window Dimensions
 	private final int initialWidth, initialHeight;
    
    // Caching Ultimi Valori di Statistica per Labels
    private int lastGen = -1, lastNBirds = -1, lastTubePassed = -1, lastMaxTubePassed = -1;
    private boolean lastAutoSaveStatus = false;
    private double lastBestLifeTime = -1.0;
	
	// Controller Reference
    // Visibilità package-private per permettere l'accessso solo alle classi dello stesso package (Classi Listeners)
	GameController gameController;
	
	// Game Objects for Rendering
    private Set<AbstractGameObject> currentVGameObj;
    
    // UI Panels
    private JPanel gamePanel, statsPanel, controlsPanel, importExportPanel, chronometerPanel;
    
    // UI Components - Statistiche
	private JLabel lFPS, lCurrLifeTime, lBestLifeTime, lNGen, lNBirds, lNTubePassed, lMaxTubePassed, lAutoSave;
	private Timer autoSaveMessageTimer;
	
	// UI Components - Controls
	private JSlider velocitySlider;
	
	// UI Components - Import/Export
	private JButton bSaveBrain, bLoadBrain;
	private JCheckBox cbAutoSaveOnGen, cbAutoSaveOnBLT, cbAutoSaveOnMaxTubePassed;
	private JSpinner autoSaveGenThresholdSpinner, autoSaveBLThresholdSpinner, autoSaveMaxTubePassedThresholdSpinner;
	
	// UI Components - Chronometer
	private JLabel lTime, lTimeValue;

	public SwingGameView(int width, int height) {
		this.initialWidth = Math.max(width, MIN_WINDOW_WIDTH);
		this.initialHeight = Math.max(height, MIN_WINDOW_HEIGHT);
		
		initWindow();
		initPanels();
		
		setupKeyListener();
		
		setVisible(true);
	}
	
	private void setupKeyListener() {
		// Aggiungere il KeyListener alla finestra principale
		addKeyListener(this);
		
		// Assicurarsi che la finestra possa ricevere eventi da tastiera
		setFocusable(true);
		
		// Richiedere il focus per input quando la finestra viene mostrata
		addWindowListener(new WindowAdapter() {
			// Chiamato quando la finestra diventa visibile (prima apertura, click, primo piano)
			@Override
			public void windowActivated(WindowEvent e) {
				requestFocusInWindow();
			}
		});
		
		// Restituire il focus quando si clicca sulla finestra in un punto qualsiasi non focusable
		addMouseListener(new MouseAdapter() {
	        @Override
	        public void mousePressed(MouseEvent e) {
	            requestFocusInWindow();
	        }
	    });
	}
	
	private void initWindow() {
		setSize(initialWidth, initialHeight);
		setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));
        setTitle(WINDOW_TITLE);
        setIconImage(new ImageIcon(getClass().getResource(ICON_PATH)).getImage());
        getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setFocusable(false);
        setLayout(new BorderLayout());
	}
	
	private void initPanels() {
		// Inizializzare per Primi per averli a SX
		initLeftPanels();
		
		JPanel centralPanel = new JPanel(new BorderLayout());
		centralPanel.setPreferredSize(new Dimension(initialWidth, initialHeight));
		
		initGamePanel();
		initStatsPanel();
		initControlsPanel();
		
		centralPanel.add(statsPanel, BorderLayout.NORTH);
	    centralPanel.add(gamePanel, BorderLayout.CENTER);
	    centralPanel.add(controlsPanel, BorderLayout.SOUTH);
	    
	    add(centralPanel, BorderLayout.CENTER);
		
		// Ridimensionare la Finestra in Base a Preferred Size dei Componenti
		pack();
	}
	
	private void initLeftPanels() {
		JPanel leftPanel = new JPanel(new BorderLayout());
		
		// Calcolare la larghezza come percentuale della larghezza totale
	    int panelWidth = calcImportExportPanelWidth(0.2f);
	    leftPanel.setPreferredSize(new Dimension(panelWidth, initialHeight));
	    leftPanel.setMinimumSize(new Dimension(MIN_IMPORT_EXPORT_PANEL_WIDTH, MIN_IMPORT_EXPORT_PANEL_HEIGHT + MIN_CHRONOMETER_PANEL_HEIGHT));
	    
	    initImportExportPanel(panelWidth);
		initChronometerPanel(panelWidth);
		
		// Aggiungere i pannelli al pannello principale di sinistra
		leftPanel.add(importExportPanel, BorderLayout.CENTER);
		leftPanel.add(chronometerPanel, BorderLayout.SOUTH);
	    
		add(leftPanel, BorderLayout.WEST);
	}
	
	private int calcImportExportPanelWidth(float percOfTotWidth) {
	    // Calcolare la larghezza come percentuale della larghezza totale
	    // Controllo Width Min
        return Math.max((int) (initialWidth * percOfTotWidth), MIN_IMPORT_EXPORT_PANEL_WIDTH);
    }
	
	private void initImportExportPanel(int panelWidth) {
		importExportPanel = new JPanel();
		importExportPanel.setLayout(new BoxLayout(importExportPanel, BoxLayout.Y_AXIS));
		importExportPanel.setBackground(IMPORT_EXPORT_BACKGROUND_COLOR);
	    importExportPanel.setMinimumSize(new Dimension(MIN_IMPORT_EXPORT_PANEL_WIDTH, MIN_IMPORT_EXPORT_PANEL_HEIGHT));
		
		// Titolo del Pannello
		TitledBorder importExportTitle = BorderFactory.createTitledBorder("Bird Brain Import/Export");
		importExportTitle.setTitleJustification(TitledBorder.CENTER);
		importExportTitle.setTitlePosition(TitledBorder.TOP);
		importExportTitle.setTitleFont(new Font("Arial", Font.BOLD, 14));
		importExportTitle.setTitleColor(Color.BLACK);
		importExportPanel.setBorder(importExportTitle);
		
		initImportExportUI(panelWidth);
	}
	
	private void initChronometerPanel(int panelWidth) {
		chronometerPanel = new JPanel();
		chronometerPanel.setLayout(new BoxLayout(chronometerPanel, BoxLayout.Y_AXIS));
		chronometerPanel.setBackground(CHRONOMETER_BACKGROUND_COLOR);
		chronometerPanel.setPreferredSize(new Dimension(panelWidth, MIN_CHRONOMETER_PANEL_HEIGHT));
		chronometerPanel.setMinimumSize(new Dimension(MIN_CHRONOMETER_PANEL_WIDTH, MIN_CHRONOMETER_PANEL_HEIGHT));
		
		// Titolo del Pannello
		TitledBorder chronometerTitle = BorderFactory.createTitledBorder("Chronometer");
		chronometerTitle.setTitleJustification(TitledBorder.CENTER);
		chronometerTitle.setTitlePosition(TitledBorder.TOP);
		chronometerTitle.setTitleFont(new Font("Arial", Font.BOLD, 14));
		chronometerTitle.setTitleColor(Color.BLACK);
		chronometerPanel.setBorder(chronometerTitle);
		
		initChronometerUI();
	}
	
	private void initGamePanel() {
		Image backgroundImg = createGameBackgroundImage();
		
        gamePanel = new JPanel() {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Disegnare l'immagine di sfondo se disponibile
                if (backgroundImg != null) {
                    g2d.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), this);
                }
                
                // Disegnare tutti gli oggetti di gioco
                if (currentVGameObj != null && !currentVGameObj.isEmpty()) {
                    for (AbstractGameObject obj : currentVGameObj) {
                        if (obj != null) {
                            obj.draw(g2d);
                        }
                    }
                }
                
                // Disegnare overlay di pausa se il gioco è in pausa
                if (!gameController.isGameRunning()) {
                    drawPauseOverlay(g2d);
                }
            }
        };
        
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Click sinistro per pausa/riprendi
                if (e.getButton() == MouseEvent.BUTTON1) {
                    gameController.togglePause();
                }
            }
        });
        
        // Se l'immagine di sfondo non è disponibile, usare un colore di sfondo
        gamePanel.setBackground(GAME_BACKGROUND_COLOR);
        gamePanel.setMinimumSize(new Dimension(MIN_GAME_PANEL_WIDTH, MIN_GAME_PANEL_HEIGHT));
        
        add(gamePanel, BorderLayout.CENTER);
    }

	private void drawPauseOverlay(Graphics2D g2d) {
	    int width = gamePanel.getWidth();
	    int height = gamePanel.getHeight();
	    
	    // Disegnare overlay scuro semi-trasparente
	    g2d.setColor(PAUSE_OVERLAY_COLOR);
	    g2d.fillRect(0, -1, width, height + 1);
	    
	    // Calcolare dimensioni del simbolo di pausa in rapporto alla dimensione del pannello
	    int symbolSize = Math.min(width, height) / 6;
	    // Centrare il simbolo di pausa
	    int symbolX = (width - symbolSize) / 2;
	    int symbolY = (height - symbolSize) / 2;

	    // Dimensioni delle barre del simbolo di pausa
	    int barWidth = (int) (symbolSize * BAR_WIDTH_RATIO);
	    int barHeight = (int) (symbolSize * BAR_HEIGHT_RATIO);
	    int barSpacing = (int) (symbolSize * BAR_GAP_RATIO);
	    
	    // Prima barra
	    int barY = symbolY + (symbolSize - barHeight) / 2;
	    int bar1X = symbolX + (symbolSize - 2 * barWidth - barSpacing) / 2;
	    RoundRectangle2D bar1 = new RoundRectangle2D.Double(bar1X, barY, barWidth, barHeight, 5, 5);
	    
	    // Seconda barra
	    int bar2X = bar1X + barWidth + barSpacing;
	    RoundRectangle2D bar2 = new RoundRectangle2D.Double(bar2X, barY, barWidth, barHeight, 5, 5);
	    
	    g2d.setColor(PAUSE_SYMBOL_COLOR);
	    g2d.fill(bar1);
	    g2d.fill(bar2);
	    
	    // Spessore del bordo delle barre
	    g2d.setStroke(new BasicStroke(1.7f));
	    // Colore del bordo delle barre
	    g2d.setColor(Color.BLACK);
	    g2d.draw(bar1);
	    g2d.draw(bar2);
	    
	    g2d.setColor(Color.WHITE);
	    g2d.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, symbolSize / 4));
	    
	    String pauseText = "PAUSED";
	    
	    // Calcolare posizione del testo sotto il simbolo di pausa
	    FontMetrics fm = g2d.getFontMetrics();
	    int textWidth = fm.stringWidth(pauseText);
	    int textX = symbolX + (symbolSize - textWidth) / 2;
	    int textY = symbolY + symbolSize + fm.getHeight();
	   
	    // Ombra del testo (testo nero leggermente spostato)
	    g2d.setColor(Color.BLACK);
	    g2d.drawString(pauseText, textX + 3, textY + 3);
	    
	    // Testo principale bianco
	    g2d.setColor(Color.WHITE);
	    g2d.drawString(pauseText, textX, textY);
	}
	
	private Image createGameBackgroundImage() {
        try {
            return ImageIO.read(getClass().getResource(GAME_BACKGROUND_IMAGE_PATH));
        } catch (IOException e) {
            System.err.println("Error Loading Game Panel Background Image: " + e.getMessage());
            return null;
        }
    }
	
	private void initStatsPanel() {
		statsPanel = new JPanel();
		statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.X_AXIS));
		statsPanel.setBackground(STATS_BACKGROUND_COLOR);
		statsPanel.setPreferredSize(new Dimension(initialWidth, MIN_STATS_PANEL_HEIGHT));
		statsPanel.setMinimumSize(new Dimension(MIN_STATS_PANEL_WIDTH, MIN_STATS_PANEL_HEIGHT));
		
		initStatsUI();
		
		add(statsPanel, BorderLayout.NORTH);
	}
	
	private void initControlsPanel() {
		controlsPanel = new JPanel(new BorderLayout());
		controlsPanel.setBackground(CONTROLS_BACKGROUND_COLOR);
		controlsPanel.setPreferredSize(new Dimension(initialWidth, MIN_CONTROLS_PANEL_HEIGHT));
		controlsPanel.setMinimumSize(new Dimension(MIN_GAME_PANEL_WIDTH, MIN_CONTROLS_PANEL_HEIGHT));
		
		initControlsUI();
		
		add(controlsPanel, BorderLayout.CENTER);
	}
	
	private void initImportExportUI(int importExportPanelWidth) {
		// Lasciare un margine di 10px per lato
	    int componentsWidth = importExportPanelWidth - 20;
	    
		// Spacing
		importExportPanel.add(Box.createVerticalStrut(20));
		
		// Bottone Salva Cervello
		bSaveBrain = createImportExportButton("Save Brain to File", Color.GREEN, componentsWidth);
		bSaveBrain.addActionListener(new SaveBrainListener(this));
		importExportPanel.add(bSaveBrain);
		
		importExportPanel.add(Box.createVerticalStrut(20));
		
		// Pannello Auto-Save Threshold
		JPanel autoSaveThresholdPanel = new JPanel();
		autoSaveThresholdPanel.setLayout(new BoxLayout(autoSaveThresholdPanel, BoxLayout.Y_AXIS));
		autoSaveThresholdPanel.setBackground(IMPORT_EXPORT_BACKGROUND_COLOR);
		autoSaveThresholdPanel.setMaximumSize(new Dimension(componentsWidth, 80));
		autoSaveThresholdPanel.setBorder(BorderFactory.createTitledBorder(
		        BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
		        "Auto-Save Settings",
		        TitledBorder.CENTER,
		        TitledBorder.TOP,
		        new Font("Arial", Font.BOLD, 12)
		));
		
		autoSaveThresholdPanel.add(Box.createVerticalStrut(5));
		
		// Auto-Save on Generation
		cbAutoSaveOnGen = createAutoSaveCheckBox("On Generation [" + GameStats.MIN_AUTOSAVE_GEN_THRESHOLD + "-" + GameStats.MAX_AUTOSAVE_GEN_THRESHOLD + "]", componentsWidth);
	    cbAutoSaveOnGen.setSelected(GameStats.DEFAULT_IS_AUTOSAVE_ON_GEN_ENABLED);
	    cbAutoSaveOnGen.addActionListener(_ -> {
            gameController.setAutoSaveOnGenEnabled(cbAutoSaveOnGen.isSelected());
            autoSaveGenThresholdSpinner.setEnabled(cbAutoSaveOnGen.isSelected());
	    });
	    autoSaveThresholdPanel.add(cbAutoSaveOnGen);
		
		autoSaveGenThresholdSpinner = new ValidatedIntegerSpinner(GameStats.MIN_AUTOSAVE_GEN_THRESHOLD, GameStats.MAX_AUTOSAVE_GEN_THRESHOLD, GameStats.DEFAULT_AUTOSAVE_GEN_THRESHOLD).withMaximumSize(100, 25);
		autoSaveGenThresholdSpinner.setEnabled(cbAutoSaveOnGen.isSelected());
		// Impostare il callback per i cambiamenti di valore validi
		((ValidatedIntegerSpinner) autoSaveGenThresholdSpinner).setOnValidValueChange(value -> {	
		    gameController.setAutoSaveGenThreshold(value);
		    // Rimettere il focus alla finestra principale
		 	requestFocusInWindow();
		});
	    
		autoSaveThresholdPanel.add(autoSaveGenThresholdSpinner);
		
		// Auto-Save on Best Life Time
		cbAutoSaveOnBLT = createAutoSaveCheckBox("On Best Life Time (s) [" + GameStats.MIN_AUTOSAVE_BLT_THRESHOLD + "-" + GameStats.MAX_AUTOSAVE_BLT_THRESHOLD + "]", componentsWidth);
	    cbAutoSaveOnBLT.setSelected(GameStats.DEFAULT_IS_AUTOSAVE_ON_BLT_ENABLED);
	    cbAutoSaveOnBLT.addActionListener(_ -> {
			gameController.setAutoSaveOnBLTEnabled(cbAutoSaveOnBLT.isSelected());
			autoSaveBLThresholdSpinner.setEnabled(cbAutoSaveOnBLT.isSelected());
	    });
	    autoSaveThresholdPanel.add(cbAutoSaveOnBLT);
	    
	    autoSaveBLThresholdSpinner = new ValidatedIntegerSpinner(GameStats.MIN_AUTOSAVE_BLT_THRESHOLD, GameStats.MAX_AUTOSAVE_BLT_THRESHOLD, GameStats.DEFAULT_AUTOSAVE_BLT_THRESHOLD).withMaximumSize(100, 25);
	    autoSaveBLThresholdSpinner.setEnabled(cbAutoSaveOnBLT.isSelected());
	    ((ValidatedIntegerSpinner) autoSaveBLThresholdSpinner).setOnValidValueChange(value -> {
	    	gameController.setAutoSaveBLTThreshold(value);
	    	requestFocusInWindow();
	    });
	    
	    autoSaveThresholdPanel.add(autoSaveBLThresholdSpinner);
	    
	    // Auto-Save on Max Tubes Passed
	    cbAutoSaveOnMaxTubePassed = createAutoSaveCheckBox("On Max Tubes Passed [" + GameStats.MIN_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD + "-" + GameStats.MAX_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD + "]", componentsWidth);
	    cbAutoSaveOnMaxTubePassed.setSelected(GameStats.DEFAULT_IS_AUTOSAVE_ON_MAX_TUBE_PASSED_ENABLED);
	    cbAutoSaveOnMaxTubePassed.addActionListener(_ -> {
			gameController.setAutoSaveOnMaxTubePassedEnabled(cbAutoSaveOnMaxTubePassed.isSelected());
			autoSaveMaxTubePassedThresholdSpinner.setEnabled(cbAutoSaveOnMaxTubePassed.isSelected());
	    });
	    autoSaveThresholdPanel.add(cbAutoSaveOnMaxTubePassed);
	    
	    autoSaveMaxTubePassedThresholdSpinner = new ValidatedIntegerSpinner(GameStats.MIN_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD, GameStats.MAX_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD, GameStats.DEFAULT_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD).withMaximumSize(100, 25);
	    autoSaveMaxTubePassedThresholdSpinner.setEnabled(cbAutoSaveOnMaxTubePassed.isSelected());
	    ((ValidatedIntegerSpinner) autoSaveMaxTubePassedThresholdSpinner).setOnValidValueChange(value -> {
	    	gameController.setAutoSaveMaxTubePassedThreshold(value);
	    	requestFocusInWindow();
	    });
	    
	    autoSaveThresholdPanel.add(autoSaveMaxTubePassedThresholdSpinner);
		
		importExportPanel.add(autoSaveThresholdPanel);
		
		importExportPanel.add(Box.createVerticalStrut(15));
		
		// Bottone Carica Cervello
		bLoadBrain = createImportExportButton("Load Brain From File", Color.CYAN, componentsWidth);
		bLoadBrain.addActionListener(new LoadBrainListener(this));
		importExportPanel.add(bLoadBrain);
		
		// Riempire lo spazio rimanente
		importExportPanel.add(Box.createVerticalGlue());
	}
	
	private JButton createImportExportButton(String text, Color backgroundColor, int width) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(width, 30));
        button.setBackground(backgroundColor);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        return button;
    }
	
	private JCheckBox createAutoSaveCheckBox(String text, int width) {
	    JCheckBox checkBox = new JCheckBox(text);
	    checkBox.setAlignmentX(Component.CENTER_ALIGNMENT);
	    checkBox.setMaximumSize(new Dimension(width, 25));
	    checkBox.setBackground(IMPORT_EXPORT_BACKGROUND_COLOR);
	    checkBox.setFont(new Font("Arial", Font.BOLD, 11));
	    checkBox.setFocusPainted(false);
	    return checkBox;
	}
	
	private void initChronometerUI() {
		// Spacing
		chronometerPanel.add(Box.createVerticalStrut(15));
		
		lTime = new JLabel("Time:");
		lTime.setAlignmentX(Component.CENTER_ALIGNMENT);
		lTime.setFont(new Font("Arial", Font.BOLD, 16));
		lTime.setForeground(Color.BLACK);
		chronometerPanel.add(lTime);
		
		// Spacing
		chronometerPanel.add(Box.createVerticalStrut(10));
		
		lTimeValue = new JLabel("00:00:00.00");
		lTimeValue.setAlignmentX(Component.CENTER_ALIGNMENT);
		lTimeValue.setFont(new Font("Courier New", Font.BOLD, 18));
		lTimeValue.setForeground(Color.DARK_GRAY);
		lTimeValue.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLoweredBevelBorder(),
			BorderFactory.createEmptyBorder(5, 10, 5, 10)
		));
		lTimeValue.setOpaque(true);
		lTimeValue.setBackground(Color.WHITE);
		chronometerPanel.add(lTimeValue);
	}

	private void initStatsUI() {
		
		// Statistiche a DX
		JPanel rightStatsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 9));
		rightStatsPanel.setBackground(STATS_BACKGROUND_COLOR);
		
		// Statistiche a SX
		JPanel leftStatsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 9));
		leftStatsPanel.setBackground(STATS_BACKGROUND_COLOR);
		
		// FPS Label
        lFPS = createStatsLabel("FPS: 0/" + GameClock.MAX_FPS);
        
        // Current Life Time Label
        lCurrLifeTime = createStatsLabel("LT: 0,00s");
        
        // Best Life Time Label
        lBestLifeTime = createStatsLabel("BLT: 0,00s");
        
        // Generation Label
        lNGen = createStatsLabel("Gen: 1");
        
        // Birds Label
        lNBirds = createStatsLabel("Birds: 0");
        
        // Tubes Passed Label
        lNTubePassed = createStatsLabel("Tubes: 0");
        
        // Max Tubes Label
        lMaxTubePassed = createStatsLabel("Max Tubes: 0");
        
        // Auto Save Label
        lAutoSave = createStatsLabel("Auto-Save: ON");
        
        // Aggiunta dei Componenti UI ai Pannelli delle Statistiche DX/SX
        rightStatsPanel.add(lNGen);
        rightStatsPanel.add(lNBirds);
        rightStatsPanel.add(lNTubePassed);
        rightStatsPanel.add(lMaxTubePassed);
        rightStatsPanel.add(lAutoSave);
        
        leftStatsPanel.add(lFPS);
        leftStatsPanel.add(lCurrLifeTime);
        leftStatsPanel.add(lBestLifeTime);
        
        // Aggiunta dei Pannelli delle Statistiche DX/SX al Pannello Principale
        statsPanel.add(leftStatsPanel);
        // Spazio Flessibile tra i 2 Pannelli
        statsPanel.add(Box.createHorizontalGlue());
        statsPanel.add(rightStatsPanel);
	}
	
	private void initControlsUI() {
		// Velocity Slider
        velocitySlider = createVelocitySlider();
        
        controlsPanel.add(velocitySlider, BorderLayout.CENTER);
	}
	
	private JLabel createStatsLabel(String text) {
		JLabel label = new JLabel(text);
		label.setOpaque(true);
		label.setBackground(Color.GREEN);
		label.setForeground(Color.RED);
		label.setBorder(BorderFactory.createLineBorder(Color.RED));
		label.setFont(new Font("Arial", Font.BOLD, 15));
		return label;
	}
	
	private JSlider createVelocitySlider() {
		JSlider sl = new JSlider(JSlider.HORIZONTAL, 1, 15, 1);
        sl.setPaintTicks(true);
        sl.setPaintLabels(true);
        sl.setSnapToTicks(true);
		sl.setMinorTickSpacing(1);
        sl.setMajorTickSpacing(1);
		sl.setBackground(CONTROLS_BACKGROUND_COLOR);
		sl.addChangeListener(_ -> handleVelocitySliderChange());

		TitledBorder sliderTitle = BorderFactory.createTitledBorder("Velocity Multiplier");
		sliderTitle.setTitleJustification(TitledBorder.CENTER);
		sliderTitle.setTitlePosition(TitledBorder.TOP);
		sliderTitle.setTitleFont(new Font("Arial", Font.BOLD, 20));
		sliderTitle.setTitleColor(Color.WHITE);
		sl.setBorder(sliderTitle);

		return sl;
	}
	
	void handleVelocitySliderChange() {
		gameController.setDtMultiplier(velocitySlider.getValue());
	}
	
	// GameView Interface Methods
	
	@Override
    public void setController(GameController gameController) {
        this.gameController = gameController;
    }
	
	@Override
    public void updateDisplay(GameClock clock, GameStats stats, Set<AbstractGameObject> vGameObj) throws NullPointerException {
		Objects.requireNonNull(clock, "Game Clock Cannot be Null");
		Objects.requireNonNull(stats, "Game Stats Cannot be Null");
		Objects.requireNonNull(vGameObj, "Game Objects List Cannot be Null");
				
		// Aggiornare UI Thread-Safe
        SwingUtilities.invokeLater(() -> {
        	updateStatsLabels(stats);
        	updateChronometerLabel(clock);
        	
            currentVGameObj = vGameObj;
            gamePanel.repaint();
        });
    }
	
	private void updateStatsLabels(GameStats stats) {
		lFPS.setText("FPS: " + stats.fps + "/" + GameClock.MAX_FPS);
        
        lCurrLifeTime.setText("LT: " + GameClock.roundAndFormatTwoDecimals(stats.currLifeTime) + "s");
		
		if (stats.bestLifeTime != lastBestLifeTime) {
			lBestLifeTime.setText("BLT: " + GameClock.roundAndFormatTwoDecimals(stats.bestLifeTime) + "s");
			lastBestLifeTime = stats.bestLifeTime;
        }
		
        if (stats.nGen != lastGen) {
        	lNGen.setText("Gen: " + stats.nGen);
            lastGen = stats.nGen;
        }
        
        if (stats.nBirds != lastNBirds) {
			lNBirds.setText("Birds: " + stats.nBirds);
			lastNBirds = stats.nBirds;
		}
        
        if (stats.nTubePassed != lastTubePassed) {
			lNTubePassed.setText("Tubes: " + stats.nTubePassed);
			lastTubePassed = stats.nTubePassed;
		}
        
        if (stats.nTubePassed != lastMaxTubePassed) {
        	lMaxTubePassed.setText("Max Tubes: " + stats.maxTubePassed);
        	lastMaxTubePassed = stats.maxTubePassed;
        }
        
        if (stats.isAutoSaveEnabled() != lastAutoSaveStatus) {
        	lAutoSave.setText("Auto-Save: " + (stats.isAutoSaveEnabled() ? "ON" : "OFF"));
            lAutoSave.setBackground(stats.isAutoSaveEnabled() ? Color.GREEN : Color.GRAY);
            lastAutoSaveStatus = stats.isAutoSaveEnabled();
		}
	}
	
	private void updateChronometerLabel(GameClock clock) {
		if (gameController.isGameRunning()) {
			lTimeValue.setText(clock.getFormattedGameTimeElapsed());
		}
	}
	
	@Override
    public void showAutoSaveMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            lAutoSave.setText(msg);
            
            if (autoSaveMessageTimer != null) {
                autoSaveMessageTimer.stop();
            }
            
            autoSaveMessageTimer = new Timer(3000, _ -> {
            	lAutoSave.setText("Auto-Save: " + (gameController.isAutoSaveEnabled() ? "ON" : "OFF"));
            });
            autoSaveMessageTimer.setRepeats(false);
            autoSaveMessageTimer.start();
        });
    }
    
    @Override
    public int getGameWidth() {
    	// Ritornare la larghezza effettiva del pannello di gioco se è già inizializzato
	    if (gamePanel != null) {
	        return gamePanel.getWidth();
	    }
	    
	    return Math.max(initialWidth - MIN_IMPORT_EXPORT_PANEL_WIDTH, MIN_GAME_PANEL_WIDTH);
    }
    
    @Override
    public int getGameHeight() {
    	// Ritornare l'altezza effettiva del pannello di gioco se è già inizializzato
        if (gamePanel != null) {
            return gamePanel.getHeight();
        }
        
        // Calcolo dell'altezza disponibile basato sulle dimensioni reali della finestra
        // altezza totale - altezza pannelli stats (GameController.STATS_HEIGHT) e controls (SLIDER_HEIGHT)
        return Math.max(initialHeight - MIN_STATS_PANEL_HEIGHT - MIN_CONTROLS_PANEL_HEIGHT, MIN_GAME_PANEL_HEIGHT);
    }
    
    @Override
    public void repaintGame() {
    	if (gamePanel != null) {
            gamePanel.repaint();
        }
    }

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			gameController.togglePause();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}
	
	@Override
	public void keyTyped(KeyEvent e) {}
	
}

// Class for Custom JSpinner to Allow Only Positive Integers

/**
 * JSpinner personalizzato che accetta solo valori interi positivi (>= 1)
 * e notifica automaticamente i cambiamenti di valore validi.
 */
class ValidatedIntegerSpinner extends JSpinner {
    
    private static final long serialVersionUID = 1L;
    
    private final int minValue;
    private final int maxValue;
    private final int defaultValue;
    private int lastValidValue;
    private Consumer<Integer> onValidValueChange;
    
    public ValidatedIntegerSpinner(int minValue, int maxValue, int defaultValue) {
        super();
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.defaultValue = Math.max(this.minValue, defaultValue);
        
        initializeSpinner();
        setupValidation();
        customizeArrowButtons();
    }
    
    public void setOnValidValueChange(Consumer<Integer> callback) {
        this.onValidValueChange = callback;
    }
   
    public int getValidatedValue() {
        return (int) getValue();
    }

    private void initializeSpinner() {
        setValue(defaultValue);
        lastValidValue = defaultValue;
        setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Centrare il testo del campo di input
        JFormattedTextField textField = ((JSpinner.DefaultEditor) getEditor()).getTextField();
        textField.setHorizontalAlignment(JTextField.CENTER);
        
        setupTextFieldValidation(textField);
    }
    
    private void setupValidation() {
        // Listener per tutti i cambiamenti di valore
        addChangeListener(_ -> validateValue());
    }
    
    private void setupTextFieldValidation(JFormattedTextField textField) {
    	// Gestire il caso quando il campo perde il focus
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateValue();
            }
        });
        
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // Ripristina l'ultimo valore valido senza validazione
                    textField.setValue(lastValidValue);
                }
                
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                	validateValue();
				}
            }
            
            @Override
            public void keyTyped(KeyEvent e) {
                // Prevenire inserimento di caratteri non numerici a parte backspace e delete
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                	// Intercettare evento e consumarlo per fermarlo
                	// Per Rimuovere anche il Beep di Default per caratteri non validi
                    e.consume();
                }
            }
        });
    }
    
    private void validateValue() {
    	// Campo contiene solo numeri, quindi parse sicuro
    	
        String valueStr = getValue().toString().trim();
       
        // Gestire campo vuoto
        if (valueStr.isEmpty()) {
            setValue(lastValidValue);
            return;
        }
        
        long longValue = Long.parseLong(valueStr);
		
		// Controllare se il valore è valido
        if (longValue < minValue || longValue > maxValue) {
			// Valore non valido, ripristina l'ultimo valore valido
			setValue(lastValidValue);
			return;
		}
        
        int value = (int) longValue;
        
        lastValidValue = value;
        
        // Notifica il callback se presente
        if (onValidValueChange != null) {
            onValidValueChange.accept(value);
        }
    }
    
    private void customizeArrowButtons() {
        // Trova e personalizza i pulsanti freccia
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                JButton arrowButton = (JButton) component;
                
                // Rimuovi tutti i listener esistenti
                ActionListener[] listeners = arrowButton.getActionListeners();
                for (ActionListener listener : listeners) {
                    arrowButton.removeActionListener(listener);
                }
                
                // Aggiungi il nostro listener personalizzato
                arrowButton.addActionListener(_ -> handleArrowButtonClick(arrowButton));
            }
        }
    }
    
    /**
     * Ottiene l'ultimo valore valido che è stato impostato
     */
    public int getLastValidValue() {
        return lastValidValue;
    }
    
    public void setLastValidValue(int value) {
		if (value >= minValue) {
			lastValidValue = value;
			setValue(value);
		}
	}
    
    /**
     * Ripristina l'ultimo valore valido
     */
    public void restoreLastValidValue() {
        setValue(lastValidValue);
    }
    
    private void handleArrowButtonClick(JButton arrowButton) {
        int currValue = getValidatedValue();
        
        // Determina la direzione basandosi sulla posizione del pulsante
        // Il pulsante "su" è generalmente il primo componente
        Component[] components = getComponents();
        boolean isUpButton = components.length > 0 && components[0] == arrowButton;
        
        if (isUpButton) {
            // Incrementa
            setValue(currValue + 1);
            lastValidValue = currValue + 1;
        } else {
            // Decrementa (ma non sotto il minimo)
        	setValue(Math.max(minValue, currValue - 1));
        	lastValidValue = Math.max(minValue, currValue - 1);
        }
    }
    
    /**
     * Metodo di utilità per creare uno spinner con dimensioni specifiche
     */
    public ValidatedIntegerSpinner withMaximumSize(int width, int height) {
        setMaximumSize(new Dimension(width, height));
        return this;
    }
}

// Classes for Action Listeners

class SaveBrainListener implements ActionListener {
	private final SwingGameView parentView;
	
	public SaveBrainListener(SwingGameView parentView) throws NullPointerException {
		this.parentView = Objects.requireNonNull(parentView, "Parent View Cannot be Null");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (parentView.gameController.getBestBirdBrain() == null) {
			JOptionPane.showMessageDialog(parentView, "No Brain Available for Saving!", "Error", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		JFileChooser fileChooser = SwingGameView.createJsonFileChooser();
		
		// Usare il template del controller per il nome file di default
        String defaultFileName = parentView.gameController.createManualSaveFileName();
        
        fileChooser.setSelectedFile(new File(defaultFileName));
		
		if (fileChooser.showSaveDialog(parentView) == JFileChooser.APPROVE_OPTION) {
			String fileName = fileChooser.getSelectedFile().getAbsolutePath();
			
			// Aggiungere estensione se non presente
			if (!fileName.toLowerCase().endsWith(".json")) {
				fileName += ".json";
			}
			
			Path filePath = Path.of(fileName);
			
			try {
                parentView.gameController.saveBestBrain(filePath);
                JOptionPane.showMessageDialog(parentView, "Brain Saved Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parentView, "Error Saving Brain: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
		}
	}
}

class LoadBrainListener implements ActionListener {
	private final SwingGameView parentView;
	
	public LoadBrainListener(SwingGameView parentView) throws NullPointerException {
		this.parentView = Objects.requireNonNull(parentView, "Parent View Cannot be Null");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = SwingGameView.createJsonFileChooser();
		
		if (fileChooser.showOpenDialog(parentView) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			
			int choice = JOptionPane.showConfirmDialog(parentView, "Loading the Brain will Reset the Game to Generation 1.\nContinue?", "Confirm Load", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			
			if (choice == JOptionPane.YES_OPTION) {
				try {
                    parentView.gameController.loadBrain(file.getAbsolutePath());
                    JOptionPane.showMessageDialog(parentView, "Brain Loaded Successfully!\nGame has been Reset to Generation 1.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(parentView, "Error Loading Brain: " + ex.getMessage() + "\nPlease Verify the File is Valid.", "Error", JOptionPane.ERROR_MESSAGE);
                }
			}
		}
	}
}