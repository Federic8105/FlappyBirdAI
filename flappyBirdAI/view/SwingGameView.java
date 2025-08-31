/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.controller.GameController;
import flappyBirdAI.controller.GameStats;
import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.GameObject;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

public class SwingGameView extends JFrame implements GameView {

	private static final long serialVersionUID = 1L;
	
	// Window and UI Constants
	
	private static final String WINDOW_TITLE = "Flappy Bird AI";
    private static final String ICON_PATH = "/res/FB_ICON.png";
    
    // Panel Minimum Dimensions Constants
    public static final int MIN_STATS_PANEL_HEIGHT = 40;
    public static final int MIN_CONTROLS_PANEL_HEIGHT = 150;
    public static final int MIN_IMPORT_EXPORT_PANEL_WIDTH = 250;
    public static final int MAX_IMPORT_EXPORT_PANEL_WIDTH = 400;
    public static final int MIN_GAME_PANEL_WIDTH = 850;
    public static final int MIN_GAME_PANEL_HEIGHT = 500;
    public static final int MIN_WINDOW_WIDTH = MIN_GAME_PANEL_WIDTH + MIN_IMPORT_EXPORT_PANEL_WIDTH;
    public static final int MIN_WINDOW_HEIGHT = MIN_GAME_PANEL_HEIGHT + MIN_STATS_PANEL_HEIGHT + MIN_CONTROLS_PANEL_HEIGHT;
    
    // Colors
    private static final Color GAME_BACKGROUND = Color.CYAN;
    private static final Color STATS_BACKGROUND = Color.DARK_GRAY;
    private static final Color CONTROLS_BACKGROUND = Color.decode("#800020");
    private static final Color IMPORT_EXPORT_BACKGROUND = Color.LIGHT_GRAY;
    
    // Utility Functions
    
