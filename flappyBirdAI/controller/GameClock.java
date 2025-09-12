/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.controller;

import java.text.DecimalFormat;

public final class GameClock {

	// Usa Locale di Default per il formato decimale
    private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("0.00");
    
    public static String roundAndFormatTwoDecimals(double value) {
        return TWO_DECIMALS.format(Math.round(value * 100) / 100.0);
    }

    // --- Delta time ---
    
    // Ultimo timestamp usato per dt
    private long lastTime;
    // Permette slow-motion o fast-forward
    private double dtMultiplier = 1.0; 

    // --- Cronometro totale ---
    
    // Tempo accumulato (ms)
    private long totElapsedGameTime = 0; 
    private long sessionStartTime = 0;
    private boolean isGameRunning = false;

    private final StringBuilder chronoBuilder = new StringBuilder(11);

    // --- API pubblica ---

    // Avvio Clock
    public void start() {
    	totElapsedGameTime = 0;
        isGameRunning = true;
    }
    
    // Avvio Sessione
    public void startSession() {
		sessionStartTime = System.currentTimeMillis();
	}

    // Mettere in pausa il clock
    public void pause() {
        if (!isGameRunning) {
        	return;
        }
        
        // Accumulare il tempo della sessione corrente
        totElapsedGameTime += System.currentTimeMillis() - sessionStartTime;
        isGameRunning = false;
    }

    // Riprendere il clock
    public void resume() {
        if (isGameRunning) {
        	return;
        }
        
        // Riavviare il conteggio del tempo della sessione
        sessionStartTime = System.currentTimeMillis();
        // Resettare il lastDt per evitare un salto di tempo anomalo quando il gioco riprende
        lastTime = System.nanoTime();
        isGameRunning = true;
    }

    // Calcolare il delta time (in secondi) dall'ultimo frame
    public double getDeltaTime() {
        if (!isGameRunning) {
        	return 0.0;
        }

        long now = System.nanoTime();
        double dt = (now - lastTime) / 1e9 * dtMultiplier;
        lastTime = now;
        
        return dt;
    }

    // Ottenere il tempo totale di gioco in millisecondi
    public long getElapsedMs() {  
        return totElapsedGameTime + (isGameRunning ? System.currentTimeMillis() - sessionStartTime : 0);
    }

    // Ottenere il tempo totale di gioco in secondi
    public double getElapsedSeconds() {
        return getElapsedMs() / 1000.0;
    }

    // Ritorna il tempo di gioco totale formattato come "HH:MM:SS.CS"
    public String getFormattedGameTimeElapsed() {
        long elapsedMs = getElapsedMs();
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

    // Reset Totale Clock
    public void reset() {
    	totElapsedGameTime = 0;
        sessionStartTime = 0;
        lastTime = 0;
    }

    // --- Getter/Setter ---
    public boolean isGameRunning() {
        return isGameRunning;
    }

    public double getDtMultiplier() {
        return dtMultiplier;
    }
    
    public void setDtMultiplier(double multiplier) {
        dtMultiplier = multiplier;
    }
    
    public void setLastTimeNow() {
		lastTime = System.nanoTime();
	}

}