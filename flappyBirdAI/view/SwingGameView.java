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
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class SwingGameView extends JFrame implements GameView {

	private static final long serialVersionUID = 1L;

	private final int width, height;
	private GameController gameController;
	
	// Game Objects for Rendering
    private List<AbstractGameObject> currentVGameObj = new ArrayList<>();
    
    // Pannelli Principali
    private JPanel gamePanel, statsPanel, controlsPanel;
    
    // UI Components
	private JLabel lCurrFPS, lBestLifeTime, lNGen, lNBirds, lNTubePassed, lMaxTubePassed, lAutoSave;
	private JSlider velocitySlider;
	private Timer autoSaveMessageTimer;

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
		initGamePanel();
		initStatsPanel();
		initControlsPanel();
		
		// Ridimensionare la Finestra in Base a Preferred Size dei Componenti
		pack();
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
        gamePanel.setPreferredSize(new Dimension(width, GameController.GAME_SCREEN_HEIGHT));
        
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
        return GameController.GAME_SCREEN_HEIGHT;
    }
    
    @Override
    public void repaintGame() {
    	if (gamePanel != null) {
            gamePanel.repaint();
        }
    }
	
}