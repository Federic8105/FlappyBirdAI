/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.controller;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.Deque;

public final class GameClock {
	
	// --- Costanti di Formattazione ---
	
	// Usa Locale di Default per il formato decimale
    private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("0.00");
	
	// --- Costanti di Configurazione ---
    
    public static final int PAUSE_SLEEP_MS = 100;
  	public static final int MAX_FPS = 60;
  	private static final long TARGET_FRAME_TIME_NS = 1_000_000_000L / MAX_FPS;
  	// Numero di frame da considerare per la media mobile
    private static final int FPS_SAMPLE_SIZE = 30;
    // Fattore di smoothing per la media pesata esponenziale (EMA)
    // Valori più alti danno più peso ai valori passati, rendendo la media più "smooth"
    // Valori più bassi danno più peso ai valori recenti, rendendo la media più reattiva
    private static final double FPS_SMOOTHING_FACTOR = 0.9;
    
    // --- Campi di Stato per FPS ---
    
    // Coda per memorizzare i tempi dei frame più recenti (in nanosecondi)
    private final Deque<Long> dequeFrameDurationsNs = new ArrayDeque<>(FPS_SAMPLE_SIZE);
    private long frameStartTime, frameEndTime, lastFrameStartTime;
    private double smoothedFPS = 0;

    // --- Campi di Stato per Delta Time ---
    
    // Ultimo timestamp usato per dt (ns)
    private long lastUpdateTime;
    // Permette slow-motion o fast-forward
    private double dtMultiplier = 1.0; 

    // --- Campi di Stato per Cronometro Totale ---
    // Sessione: Tempo Trascorso dall'inizio della sessione di gioco/ultima ripresa del gioco fino alla pausa o al reset (ms)
    // Variabile volatile per garantire la visibilità tra thread sempre dei valori aggiornati senza sincronizzazione esplicita
    
    // Tempo accumulato dalle sessioni precedenti (ms)
    private volatile long totElapsedPastSessionsTime = 0; 
    // Timestamp di inizio sessione attuale (ms)
    private volatile long sessionStartTime;
    private volatile boolean isGameRunning = false;
    private final StringBuilder chronoBuilder = new StringBuilder(11);
    
    // --- Utily Statiche ---
    
    public static String roundAndFormatTwoDecimals(double value) {
        return TWO_DECIMALS.format(Math.round(value * 100) / 100.0);
    }
    
    // --- Gestione Frame ---
    
    public void setFrameStartTime() {
    	lastFrameStartTime = frameStartTime;
    	frameStartTime = System.nanoTime();
    }
    
    // Ritorna lo sleepTime in ns
    public long setFrameEndTime() {
		frameEndTime = System.nanoTime();
		// Durata totale del frame con sleep - Differenza tra inizio del frame corrente e inizio del frame precedente
		long totFrameDurationNs = frameStartTime - lastFrameStartTime;
		
		// Registrare il tempo del frame solo se è positivo (non contare il primo frame)
		if (totFrameDurationNs > 0) {
			registerFrameDuration(totFrameDurationNs);
		}
		
		// Calcolare il tempo di sleep necessario per mantenere gli FPS target
        return TARGET_FRAME_TIME_NS - (frameEndTime - frameStartTime);
	}
    
    private void registerFrameDuration(long frameDurationNs) {
        dequeFrameDurationsNs.addLast(frameDurationNs);
        
        // Mantenere solo gli ultimi FPS_SAMPLE_SIZE frame
        if (dequeFrameDurationsNs.size() > FPS_SAMPLE_SIZE) {
            dequeFrameDurationsNs.removeFirst();
        }
    }
    
    // --- Calcolo FPS ---
    
    // Calcolare gli FPS con media mobile sugli ultimi frame
    public int getAvgFPS() {
        if (dequeFrameDurationsNs.isEmpty()) {
            return 0;
        }
        
        long totFrameDurations = 0;
        for (long frameDuration : dequeFrameDurationsNs) {
        	totFrameDurations += frameDuration;
        }
        
        // Calcolare il tempo medio per frame in nanosecondi
        double avgFrameTimeNs = (double) totFrameDurations / dequeFrameDurationsNs.size();
        
        // Convertire in FPS (1 secondo = 1_000_000_000 nanosecondi)   
        return (int) (1_000_000_000.0 / avgFrameTimeNs);
    }
    
    // Calcolare gli FPS con media pesata esponenziale
    public int getEMAFPS() {
    	if (dequeFrameDurationsNs.isEmpty()) {
            return 0;
        }
        
        long lastFrameDuration = dequeFrameDurationsNs.getLast();
        double instantFPS = 1_000_000_000.0 / lastFrameDuration;
        
        // Formula EMA: smoothed = smoothed * alpha + instant * (1 - alpha)
        if (smoothedFPS != 0) {
        	smoothedFPS = smoothedFPS * FPS_SMOOTHING_FACTOR + instantFPS * (1 - FPS_SMOOTHING_FACTOR);
        } else {
        	// Prima inizializzazione
            smoothedFPS = instantFPS;
        }
        
        return (int) smoothedFPS;
    }
   
    // Calcolare gli FPS attuali basati sull'ultimo frame
    public int getCurrentFPS() {    
         if (dequeFrameDurationsNs.isEmpty()) {
			return 0;
         }
         
         long lastFrameDuration = dequeFrameDurationsNs.getLast();
         
         // Convertire in FPS (1 secondo = 1_000_000_000 nanosecondi)
         return (int) (1_000_000_000.0 / lastFrameDuration);
    }
    
    // --- Gestione Cronometro Totale ---

    // Avvio Clock
    public void start() {
    	totElapsedPastSessionsTime = 0;
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
        totElapsedPastSessionsTime += System.currentTimeMillis() - sessionStartTime;
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
        lastUpdateTime = System.nanoTime();
        isGameRunning = true;
    }
    
    // Reset Totale Clock e riavvio della sessione
    public void reset() {
    	totElapsedPastSessionsTime = 0;
    	startSession();
    	setLastUpdateTimeNow();
    }
    
    // --- Getters Delta Time e Tempo Totale ---

    // Calcolare il delta time (in secondi) dall'ultimo frame
    public double getDeltaTime() {
        if (!isGameRunning) {
        	return 0.0;
        }

        long now = System.nanoTime();
        double realDt = (now - lastUpdateTime) / 1e9;
        lastUpdateTime = now;
        
        // Limitare il dt massimo a 1/30 secondi per evitare salti di tempo anomali
        return Math.min(realDt, 1.0/30.0) * dtMultiplier;
    }

    // Ottenere il tempo totale di gioco in millisecondi
    public long getElapsedMs() {  
        return totElapsedPastSessionsTime + (isGameRunning ? System.currentTimeMillis() - sessionStartTime : 0);
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

    // --- Getters/Setters e Query di Stato ---
    
    public boolean isGameRunning() {
        return isGameRunning;
    }
    
    public void setDtMultiplier(double multiplier) {
        dtMultiplier = multiplier;
    }
    
    public void setLastUpdateTimeNow() {
		lastUpdateTime = System.nanoTime();
	}

}