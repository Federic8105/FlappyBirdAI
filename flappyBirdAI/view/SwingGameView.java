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
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

public class SwingGameView extends JFrame implements GameView {

	private static final long serialVersionUID = 1L;

	private final int width, height;
	private GameController gameController;
	
	// Game Objects for Rendering
    private List<AbstractGameObject> currentVGameObj = new ArrayList<>();
    
    // Pannelli Principali
    private JPanel gamePanel, statsPanel, controlsPanel, importExportPanel;
    
    // UI Components - Statistiche e Controlli
	private JLabel lCurrFPS, lBestLifeTime, lNGen, lNBirds, lNTubePassed, lMaxTubePassed, lAutoSave;
	private JSlider velocitySlider;
	private Timer autoSaveMessageTimer;
	
	// UI Components - Import/Export
	private JButton bSaveBrain, bLoadBrain, bToggleAutoSave;
	private JTextField tfAutoSaveThreshold;
	private JLabel lAutoSaveThreshold;

	public SwingGameView(int width, int height) {
		this.width = width;
		this.height = height;
		
		initWindow();
		initPanels();
		
		setVisible(true);
	}
	
	private void initWindow() {
		setSize(width, height);
        setTitle("Flappy Bird AI");
        setIconImage(new ImageIcon(getClass().getResource("/res/FB_ICON.png")).getImage());
        getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setFocusable(false);
        setLayout(new BorderLayout());
	}
	
	private void initPanels() {
		// Inizializzare per Primo per averlo a SX
		initImportExportPanel();
		
		JPanel centralPanel = new JPanel(new BorderLayout());
		centralPanel.setPreferredSize(new Dimension(width, height));
		
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
		importExportPanel.setBackground(Color.LIGHT_GRAY);
		importExportPanel.setPreferredSize(new Dimension(300, height));
		
		// Calcolare la larghezza come percentuale della larghezza totale (20%)
	    int panelWidth = (int) (width * 0.20);
	    // Controllo Width Min
	    panelWidth = Math.max(panelWidth, 250);
	    // Controllo Width Max
	    panelWidth = Math.min(panelWidth, 400);
	    
	    importExportPanel.setPreferredSize(new Dimension(panelWidth, height));
		
		// Titolo del Pannello
		TitledBorder importExportTitle = BorderFactory.createTitledBorder("Import/Export Cervelli dei Volatili");
		importExportTitle.setTitleJustification(TitledBorder.CENTER);
		importExportTitle.setTitlePosition(TitledBorder.TOP);
		importExportTitle.setTitleFont(new Font("Arial", Font.BOLD, 14));
		importExportTitle.setTitleColor(Color.BLACK);
		importExportPanel.setBorder(importExportTitle);
		
		initImportExportUI(panelWidth);
		
		add(importExportPanel, BorderLayout.WEST);
	}
	
