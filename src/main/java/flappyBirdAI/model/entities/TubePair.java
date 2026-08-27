/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model.entities;

import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.GameObject;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class TubePair extends AbstractGameObject {
	
	private static final Random RANDOM = new Random();
	
	// Percentuale di quanto si può spostare il buco verso l'alto o verso il basso rispetto al centro dello schermo
    private static final double HOLE_OFFSET_RATIO = 0.4;
    public static final int DIST_X_BETWEEN_TUBES = 750;
	public static final int DIST_Y_BETWEEN_TUBES = 180;
	
	public static final int WIDTH = Tube.WIDTH;
	
	// Vuoti se l'altezza calcolata per quel tubo è 0 (il tubo non esiste)
	private final Optional<Tube> tubeUpOpt, tubeDownOpt;
	private final int yTubeHoleCenter;
	
	public TubePair(int x0, int gameHeight) throws IllegalStateException {
        this.yTubeHoleCenter = randomYTubeHoleCenter(gameHeight);
        
        int upperTubeHeight = Math.max(0, yTubeHoleCenter - DIST_Y_BETWEEN_TUBES / 2);
        int lowerTubeHeight = Math.max(0, gameHeight - upperTubeHeight - DIST_Y_BETWEEN_TUBES);
        
        // Se l'altezza calcolata è 0, il tubo corrispondente non esiste (Optional.empty())
        this.tubeUpOpt = upperTubeHeight > 0 ? Optional.of(new Tube(x0, 0, upperTubeHeight, true)) : Optional.empty();
        this.tubeDownOpt = lowerTubeHeight > 0 ? Optional.of(new Tube(x0, upperTubeHeight + DIST_Y_BETWEEN_TUBES, lowerTubeHeight, false)) : Optional.empty();
        
        if (tubeUpOpt.isEmpty() && tubeDownOpt.isEmpty()) {
        	throw new IllegalStateException("Window Height Too Small: No Tube Can Exist (Height: " + gameHeight + ")");
        }
        
        this.x = x0;
        // La y del TubePair è sempre 0, dato che i tubi partono entrambi da y=0
        this.y = 0;
        this.w = TubePair.WIDTH;
        this.h = gameHeight;
        
        updateHitBox();
    }
	
	// Costruttore che permette di specificare la posizione del buco come percentuale dell'altezza (0.0 - 1.0)
	public TubePair(int x0, int gameHeight, double holeRatio) throws IllegalStateException {	
		int screenCenter = gameHeight / 2;
		// valore massimo teorico di lontanza del buco dal centro dello schermo
        int maxHoleOffset = calcMaxHoleOffset(gameHeight);
        
        int desiredYTubeHoleCenter = (int) Math.round(holeRatio * gameHeight);
        int desiredOffset = desiredYTubeHoleCenter - screenCenter;
        
        // Clampare l'offset entro i limiti validi per l'altezza corrente per garantire che il buco rimanga all'interno dello schermo
        int clampedOffset = Math.max(-maxHoleOffset, Math.min(maxHoleOffset, desiredOffset));
        
        this.yTubeHoleCenter = screenCenter + clampedOffset;
        
        int upperTubeHeight = Math.max(0, yTubeHoleCenter - DIST_Y_BETWEEN_TUBES / 2);
        int lowerTubeHeight = Math.max(0, gameHeight - upperTubeHeight - DIST_Y_BETWEEN_TUBES);
        
        // Se l'altezza calcolata è 0, il tubo corrispondente non esiste (Optional.empty())
        this.tubeUpOpt = upperTubeHeight > 0 ? Optional.of(new Tube(x0, 0, upperTubeHeight, true)) : Optional.empty();
        this.tubeDownOpt = lowerTubeHeight > 0 ? Optional.of(new Tube(x0, upperTubeHeight + DIST_Y_BETWEEN_TUBES, lowerTubeHeight, false)) : Optional.empty();
        
        if (tubeUpOpt.isEmpty() && tubeDownOpt.isEmpty()) {
        	throw new IllegalStateException("Window Height Too Small: No Tube Can Exist (Height: " + gameHeight + ")");
        }
		
		this.x = x0;
        // La y del TubePair è sempre 0, dato che i tubi partono entrambi da y=0
        this.y = 0;
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
        
        // Proteggere da altezze schermo inferiori alla dimensione del buco
        int maxOffsetByBounds = Math.max(0, screenCenter - halfHoleSize);
        
        // Ritornare il minimo tra l'offset basato sulla percentuale e quello basato sui confini
        return Math.min(maxOffsetByPercentage, maxOffsetByBounds);
    }
	
	private static int randomYTubeHoleCenter(int gameHeight) {
	    int maxHoleOffset = calcMaxHoleOffset(gameHeight);
	    int tubeHoleOffset = RANDOM.nextInt(- maxHoleOffset, maxHoleOffset + 1);
	    return (gameHeight / 2) + tubeHoleOffset;
	}
	
	public int getYTubeHoleCenter() {
	    return yTubeHoleCenter;
	}
	
	@Override
	public void setAlive(boolean alive) {
		isAlive = alive;
		tubeUpOpt.ifPresent(t -> t.setAlive(alive));
		tubeDownOpt.ifPresent(t -> t.setAlive(alive));
	}
	
	@Override
	public void updateHitBox() {
		Rectangle[] hitBoxUp = tubeUpOpt.map(Tube::getHitBox).orElse(new Rectangle[0]);
		Rectangle[] hitBoxDown = tubeDownOpt.map(Tube::getHitBox).orElse(new Rectangle[0]);

		int totalLength = hitBoxUp.length + hitBoxDown.length;
		
		// Inizializzare l'array hitBox se non è già stato fatto o se la lunghezza è cambiata
		if (hitBox == null || hitBox.length != totalLength) {
			hitBox = new Rectangle[totalLength];
		}

		// Unire le hitbox dei due tubi in un unico array
		System.arraycopy(hitBoxUp, 0, hitBox, 0, hitBoxUp.length);
		System.arraycopy(hitBoxDown, 0, hitBox, hitBoxUp.length, hitBoxDown.length);
	}
	
	@Override
	public void updateXY(double dt_s) {
		tubeUpOpt.ifPresent(t -> t.updateXY(dt_s));
		tubeDownOpt.ifPresent(t -> t.updateXY(dt_s));
		
		// I tubi hanno sempre la stessa x, quindi prendo quella di uno dei due
		x = tubeUpOpt.map(t -> t.x).orElseGet(() -> tubeDownOpt.get().x);
		
		updateHitBox();
	}
	
	@Override
	public List<? extends GameObject> getRenderableComponents() {
		List<AbstractGameObject> components = new ArrayList<>(2);
		tubeUpOpt.ifPresent(components::add);
		tubeDownOpt.ifPresent(components::add);
		return components;
	}
	
	@Override
	public String toString() {
		if (!isAlive()) {
	        return "Tube Not Alive";
	    }
		
		return "TubePair --> " + String.join(" | ",
				"TubeUp: " + tubeUpOpt.map(Tube::toString).orElse("None"),
				"TubeDown: " + tubeDownOpt.map(Tube::toString).orElse("None")
		);
	}
	
}