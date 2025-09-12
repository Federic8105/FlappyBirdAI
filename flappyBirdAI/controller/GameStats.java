/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.controller;

public final class GameStats {
	
	// Default values
	public static final int DEFAULT_AUTOSAVE_THRESHOLD = 50;
	
    public int fps = 0, nGen = 1, nBirds = 0, nTubePassed = 0, nMaxTubePassed = 0, autoSaveThreshold = DEFAULT_AUTOSAVE_THRESHOLD;
    public double currLifeTime = 0, bestLifeTime = 0;
    public boolean isAutoSaveEnabled = true;
    
    public boolean isFirstGen() {
		return nGen == 1;
	}
    
    public void resetToFirstGen() {
		nGen = 1;
		nBirds = 0;
		nTubePassed = 0;
		nMaxTubePassed = 0;
		currLifeTime = 0;
		bestLifeTime = 0;
	}
    
    public void resetToDefaults() {
    	resetToFirstGen();
    	autoSaveThreshold = DEFAULT_AUTOSAVE_THRESHOLD;
    	isAutoSaveEnabled = true;
    }
}