	private void initImportExportUI(int importExportPanelWidth) {
	    int componentsWidth = importExportPanelWidth - 20; // Lascia un margine di 10px per lato
	    
		// Spacing
		importExportPanel.add(Box.createVerticalStrut(20));
		
		// Bottone Salva Cervello
		bSaveBrain = new JButton("Salva Cervello Migliore");
		bSaveBrain.setAlignmentX(Component.CENTER_ALIGNMENT);
		bSaveBrain.setMaximumSize(new Dimension(componentsWidth, 30));
		bSaveBrain.setBackground(Color.GREEN);
		bSaveBrain.setForeground(Color.BLACK);
		bSaveBrain.setFont(new Font("Arial", Font.BOLD, 12));
		bSaveBrain.addActionListener(new SaveBrainListener());
		importExportPanel.add(bSaveBrain);
		
		importExportPanel.add(Box.createVerticalStrut(20));
		
		// Sezione Auto-Save Threshold
		JPanel thresholdPanel = new JPanel();
		thresholdPanel.setLayout(new BoxLayout(thresholdPanel, BoxLayout.Y_AXIS));
		thresholdPanel.setBackground(Color.LIGHT_GRAY);
		thresholdPanel.setMaximumSize(new Dimension(componentsWidth, 80));
		
		lAutoSaveThreshold = new JLabel("Auto-Save Soglia (generazioni):");
		lAutoSaveThreshold.setAlignmentX(Component.CENTER_ALIGNMENT);
		lAutoSaveThreshold.setFont(new Font("Arial", Font.PLAIN, 11));
		thresholdPanel.add(lAutoSaveThreshold);
		
		thresholdPanel.add(Box.createVerticalStrut(5));
		
		tfAutoSaveThreshold = new JTextField("50");
		tfAutoSaveThreshold.setMaximumSize(new Dimension(100, 25));
		tfAutoSaveThreshold.setHorizontalAlignment(JTextField.CENTER);
		tfAutoSaveThreshold.setFont(new Font("Arial", Font.PLAIN, 12));
		tfAutoSaveThreshold.addActionListener(new ThresholdChangeListener());
		thresholdPanel.add(tfAutoSaveThreshold);
		
		importExportPanel.add(thresholdPanel);
		
		importExportPanel.add(Box.createVerticalStrut(15));
		
		// Bottone Toggle Auto-Save
		bToggleAutoSave = new JButton("Disattiva Auto-Save");
		bToggleAutoSave.setAlignmentX(Component.CENTER_ALIGNMENT);
		bToggleAutoSave.setMaximumSize(new Dimension(componentsWidth, 30));
		bToggleAutoSave.setBackground(Color.ORANGE);
		bToggleAutoSave.setForeground(Color.BLACK);
		bToggleAutoSave.setFont(new Font("Arial", Font.BOLD, 12));
		bToggleAutoSave.addActionListener(new ToggleAutoSaveListener());
		importExportPanel.add(bToggleAutoSave);
		
		importExportPanel.add(Box.createVerticalStrut(20));
		
		// Bottone Carica Cervello
		bLoadBrain = new JButton("Carica Cervello da File");
		bLoadBrain.setAlignmentX(Component.CENTER_ALIGNMENT);
		bLoadBrain.setMaximumSize(new Dimension(componentsWidth, 30));
		bLoadBrain.setBackground(Color.CYAN);
		bLoadBrain.setForeground(Color.BLACK);
		bLoadBrain.setFont(new Font("Arial", Font.BOLD, 12));
		bLoadBrain.addActionListener(new LoadBrainListener());
		importExportPanel.add(bLoadBrain);
		
		// Riempire lo spazio rimanente
		importExportPanel.add(Box.createVerticalGlue());
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
        
        
        gamePanel.setBackground(Color.CYAN);
        // Calcolare l'altezza disponibile: altezza totale - altezza pannelli stats (40) e controls (GameController.SLIDER_HEIGHT)
        int availableHeight = height - 40 - GameController.SLIDER_HEIGHT;
        gamePanel.setPreferredSize(new Dimension(width, availableHeight));
        
        add(gamePanel, BorderLayout.CENTER);
    }
	
