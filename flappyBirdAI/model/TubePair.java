/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Random;

public class TubePair extends AbstractGameObject {
	
	private static final Random RANDOM = new Random();
	
	// Percentuale di quanto si può spostare il buco verso l'alto o verso il basso rispetto al centro dello schermo
    private static final double HOLE_OFFSET_RATIO = 0.3;
    public static final int DIST_X_BETWEEN_TUBES = 750;
	public static final int DIST_Y_BETWEEN_TUBES = 180;
	
	public static final int WIDTH = Tube.WIDTH;
	
	private final Tube tubeUp, tubeDown;
	
	public TubePair(int x0, int gameHeight) {
        int yTubeHoleCenter = randomYTubeHoleCenter(gameHeight);
        int upperTubeHeight = yTubeHoleCenter - DIST_Y_BETWEEN_TUBES / 2;
        
        // Tubo superiore
        this.tubeUp = new Tube(x0, 0, upperTubeHeight, true);
        
        // Tubo inferiore
        this.tubeDown = new Tube(x0, upperTubeHeight + DIST_Y_BETWEEN_TUBES, gameHeight - upperTubeHeight - DIST_Y_BETWEEN_TUBES, false);
        
        this.w = TubePair.WIDTH;
        this.h = gameHeight;
        
        updateHitBox();
    }
	
	public TubePair(int x0, int gameHeight, int desiredYTubeHoleCenter) { //TODO altezza puù diventare 0
		int screenCenter = gameHeight / 2;
	    int maxHoleOffset = calcMaxHoleOffset(gameHeight);
	    
	    // Clampare l'offset desiderato entro i limiti validi per la nuova altezza,
	    // per evitare che il buco esca dai confini dello schermo
	    int desiredOffset = desiredYTubeHoleCenter - screenCenter;
	    int clampedOffset = Math.max(-maxHoleOffset, Math.min(maxHoleOffset, desiredOffset));
	    
	    int yTubeHoleCenter = screenCenter + clampedOffset;
	    int upperTubeHeight = yTubeHoleCenter - DIST_Y_BETWEEN_TUBES / 2;
		
		// Tubo superiore
		this.tubeUp = new Tube(x0, 0, upperTubeHeight, true);
		
		// Tubo inferiore
		this.tubeDown = new Tube(x0, upperTubeHeight + DIST_Y_BETWEEN_TUBES, gameHeight - upperTubeHeight - DIST_Y_BETWEEN_TUBES, false);
		
		this.w = TubePair.WIDTH;
		this.h = gameHeight;
		
		updateHitBox();
	}

	private static int calcMaxHoleOffset(int gamePanelHeight) {
    	// Calcolare offset massimo come percentuale dell'altezza
        int maxOffsetByPercentage = (int) (gamePanelHeight * HOLE_OFFSET_RATIO);
        
        // Calcolare l'offset massimo che mantiene il buco dentro i confini dello schermo
    
        // Il buco si estende DIST_Y_BETWEEN_TUBES/2 sopra e sotto il centro
        int halfHoleSize = DIST_Y_BETWEEN_TUBES / 2;
        int screenCenter = gamePanelHeight / 2;
        
        // Offset massimo verso l'alto
        // Il centro può spostarsi fino a quando la parte superiore del buco (centro - halfHoleSize) tocca il bordo superiore ( yGamePanel = 0)
        int maxOffsetUp = screenCenter - halfHoleSize;
        
        // Offset massimo verso il basso
        // Il centro può spostarsi fino a quando la parte inferiore del buco (centro + halfHoleSize) tocca il bordo inferiore (gamePanelHeight)
        int maxOffsetDown = (gamePanelHeight - screenCenter) - halfHoleSize;
        
        // Prendere il minimo tra i due per garantire che il buco non esca dai confini
        int maxOffsetByBounds = Math.min(maxOffsetUp, maxOffsetDown);
        
        // Ritornare il minimo tra l'offset basato sulla percentuale e quello basato sui confini
        return Math.min(maxOffsetByPercentage, maxOffsetByBounds);
    }
	
	private static int randomYTubeHoleCenter(int gameHeight) {
	    int maxHoleOffset = calcMaxHoleOffset(gameHeight);
	    int tubeHoleOffset = RANDOM.nextInt(- maxHoleOffset, maxHoleOffset + 1);
	    return (gameHeight / 2) + tubeHoleOffset;
	}
	
	public int getYTubeHoleCenter() {
	    return tubeUp.h + DIST_Y_BETWEEN_TUBES / 2;
	}
	
	@Override
	public boolean isAlive() {
		return tubeUp.isAlive() && tubeDown.isAlive();
	}
	
	@Override
	public void setAlive(boolean alive) {
		tubeUp.setAlive(alive);
		tubeDown.setAlive(alive);
	}
	
	@Override
	public void updateHitBox() {
		Rectangle[] hitBoxUp = tubeUp.getHitBox();
		Rectangle[] hitBoxDown = tubeDown.getHitBox();

		if (hitBox == null) {
			hitBox = new Rectangle[hitBoxUp.length + hitBoxDown.length];
		}

		// Unire le hitbox dei due tubi in un unico array
		System.arraycopy(hitBoxUp, 0, hitBox, 0, hitBoxUp.length);
		System.arraycopy(hitBoxDown, 0, hitBox, hitBoxUp.length, hitBoxDown.length);
	}
	
	@Override
	public void updateXY(double dt_s) {
		tubeUp.updateXY(dt_s);
		tubeDown.updateXY(dt_s);
		
		x = tubeUp.x; // I tubi hanno sempre la stessa x, quindi prendo quella di uno dei due
		y = 0; // La y del TubePair è sempre 0, dato che i tubi partono entrambi da y=0
		
		updateHitBox();
	}
	
	@Override
	public void draw(Graphics2D g2d) {
		tubeUp.draw(g2d);
		tubeDown.draw(g2d);
	}
	
	@Override
	public int hashCode() {
		return tubeUp.hashCode() * 31 + tubeDown.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		TubePair other = (TubePair) obj;
		return tubeUp.equals(other.tubeUp) && tubeDown.equals(other.tubeDown);
	}
	
	@Override
	public String toString() {
		return "TubePair --> " + String.join(" | ",
				"TubeUp: " + tubeUp.toString(),
				"TubeDown: " + tubeDown.toString()
		);
	}
	
}