    static JFileChooser createJsonFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON File", "json"));
        return fileChooser;
    }

    // Initial Window Dimensions
	private final int initialWidth, initialHeight;
	
	// Controller Reference
	GameController gameController;
	
	// Game Objects for Rendering
    private List<AbstractGameObject> currentVGameObj = new ArrayList<>();
    
    // UI Panels
    private JPanel gamePanel, statsPanel, controlsPanel, importExportPanel;
    
    // UI Components - Statistiche
	private JLabel lCurrFPS, lBestLifeTime, lNGen, lNBirds, lNTubePassed, lMaxTubePassed, lAutoSave;
	private Timer autoSaveMessageTimer;
	
	// UI Components - Controls
	private JSlider velocitySlider;
	
	// UI Components - Import/Export
	private JButton bSaveBrain, bLoadBrain, bToggleAutoSave;
	private JSpinner autoSaveThresholdSpinner;
	private JLabel lAutoSaveThreshold;

	public SwingGameView(int width, int height) {
		this.initialWidth = Math.max(width, MIN_WINDOW_WIDTH);
		this.initialHeight = Math.max(height, MIN_WINDOW_HEIGHT);
		
		initWindow();
		initPanels();
		
		setVisible(true);
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
		// Inizializzare per Primo per averlo a SX
		initImportExportPanel();
		
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
	
	private void initImportExportPanel() {
		importExportPanel = new JPanel();
		importExportPanel.setLayout(new BoxLayout(importExportPanel, BoxLayout.Y_AXIS));
		importExportPanel.setBackground(IMPORT_EXPORT_BACKGROUND);
		
		// Calcolare la larghezza come percentuale della larghezza totale
	    int panelWidth = calcImportExportPanelWidth(0.2f);
	    importExportPanel.setPreferredSize(new Dimension(panelWidth, initialHeight));
	    importExportPanel.setMinimumSize(new Dimension(MIN_IMPORT_EXPORT_PANEL_WIDTH, initialHeight));
	    
		// Titolo del Pannello
		TitledBorder importExportTitle = BorderFactory.createTitledBorder("Bird Brain Import/Export");
		importExportTitle.setTitleJustification(TitledBorder.CENTER);
		importExportTitle.setTitlePosition(TitledBorder.TOP);
		importExportTitle.setTitleFont(new Font("Arial", Font.BOLD, 14));
		importExportTitle.setTitleColor(Color.BLACK);
		importExportPanel.setBorder(importExportTitle);
		
		initImportExportUI(panelWidth);
		
		add(importExportPanel, BorderLayout.WEST);
	}
	
	private int calcImportExportPanelWidth(float percOfTotWidth) {
	    // Calcolare la larghezza come percentuale della larghezza totale
		int panelWidth = (int) (initialWidth * percOfTotWidth);
	    // Controllo Width Min
	    panelWidth = Math.max(panelWidth, MIN_IMPORT_EXPORT_PANEL_WIDTH);
	    // Controllo Width Max
	    panelWidth = Math.min(panelWidth, MAX_IMPORT_EXPORT_PANEL_WIDTH);
        return panelWidth;
    }
	
	private void initGamePanel() {
        gamePanel = new JPanel() {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                if (currentVGameObj != null && !currentVGameObj.isEmpty()) {
                    for (GameObject obj : currentVGameObj) {
                        if (obj != null) {
                            obj.draw(g2d);
                        }
                    }
                }
            }
        };
        
        
        gamePanel.setBackground(GAME_BACKGROUND);
        
        // Calcolare l'altezza disponibile: altezza totale - altezza pannelli stats (GameController.STATS_HEIGHT) e controls (GameController.SLIDER_HEIGHT)
        int availableHeight = Math.max(initialHeight - MIN_STATS_PANEL_HEIGHT - MIN_CONTROLS_PANEL_HEIGHT, MIN_GAME_PANEL_HEIGHT);
        int availableWidth = Math.max(initialWidth - MIN_IMPORT_EXPORT_PANEL_WIDTH, MIN_GAME_PANEL_WIDTH);
        
        gamePanel.setPreferredSize(new Dimension(availableWidth, availableHeight));
        gamePanel.setMinimumSize(new Dimension(MIN_GAME_PANEL_WIDTH, MIN_GAME_PANEL_HEIGHT));
        
        add(gamePanel, BorderLayout.CENTER);
    }
	
	private void initStatsPanel() {
		statsPanel = new JPanel();
		statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.X_AXIS));
		statsPanel.setBackground(STATS_BACKGROUND);
		statsPanel.setPreferredSize(new Dimension(initialWidth, MIN_STATS_PANEL_HEIGHT));
		statsPanel.setMinimumSize(new Dimension(initialWidth, MIN_STATS_PANEL_HEIGHT));
		
		initStatsUI();
		
		add(statsPanel, BorderLayout.NORTH);
	}
	
	private void initControlsPanel() {
		controlsPanel = new JPanel(new BorderLayout());
		controlsPanel.setBackground(CONTROLS_BACKGROUND);
		controlsPanel.setPreferredSize(new Dimension(initialWidth, MIN_CONTROLS_PANEL_HEIGHT));
		controlsPanel.setMinimumSize(new Dimension(initialWidth, MIN_CONTROLS_PANEL_HEIGHT));
		
		initControlsUI();
		
		add(controlsPanel, BorderLayout.SOUTH);
	}
	
	private void initImportExportUI(int importExportPanelWidth) {
	    int componentsWidth = importExportPanelWidth - 20; // Lascia un margine di 10px per lato
	    
		// Spacing
		importExportPanel.add(Box.createVerticalStrut(20));
		
		// Bottone Salva Cervello
		bSaveBrain = createImportExportButton("Save Brain to File", Color.GREEN, componentsWidth);
		bSaveBrain.addActionListener(new SaveBrainListener(this));
		importExportPanel.add(bSaveBrain);
		
		importExportPanel.add(Box.createVerticalStrut(20));
		
		// Sezione Auto-Save Threshold
		JPanel thresholdPanel = new JPanel();
		thresholdPanel.setLayout(new BoxLayout(thresholdPanel, BoxLayout.Y_AXIS));
		thresholdPanel.setBackground(Color.LIGHT_GRAY);
		thresholdPanel.setMaximumSize(new Dimension(componentsWidth, 80));
		
		lAutoSaveThreshold = new JLabel("Auto-Save Threshold (Generations):");
		lAutoSaveThreshold.setAlignmentX(Component.CENTER_ALIGNMENT);
		lAutoSaveThreshold.setFont(new Font("Arial", Font.PLAIN, 11));
		thresholdPanel.add(lAutoSaveThreshold);
		
		thresholdPanel.add(Box.createVerticalStrut(5));
		
		// JSpinner con Valore di Default 50, Min 1, Max Integer.MAX_VALUE, Step 1
		autoSaveThresholdSpinner = new JSpinner(new SpinnerNumberModel(50, 1, Integer.MAX_VALUE, 1));
		autoSaveThresholdSpinner.setMaximumSize(new Dimension(100, 25));
		autoSaveThresholdSpinner.setFont(new Font("Arial", Font.PLAIN, 12));
		
		// Centrare il Testo del Campo di Input del JSpinner
		JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) autoSaveThresholdSpinner.getEditor()).getTextField();
		spinnerTextField.setHorizontalAlignment(JTextField.CENTER);
		spinnerTextField.addActionListener(_ -> validateSpinnerValue(spinnerTextField));
		
		thresholdPanel.add(autoSaveThresholdSpinner);
		
		importExportPanel.add(thresholdPanel);
		
		importExportPanel.add(Box.createVerticalStrut(15));
		
		// Bottone Toggle Auto-Save
		bToggleAutoSave = createImportExportButton("Disable Auto-Save", Color.ORANGE, componentsWidth);
		bToggleAutoSave.addActionListener(new ToggleAutoSaveListener(this));
		importExportPanel.add(bToggleAutoSave);
		
		importExportPanel.add(Box.createVerticalStrut(20));
		
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
	
	private void initStatsUI() {
		
		// Statistiche a DX
		JPanel rightStatsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		rightStatsPanel.setBackground(STATS_BACKGROUND);
		
		// Statistiche a SX
		JPanel leftStatsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		leftStatsPanel.setBackground(STATS_BACKGROUND);
		
		// FPS Label
        lCurrFPS = createStatsLabel("FPS: 0");
        
        // Best Life Time Label
        lBestLifeTime = createStatsLabel("BLT: 0.000s");
        
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
        
        leftStatsPanel.add(lCurrFPS);
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
		sl.setBackground(CONTROLS_BACKGROUND);
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
		if (gameController != null) {
			gameController.setDtMultiplier(velocitySlider.getValue());
		}
	}
	
	private void validateSpinnerValue(JFormattedTextField textField) {
	    String input = textField.getText().trim();
	    
	    try {
	        int value = Integer.parseInt(input);
	        
	        if (value < 1 || value > Integer.MAX_VALUE) {
	            throw new NumberFormatException();
	        }
	        
	        // Valore valido - aggiorna tutto
	        autoSaveThresholdSpinner.setValue(value);
	        if (gameController != null) {
	            gameController.setAutoSaveThreshold(value);
	        }
	        
	    } catch (NumberFormatException e) {
	        // Forza il ripristino del valore corretto
	        int validValue = gameController != null ? gameController.getAutoSaveThreshold() : GameStats.DEFAULT_AUTOSAVE_THRESHOLD;
	        autoSaveThresholdSpinner.setValue(validValue);
	        
	        // Forza l'aggiornamento del display
	        SwingUtilities.invokeLater(() -> {
	            textField.setValue(validValue);
	            textField.selectAll(); // seleziona tutto per facilitare la riscrittura
	        });
	    }
	}
	
	void updateAutoSaveButton() {
		if (gameController != null) {
			if (gameController.isAutoSaveEnabled()) {
				bToggleAutoSave.setText("Disable Auto-Save");
				bToggleAutoSave.setBackground(Color.ORANGE);
			} else {
				bToggleAutoSave.setText("Enable Auto-Save");
				bToggleAutoSave.setBackground(Color.LIGHT_GRAY);
			}
		}
	}
	
	// GameView Interface Methods
	
	@Override
    public void setController(GameController gameController) {
        this.gameController = gameController;
    }
	
	@Override
    public void updateDisplay(GameStats stats, List<AbstractGameObject> vGameObj) {
        // Aggiorna UI Thread-Safe
        SwingUtilities.invokeLater(() -> {
        	updateStatsLabels(stats);
            this.currentVGameObj = vGameObj;
            gamePanel.repaint();
        });
    }
	
	//TODO
	private void updateStatsLabels(GameStats stats) {
		lCurrFPS.setText("FPS: " + stats.fps);
        lBestLifeTime.setText("BLT: " + String.format("%.3f", stats.bestLifeTime) + "s");
        lNGen.setText("Gen: " + stats.nGen);
        lNBirds.setText("Birds: " + stats.nBirds);
        lNTubePassed.setText("Tubes: " + stats.nTubePassed);
        
        if (stats.nTubePassed > stats.nMaxTubePassed) {
            lMaxTubePassed.setText("Max Tubes: " + stats.nTubePassed);
        } else {
            lMaxTubePassed.setText("Max Tubes: " + stats.nMaxTubePassed);
        }
        
        lAutoSave.setText("Auto-Save: " + (stats.isAutoSaveEnabled ? "ON" : "OFF"));
        lAutoSave.setBackground(stats.isAutoSaveEnabled ? Color.GREEN : Color.GRAY);
	}
	
	@Override
    public void showAutoSaveMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            lAutoSave.setText(message);
            
            if (autoSaveMessageTimer != null) {
                autoSaveMessageTimer.stop();
            }
            
            autoSaveMessageTimer = new Timer(3000, _ -> {
                if (gameController != null) {
                    lAutoSave.setText("Auto-Save: " + (gameController.isAutoSaveEnabled() ? "ON" : "OFF"));
                }
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
	
}

// Classes for Action Listeners

class SaveBrainListener implements ActionListener {
	private final SwingGameView parentView;
	
	public SaveBrainListener(SwingGameView parentView) {
		this.parentView = parentView;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (parentView.gameController == null || parentView.gameController.getBestBirdBrain() == null) {
			JOptionPane.showMessageDialog(parentView, "No Brain Available for Saving!", "Error", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		JFileChooser fileChooser = SwingGameView.createJsonFileChooser();
		
		// Usare il template del controller per il nome file di default
        String defaultFileName = parentView.gameController.generateManualSaveFileName();
        
        fileChooser.setSelectedFile(new File(defaultFileName));
		
		if (fileChooser.showSaveDialog(parentView) == JFileChooser.APPROVE_OPTION) {
			String fileName = fileChooser.getSelectedFile().getAbsolutePath();
			
			// Aggiungere estensione se non presente
			if (!fileName.toLowerCase().endsWith(".json")) {
				fileName += ".json";
			}
			
			Path filePath = Path.of(fileName);
			
			if (parentView.gameController.saveBestBrain(filePath)) {
				JOptionPane.showMessageDialog(parentView, "Brain Saved Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(parentView, "Error Saving Brain!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}

class LoadBrainListener implements ActionListener {
	private final SwingGameView parentView;
	
	public LoadBrainListener(SwingGameView parentView) {
		this.parentView = parentView;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (parentView.gameController == null) {
			return;
		}
		
		JFileChooser fileChooser = SwingGameView.createJsonFileChooser();
		
		if (fileChooser.showOpenDialog(parentView) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			
			int choice = JOptionPane.showConfirmDialog(parentView, "Loading the Brain will Reset the Game to Generation 1.\nContinue?", "Confirm Load", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			
			if (choice == JOptionPane.YES_OPTION) {
				if (parentView.gameController.loadBrain(file.getAbsolutePath())) {
					JOptionPane.showMessageDialog(parentView, "Brain Loaded Successfully!\nGame has been Reset to Generation 1.", "Success", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(parentView, "Error Loading Brain!\nPlease Verify the File is Valid.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
}

class ToggleAutoSaveListener implements ActionListener {
	private final SwingGameView parentView;
	
	public ToggleAutoSaveListener(SwingGameView parentView) {
		this.parentView = parentView;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (parentView.gameController != null) {
			parentView.gameController.toggleAutoSave();
			parentView.updateAutoSaveButton();
		}
	}
}
