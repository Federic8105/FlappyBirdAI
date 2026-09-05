/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view.fx;

import flappyBirdAI.controller.GameController;
import flappyBirdAI.controller.GameStats;
import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.view.GameRenderer;
import flappyBirdAI.view.GameView;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Set;

public class FxGameView implements GameView {
	
	// --- Riferimenti a Componenti Esterne ---
    
	private GameController gameController;
	
	// Renderer per disegnare gli sprite dei GameObject
	private final GameRenderer<GraphicsContext, Image> spriteRenderer = new FxGameRenderer();
	
	// --- Costruttori ---

	public FxGameView(int width, int height, boolean isFullScreen) {
		
	}
	
	// --- Gestione Ciclo di Vita ---
	
	@Override
	public void setController(GameController controller) {
		this.gameController = controller;
	}
	
	@Override
	public void close() {
		
		
	}
	
	@Override
	public void exitGame() {
		
		
	}
	
	// --- Aggiornamento UI ---
	
	@Override
	public void updateDisplay(GameStats stats, Set<AbstractGameObject> vGameObj) {
		
		
	}

	@Override
	public void repaintGame() {
		
		
	}
	
	@Override
	public void startChronometerTimer() {
		
		
	}
	
	// --- Rendering e Animazioni ---
	
	@Override
	public void preloadSprites(Set<AbstractGameObject> vGameObj) {
		
		
	}
	
	@Override
	public void updateAnimations() {
		
		
	}
	
	// --- Gestione Pausa ---
	
	@Override
	public void togglePause() {
		
		
	}
	
	// --- Gestione Messaggi e Notifiche ---
	
	@Override
	public void showBlockingWarning(String headerText, String detailText) {
		
		
	}

	@Override
	public void showAutoSaveMessage(boolean success, String headerText, String errorDetail) {
		
		
	}
	
	// --- Getters per Dimensioni Pannello di Gioco ---

	@Override
	public int getGameWidth() {
		
		return 0;
	}

	@Override
	public int getGameHeight() {
		
		return 0;
	}

}