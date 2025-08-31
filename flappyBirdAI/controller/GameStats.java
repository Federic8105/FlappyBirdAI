package flappyBirdAI.controller;

public class GameStats {
	
	// Default values
	public static final int DEFAULT_AUTOSAVE_THRESHOLD = 50;
	
    public int fps = 0, nGen = 1, nBirds = 0, nTubePassed = 0, nMaxTubePassed = 0, autoSaveThreshold = DEFAULT_AUTOSAVE_THRESHOLD;
    public double bestLifeTime = 0;
    public boolean isAutoSaveEnabled = true;
}