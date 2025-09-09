package flappyBirdAI.controller;

import java.text.DecimalFormat;

public class GameStats {
	
	private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("#0.00");
	
	public static double roundTwoDecimals(double value) {
		return Double.parseDouble(TWO_DECIMALS.format(Math.round(value * 100) / 100.0).replace(',', '.'));
	}
	
	// Default values
	public static final int DEFAULT_AUTOSAVE_THRESHOLD = 50;
	
    public int fps = 0, nGen = 1, nBirds = 0, nTubePassed = 0, nMaxTubePassed = 0, autoSaveThreshold = DEFAULT_AUTOSAVE_THRESHOLD;
    public double currLifeTime = 0, bestLifeTime = 0;
    public boolean isAutoSaveEnabled = true;
    
    public boolean isFirstGen() {
		return nGen == 1;
	}
    
    public void reset() {
		fps = 0;
		nGen = 1;
		nBirds = 0;
		nTubePassed = 0;
		nMaxTubePassed = 0;
		currLifeTime = 0;
		bestLifeTime = 0;
		autoSaveThreshold = DEFAULT_AUTOSAVE_THRESHOLD;
		isAutoSaveEnabled = true;
	}
}