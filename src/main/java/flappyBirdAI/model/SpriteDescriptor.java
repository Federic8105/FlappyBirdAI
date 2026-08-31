/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import flappyBirdAI.model.entities.FlappyBird;
import flappyBirdAI.model.entities.Tube;

public record SpriteDescriptor(String resourceKey, int nFrames) {
	
	// --- Costanti di Configurazione per Percorsi e Estensioni delle Immagini ---
	
	public static final String IMG_PATH= "/images/";
	public static final String IMG_EXT= ".png";
	
	// --- Istanze Predefinite per le Entità del Gioco ---
	
    public static final SpriteDescriptor BIRD = new SpriteDescriptor(FlappyBird.IMG_NAME, FlappyBird.NUM_IMAGES);
    public static final SpriteDescriptor TUBE_UP = new SpriteDescriptor(Tube.IMG_NAME + "_UP", 1);
    public static final SpriteDescriptor TUBE_DOWN = new SpriteDescriptor(Tube.IMG_NAME + "_DOWN", 1);
}