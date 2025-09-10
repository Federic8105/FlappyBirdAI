package flappyBirdAI.controller;

import java.text.DecimalFormat;

public class GameStats {
	
	// Usa Locale di Default per il formato decimale
	private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("0.00");
	
	public static String roundAndFormatTwoDecimals(double value) {
		return TWO_DECIMALS.format(Math.round(value * 100) / 100.0);
	}
	
	// Default values
	public static final int DEFAULT_AUTOSAVE_THRESHOLD = 50;
	
    public int fps = 0, nGen = 1, nBirds = 0, nTubePassed = 0, nMaxTubePassed = 0, autoSaveThreshold = DEFAULT_AUTOSAVE_THRESHOLD;
    public long totGameTime = 0, sessionStartTime = 0;
    public double currLifeTime = 0, bestLifeTime = 0;
    public boolean isAutoSaveEnabled = true, isGameRunning = false;
    
    private final StringBuilder chronoBuilder = new StringBuilder(11);
    
    public boolean isFirstGen() {
		return nGen == 1;
	}
    
    // Ritorna il tempo di gioco totale in millisecondi
    public long getGameTimeElapsed() {
		return totGameTime + (isGameRunning ? System.currentTimeMillis() - sessionStartTime : 0);
	}
  	
    // Ritorna il tempo di gioco totale formattato come "HH:MM:SS.CS"
  	public String getFormattedGameTimeElapsed() {
  		long elapsedMs = getGameTimeElapsed();
  		long totalSeconds = elapsedMs / 1000;
  		long hours = totalSeconds / 3600;
  		long minutes = (totalSeconds % 3600) / 60;
  		long seconds = totalSeconds % 60;
  		long centiseconds = (elapsedMs % 1000) / 10;
  		
  		chronoBuilder.setLength(0);
  		
  		if (hours < 10) {
  			chronoBuilder.append('0');
  		}
  	    chronoBuilder.append(hours).append(':');

  	    if (minutes < 10) {
  	    	chronoBuilder.append('0');
  	    }
  	    chronoBuilder.append(minutes).append(':');

  	    if (seconds < 10) {
  	    	chronoBuilder.append('0');
  	    }
  	    chronoBuilder.append(seconds).append('.');

  	    if (centiseconds < 10) {
  	    	chronoBuilder.append('0');
  	    }
  	    chronoBuilder.append(centiseconds);

  	    return chronoBuilder.toString();
  	}
    
    public void reset() {
    	totGameTime = 0;
    	sessionStartTime = 0;
		fps = 0;
		nGen = 1;
		nBirds = 0;
		nTubePassed = 0;
		nMaxTubePassed = 0;
		currLifeTime = 0;
		bestLifeTime = 0;
		autoSaveThreshold = DEFAULT_AUTOSAVE_THRESHOLD;
		isAutoSaveEnabled = true;
		isGameRunning = false;
	}
}