	private void initStatsPanel() {
		statsPanel = new JPanel();
		statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.X_AXIS));
		statsPanel.setBackground(Color.DARK_GRAY);
		statsPanel.setPreferredSize(new Dimension(width, 40));
		
		initStatsUI();
		
		add(statsPanel, BorderLayout.NORTH);
	}
	
	private void initControlsPanel() {
		controlsPanel = new JPanel(new BorderLayout());
		controlsPanel.setBackground(Color.decode("#800020"));
		controlsPanel.setPreferredSize(new Dimension(width, GameController.SLIDER_HEIGHT));
		
		initControlsUI();
		
		add(controlsPanel, BorderLayout.SOUTH);
	}
	
	private void initStatsUI() {
		
		// Statistiche a DX
		JPanel rightStatsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		rightStatsPanel.setBackground(Color.DARK_GRAY);
		
		// Statistiche a SX
		JPanel leftStatsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		leftStatsPanel.setBackground(Color.DARK_GRAY);
		
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
		sl.setBackground(Color.decode("#800020"));
		sl.addChangeListener(_ -> {
			if (gameController != null) {
				gameController.setDtMultiplier(velocitySlider.getValue());
			}
		});

		TitledBorder sliderTitle = BorderFactory.createTitledBorder("Velocity Multiplier");
		sliderTitle.setTitleJustification(TitledBorder.CENTER);
		sliderTitle.setTitlePosition(TitledBorder.TOP);
		sliderTitle.setTitleFont(new Font("Arial", Font.BOLD, 20));
		sliderTitle.setTitleColor(Color.WHITE);
		sl.setBorder(sliderTitle);

		return sl;
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
            
            this.currentVGameObj = vGameObj;
            gamePanel.repaint();
        });
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
        return width;
    }
    
    @Override
    public int getGameHeight() {
    	// Ritornare l'altezza effettiva del pannello di gioco se è già inizializzato
        if (gamePanel != null) {
            return gamePanel.getHeight();
        }
        
        // Calcolo dell'altezza disponibile basato sulle dimensioni reali della finestra
        // altezza totale - altezza pannelli stats (40) e controls (SLIDER_HEIGHT)
        return height - 40 - GameController.SLIDER_HEIGHT;
    }
    
    @Override
    public void repaintGame() {
    	if (gamePanel != null) {
            gamePanel.repaint();
        }
    }
    
    // Inner Classes for Action Listeners for Import/Export and Auto-Save
	
    private class SaveBrainListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (gameController == null || gameController.getBestBirdBrain() == null) {
				JOptionPane.showMessageDialog(SwingGameView.this, "Nessun cervello disponibile per il salvataggio!", "Errore", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileNameExtensionFilter("File JSON", "json"));
			
			// Usare il template del controller per il nome file di default
	        String defaultFileName = gameController.generateManualSaveFileName();
	        
	        fileChooser.setSelectedFile(new File(defaultFileName));
			
			if (fileChooser.showSaveDialog(SwingGameView.this) == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				String fileName = file.getAbsolutePath();
				
				// Aggiungere estensione se non presente
				if (!fileName.toLowerCase().endsWith(".json")) {
					fileName += ".json";
				}
				
				Path filePath = Path.of(fileName);
				
				if (gameController.saveBestBrain(filePath)) {
					JOptionPane.showMessageDialog(SwingGameView.this, "Cervello salvato con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(SwingGameView.this, "Errore nel salvataggio del cervello!", "Errore", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
    
    private class LoadBrainListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (gameController == null) {
				return;
			}
			
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileNameExtensionFilter("File JSON", "json"));
			
			if (fileChooser.showOpenDialog(SwingGameView.this) == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				
				int choice = JOptionPane.showConfirmDialog(SwingGameView.this, "Caricare il cervello resetterà il gioco alla generazione 1.\nContinuare?", "Conferma Caricamento", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				
				if (choice == JOptionPane.YES_OPTION) {
					if (gameController.loadBrain(file.getAbsolutePath())) {
						JOptionPane.showMessageDialog(SwingGameView.this, "Cervello caricato con successo!\nIl gioco è stato resettato alla generazione 1.", "Successo", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(SwingGameView.this, "Errore nel caricamento del cervello!\nVerificare che il file sia valido.", "Errore", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}
	
	private class ToggleAutoSaveListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (gameController != null) {
				gameController.toggleAutoSave();
				updateAutoSaveButton();
			}
		}
	}
	
	private class ThresholdChangeListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (gameController == null) {
				return;
			}
			
			try {
				int threshold = Integer.parseInt(tfAutoSaveThreshold.getText().trim());
				if (threshold > 0) {
					gameController.setAutoSaveThreshold(threshold);
				} else {
					throw new NumberFormatException("Valore deve essere maggiore di 0");
				}
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(SwingGameView.this, "Inserire un numero valido maggiore di 0!", "Errore", JOptionPane.WARNING_MESSAGE);
				tfAutoSaveThreshold.setText(String.valueOf(gameController.getAutoSaveThreshold()));
			}
		}
	}
	
	private void updateAutoSaveButton() {
		if (gameController != null) {
			if (gameController.isAutoSaveEnabled()) {
				bToggleAutoSave.setText("Disattiva Auto-Save");
				bToggleAutoSave.setBackground(Color.ORANGE);
			} else {
				bToggleAutoSave.setText("Attiva Auto-Save");
				bToggleAutoSave.setBackground(Color.LIGHT_GRAY);
			}
		}
	}
	
}

/**
 * JTextField che accetta solo numeri interi positivi
 */
class PositiveNumericTextField extends JTextField {
	private static final long serialVersionUID = 1L;

	/**
     * Crea un PositiveNumericTextField con larghezza specificata
     * @param columns numero di colonne
     * @param maxDigits numero massimo di cifre (0 = illimitato)
     */
    public PositiveNumericTextField(int columns, int maxDigits) {
        super(columns);
        setupFilter(maxDigits);
    }
    
    /**
     * Crea un PositiveNumericTextField con larghezza specificata e senza limite di cifre
     * @param columns numero di colonne
     */
    public PositiveNumericTextField(int columns) {
        this(columns, 0);
    }
    
    /**
     * Crea un PositiveNumericTextField con valore iniziale
     * @param initialValue valore iniziale
     * @param maxDigits numero massimo di cifre
     */
    public PositiveNumericTextField(String initialValue, int maxDigits) {
        super(initialValue);
        setupFilter(maxDigits);
    }
    
    /**
     * Crea un PositiveNumericTextField senza parametri
     */
    public PositiveNumericTextField() {
        this(0, 0);
    }

    private void setupFilter(int maxDigits) {
        ((AbstractDocument) getDocument()).setDocumentFilter(new PositiveNumericDocumentFilter(maxDigits));
        
        // Centra il testo per default
        setHorizontalAlignment(JTextField.CENTER);
        
        // Tooltip informativo
        setToolTipText("Inserire solo numeri interi positivi");
    }
    
    /**
     * Ottiene il valore numerico del campo
     * @return valore numerico, 0 se vuoto o non valido
     */
    public int getIntValue() {
        String text = getText().trim();
        if (text.isEmpty()) {
            return 0;
        }
        
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Imposta il valore numerico del campo
     * @param value valore da impostare
     */
    public void setIntValue(int value) {
        if (value >= 0) {
            setText(String.valueOf(value));
        }
    }
    
    /**
     * Controlla se il campo contiene un valore valido
     * @return true se il valore è valido e maggiore di 0
     */
    public boolean hasValidValue() {
        return getIntValue() > 0;
    }
    
}

/**
 * DocumentFilter che permette solo l'inserimento di numeri interi positivi
 */
class PositiveNumericDocumentFilter extends DocumentFilter {
    private final int maxDigits;
    
    /**
     * @param maxDigits numero massimo di cifre consentite (0 = illimitato)
     */
    public PositiveNumericDocumentFilter(int maxDigits) {
        this.maxDigits = maxDigits;
    }
    
    public PositiveNumericDocumentFilter() {
        this(0); // Nessun limite
    }
    
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) 
            throws BadLocationException {
        if (isValidInput(fb, offset, string, 0)) {
            super.insertString(fb, offset, string, attr);
        }
    }
    
    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
            throws BadLocationException {
        if (isValidInput(fb, offset, text, length)) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
    
    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        super.remove(fb, offset, length);
    }
    
    private boolean isValidInput(FilterBypass fb, int offset, String text, int replaceLength) 
            throws BadLocationException {
        // Permetti stringa vuota
        if (text == null || text.isEmpty()) {
            return true;
        }
        
        // Controlla se contiene solo cifre
        if (!text.matches("\\d+")) {
            return false;
        }
        
        // Calcola la lunghezza risultante dopo l'operazione
        String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
        String resultText = currentText.substring(0, offset) + text + 
                           currentText.substring(offset + replaceLength);
        
        // Controlla il limite di cifre se specificato
        if (maxDigits > 0 && resultText.length() > maxDigits) {
            return false;
        }
        
        // Controlla che il numero risultante non sia troppo grande per un int
        if (!resultText.isEmpty()) {
            try {
                long value = Long.parseLong(resultText);
                return value <= Integer.MAX_VALUE;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return true;
    }
}