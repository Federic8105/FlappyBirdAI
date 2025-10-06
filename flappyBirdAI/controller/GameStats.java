/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.controller;

public final class GameStats {
	
	// Default values
	
	public static final int DEFAULT_AUTOSAVE_GEN_THRESHOLD = 50;
	// in secondi
	public static final int DEFAULT_AUTOSAVE_BLT_THRESHOLD = 30;
	public static final int DEFAULT_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD = 10;

	public static final boolean DEFAULT_IS_AUTOSAVE_ENABLED = true;
	public static final boolean DEFAULT_IS_AUTOSAVE_ON_GEN_ENABLED = true;
	public static final boolean DEFAULT_IS_AUTOSAVE_ON_BLT_ENABLED = false;
	public static final boolean DEFAULT_IS_AUTOSAVE_ON_MAX_TUBE_PASSED_ENABLED = false;
	
	// Min e Max valori per le soglie di autosave
	public static final int MIN_AUTOSAVE_GEN_THRESHOLD = 1, MAX_AUTOSAVE_GEN_THRESHOLD = 1000;
	public static final int MIN_AUTOSAVE_BLT_THRESHOLD = 10, MAX_AUTOSAVE_BLT_THRESHOLD = 36000;
	public static final int MIN_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD = 1, MAX_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD = 100;
	
    public int fps = 0, nGen = 1, nBirds = 0, nTubePassed = 0, maxTubePassed = 0;
    // in secondi
    public double currLifeTime = 0, bestLifeTime = 0;
    
    private int autoSaveGenThreshold = DEFAULT_AUTOSAVE_GEN_THRESHOLD, autoSaveBLTThreshold = DEFAULT_AUTOSAVE_BLT_THRESHOLD, autoSaveMaxTubePassedThreshold = DEFAULT_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD;
    
    // Stati di autosave
    //TODO gestione isAutoSaveEnabled per disabilitare tutte le altre
    public boolean isAutoSaveEnabled = DEFAULT_IS_AUTOSAVE_ENABLED;
    public boolean isAutoSaveOnGenEnabled = DEFAULT_IS_AUTOSAVE_ON_GEN_ENABLED;
    public boolean isAutoSaveOnBLTEnabled = DEFAULT_IS_AUTOSAVE_ON_BLT_ENABLED;
    public boolean isAutoSaveOnMaxTubePassedEnabled = DEFAULT_IS_AUTOSAVE_ON_MAX_TUBE_PASSED_ENABLED;
    
    public boolean isFirstGen() {
		return nGen == 1;
	}
    
    public int getAutoSaveGenThreshold() {
    	return autoSaveGenThreshold;
    }
    
    public void setAutoSaveGenThreshold(int threshold) throws IllegalArgumentException {
		if (threshold < MIN_AUTOSAVE_GEN_THRESHOLD || threshold > MAX_AUTOSAVE_GEN_THRESHOLD) {
			throw new IllegalArgumentException("Generation Autosave Threshold Must be Between " + MIN_AUTOSAVE_GEN_THRESHOLD + " and " + MAX_AUTOSAVE_GEN_THRESHOLD);
		}
		autoSaveGenThreshold = threshold;
	}
    
    public int getAutoSaveBLTThreshold() {
		return autoSaveBLTThreshold;
	}
    
    public void setAutoSaveBLTThreshold(int threshold) throws IllegalArgumentException {
    	if (threshold < MIN_AUTOSAVE_BLT_THRESHOLD || threshold > MAX_AUTOSAVE_BLT_THRESHOLD) {
    		throw new IllegalArgumentException("Best Life Time Autosave Threshold Must be Between " + MIN_AUTOSAVE_BLT_THRESHOLD + " and " + MAX_AUTOSAVE_BLT_THRESHOLD);
    	}
    	autoSaveBLTThreshold = threshold;
    }
    
    public int getAutoSaveMaxTubePassedThreshold() {
    	return autoSaveMaxTubePassedThreshold;
    }
    
    public void setAutoSaveMaxTubePassedThreshold(int threshold) throws IllegalArgumentException {
		if (threshold < MIN_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD || threshold > MAX_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD) {
			throw new IllegalArgumentException("Max Tube Passed Autosave Threshold Must be Between " + MIN_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD + " and " + MAX_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD);
		}
		autoSaveMaxTubePassedThreshold = threshold;
	}
    
    public void resetToFirstGen() {
		nGen = 1;
		nBirds = 0;
		nTubePassed = 0;
		maxTubePassed = 0;
		currLifeTime = 0;
		bestLifeTime = 0;
	}
    
    public void resetToDefaults() {
    	resetToFirstGen();
    	autoSaveGenThreshold = DEFAULT_AUTOSAVE_GEN_THRESHOLD;
    	autoSaveBLTThreshold = DEFAULT_AUTOSAVE_BLT_THRESHOLD;
    	autoSaveMaxTubePassedThreshold = DEFAULT_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD;
    	isAutoSaveEnabled = DEFAULT_IS_AUTOSAVE_ENABLED;
    	isAutoSaveOnGenEnabled = DEFAULT_IS_AUTOSAVE_ON_GEN_ENABLED;
    	isAutoSaveOnBLTEnabled = DEFAULT_IS_AUTOSAVE_ON_BLT_ENABLED;
    	isAutoSaveOnMaxTubePassedEnabled = DEFAULT_IS_AUTOSAVE_ON_MAX_TUBE_PASSED_ENABLED;
    }
    
}