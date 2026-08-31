/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view.fx;

import flappyBirdAI.model.GameObject;
import flappyBirdAI.model.SpriteDescriptor;
import flappyBirdAI.view.GameRenderer;
import javafx.scene.canvas.GraphicsContext;
import java.awt.Image;

public class FxGameRenderer implements GameRenderer<GraphicsContext> {

	@Override
	public Image[] ensureLoaded(SpriteDescriptor desc) {
		return null;
	}

	@Override
	public void renderSingle(GraphicsContext graphicsContext, GameObject obj) {
		
	}
	
}