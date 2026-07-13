/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.persistence;

import flappyBirdAI.ai.BirdBrain;
import flappyBirdAI.controller.GameStats;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public final class BirdBrainFileStorage {
	
	// Garantisce mutua esclusione tra scritture e chiusura del gioco
    private static final ReentrantLock WRITE_LOCK = new ReentrantLock();
    // Una volta true, nessuna nuova scrittura può iniziare
    private static volatile boolean shuttingDown = false;
	
	// Costruttore privato per evitare l'istanziazione
    private BirdBrainFileStorage() {
        throw new UnsupportedOperationException("BirdBrainFileStorage is a utility class and cannot be instantiated.");
    }
    
    // Da chiamare una volta all'avvio del gioco, prima di qualunque save/load
    public static void cleanupOrphanedTempFiles() {
        try (Stream<Path> stream = Files.list(SaveFileNaming.AUTOSAVE_DIR)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".tmp"))
                  .forEach(p -> {
                      try {
                          Files.deleteIfExists(p);
                      } catch (IOException e) {
                          // Non bloccante: se un tmp non si riesce a cancellare ora, si riprova al prossimo avvio
                      }
                  });
        } catch (IOException e) {
        	// Non bloccante: se la directory non esiste o non è leggibile, si ignora e si prosegue
		}
    }
    
    // Da chiamare alla chiusura del gioco: aspetta che una scrittura in corso finisca,
    // poi impedisce a qualunque nuova scrittura di partire
    public static void shutdownWrites() {
    	// deve acquisire lo stesso lock usato per le scritture, così da garantire che nessuna scrittura sia in corso
        WRITE_LOCK.lock();
       
        shuttingDown = true;
        
        WRITE_LOCK.unlock();
    }
    
    public static void save(BirdBrain brain, GameStats gameStats) throws NullPointerException, IOException {
		Objects.requireNonNull(brain, "Brain Cannot be Null");
		Objects.requireNonNull(gameStats, "GameStats Cannot be Null");
		atomicWrite(brain, SaveFileNaming.createAutoSaveFilePath(gameStats));
	}
    
    public static void save(BirdBrain brain, Path file) throws NullPointerException, IOException {
		Objects.requireNonNull(brain, "Brain Cannot be Null");
		Objects.requireNonNull(file, "File Path Cannot be Null");
		atomicWrite(brain, file);
	}
    
    private static void atomicWrite(BirdBrain brain, Path file) throws IOException {
    	WRITE_LOCK.lock();
    	
		try {
			// Il gioco si sta chiudendo: non iniziare una nuova scrittura
            if (shuttingDown) {
                return;
            }
			
            // Ottiene la directory padre del file di destinazione
			Path targetDir = file.toAbsolutePath().getParent();
			// Crea la directory padre se non esiste
	        Files.createDirectories(targetDir);
			
			 // File temporaneo nella stessa directory del file di destinazione
		    Path tempFile = Files.createTempFile(targetDir, file.getFileName().toString(), ".tmp");
		    
		    try {
		    	// Scrive il contenuto del BirdBrain in formato JSON nel file temporaneo
				// carica il contenuto in memoria in unica stringa, nessun buffering (nessun problema per file di piccole dimensioni)
		        Files.writeString(
		            tempFile,
		            brain.toJson(),
		            StandardCharsets.UTF_8,
		            StandardOpenOption.WRITE,
		            StandardOpenOption.TRUNCATE_EXISTING
		        );
	
		        // Move atomico: nessuno stato intermedio visibile su absFilePath
		        // operazione atomica garantita dal filesystem perchè tempFile e absFilePath sono nella stessa directory
		        Files.move(tempFile, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		    } finally {
		        // Pulire il temporaneo se il move non è avvenuto
		        Files.deleteIfExists(tempFile);
		    }
	    
		} finally {
			WRITE_LOCK.unlock();
		}
    }

	public static BirdBrain load(Path file) throws NullPointerException, IOException, BadFileFormatException {
		Objects.requireNonNull(file, "File Path Cannot be Null");

		if (!Files.exists(file)) {
			throw new IOException("File Not Found: " + file);
		}
		if (!Files.isRegularFile(file)) {
			throw new IOException("Path is Not a Regular File: " + file);
		}
		if (!Files.isReadable(file)) {
			throw new IOException("File Not Readable: " + file);
		}

		String json = Files.readString(file, StandardCharsets.UTF_8);
		return BirdBrain.fromJson(json);
	}
	
	// API Methods
	
	public static String createManualSaveFileName(GameStats gameStats) {
	    return SaveFileNaming.createManualSaveFileName(gameStats);
	}

}