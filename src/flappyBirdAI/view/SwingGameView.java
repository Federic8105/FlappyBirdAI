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

public class SwingGameView extends JPanel implements GameView {

	private static final long serialVersionUID = 1L;

	private final int width, height;
	private GameController gameController;
	
	// Game Objects for Rendering
    private List<AbstractGameObject> currentVGameObj;
	
    // UI Components
	private JLabel lCurrFPS, lBestLifeTime, lNGen, lNBirds, lNTubePassed, lMaxTubePassed, lAutoSave;
	private JSlider velocitySlider;
	private Timer autoSaveMessageTimer;

	public SwingGameView(int width, int height) {
		this.width = width;
		this.height = height;

		setBounds(0, 0, this.width, this.height);
		setBackground(Color.CYAN);
		setLayout(null);
		
		initUI();
	}
	
	private void initUI() {
		// FPS Label
        lCurrFPS = createStatsLabel("FPS: 0", 0, 0, 55, 20);
        
        // Best Life Time Label
        lBestLifeTime = createStatsLabel("BLT: 0.000s", 0, 20, 90, 20);
        
        // Generation Label
        lNGen = createStatsLabel("Gen: 1", width - 85, 0, 70, 20);
        
        // Birds Label
        lNBirds = createStatsLabel("Birds: 0", width - 85, 20, 70, 20);
        
        // Tubes Passed Label
        lNTubePassed = createStatsLabel("Tubes: 0", width - 85, 40, 70, 20);
        
        // Max Tubes Label
        lMaxTubePassed = createStatsLabel("Max Tubes: 0", width - 115, 60, 100, 20);
        
        // Auto Save Label
        lAutoSave = createStatsLabel("Auto-Save: ON", width - 115, 80, 100, 20);
        
        // Velocity Slider
        velocitySlider = createVelocitySlider();
        
        add(lCurrFPS);
        add(lBestLifeTime);
        add(lNGen);
        add(lNBirds);
        add(lNTubePassed);
        add(lMaxTubePassed);
        add(lAutoSave);
        add(velocitySlider);
	}
	
	private JLabel createStatsLabel(String text, int x, int y, int width, int height) {
		JLabel label = new JLabel(text);
		label.setBounds(x, y, width, height);
		label.setOpaque(true);
		label.setBackground(Color.GREEN);
		label.setForeground(Color.RED);
		label.setBorder(BorderFactory.createLineBorder(Color.RED));
		return label;
	}
	
	private JSlider createVelocitySlider() {
		JSlider sl = new JSlider(JSlider.HORIZONTAL, 1, 15, 1);
		sl.setBounds(0, height - GameController.SLIDER_HEIGHT, width - 14, GameController.SLIDER_HEIGHT);
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
        // Aggiorna UI thread-safe
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
            repaint();
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
        repaint();
    }
	
	// Paint di Ogni GameObject

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;

		for (GameObject obj : currentVGameObj) {
			obj.draw(g2d);
		}
	}
	
}