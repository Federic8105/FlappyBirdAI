/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.persistence;

import flappyBirdAI.ai.BirdBrain;
import flappyBirdAI.controller.GameStats;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class BirdBrainFileStorage {
    
	// classe con 1 thread e una coda FIFO di task
    private static final ExecutorService PERSISTENCE_EXECUTOR =
    		Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "persistence-thread");
                // thread non daemon (background) per poterlo aspettare alla chiusura del gioco, così da garantire che tutte le scritture in corso siano completate
                t.setDaemon(false);
                return t;
            });
	
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
    
    public enum ShutdownResult { COMPLETED, TIMED_OUT, INTERRUPTED }
    
    // Da chiamare una volta alla chiusura del gioco, per garantire che tutte le scritture in corso siano completate
    public static ShutdownResult shutdownAndAwaitCompletion() {
    	// Disabilita l'accettazione di nuovi task e lascia completare quelli in coda
        PERSISTENCE_EXECUTOR.shutdown();
        try {
        	// Blocca il thread chiamante per massimo 30 secondi e nel frattempo lascia completare i task in coda
        	boolean completedInTime = PERSISTENCE_EXECUTOR.awaitTermination(30, TimeUnit.SECONDS);
        	
            if (completedInTime) {
            	return ShutdownResult.COMPLETED;
            } else {
            	return ShutdownResult.TIMED_OUT;
            }
            
        // arrivo di un segnale di interruzione esterno
        } catch (InterruptedException e) {
        	// Ripristina lo stato di interruzione del thread corrente
            Thread.currentThread().interrupt();
            
            return ShutdownResult.INTERRUPTED;
        }
    }
    
    public static CompletableFuture<Void> saveAsync(BirdBrain brain, GameStats gameStats) {
        Objects.requireNonNull(brain, "Brain Cannot be Null");
        Objects.requireNonNull(gameStats, "GameStats Cannot be Null");

        // creazione snapshot sincrona del BirdBrain in JSON, da passare al thread di persistenza
        // così da evitare che il BirdBrain venga modificato mentre lo si sta serializzando
        String json = brain.toJson();
        Path file = SaveFileNaming.createAutoSaveFilePath(gameStats);

        // ritorna subito CompletableFuture ma ExecutorService eseguirà il compito appena possibile
        return CompletableFuture.runAsync(() -> atomicWriteJson(json, file), PERSISTENCE_EXECUTOR);
    }
    
    public static CompletableFuture<Void> saveAsync(BirdBrain brain, Path file) {
        Objects.requireNonNull(brain, "Brain Cannot be Null");
        Objects.requireNonNull(file, "File Path Cannot be Null");

        String json = brain.toJson();
        return CompletableFuture.runAsync(() -> atomicWriteJson(json, file), PERSISTENCE_EXECUTOR);
    }
    
    private static void atomicWriteJson(String json, Path file) {
        try {
        	// Ottiene la directory padre del file di destinazione
            Path targetDir = file.toAbsolutePath().getParent();
            // Crea la directory padre se non esiste
            Files.createDirectories(targetDir);
            // File temporaneo nella stessa directory del file di destinazione
            Path tempFile = Files.createTempFile(targetDir, file.getFileName().toString(), ".tmp");
            try {
            	// Scrive il contenuto del BirdBrain in formato JSON nel file temporaneo
				// carica il contenuto in memoria in unica stringa, nessun buffering (nessun problema per file di piccole dimensioni)
                Files.writeString(tempFile, json, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                
                // Move atomico: nessuno stato intermedio visibile su absFilePath
		        // operazione atomica garantita dal filesystem perchè tempFile e absFilePath sono nella stessa directory
                Files.move(tempFile, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } finally {
            	// Pulire il temporaneo se il move non è avvenuto
                Files.deleteIfExists(tempFile);
            }
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }
	
    public static CompletableFuture<BirdBrain> loadAsync(Path file) {
        Objects.requireNonNull(file, "File Path Cannot be Null");
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!Files.exists(file)) throw new UncheckedIOException(new IOException("File Not Found: " + file));
                if (!Files.isRegularFile(file)) throw new UncheckedIOException(new IOException("Path is Not a Regular File: " + file));
                if (!Files.isReadable(file)) throw new UncheckedIOException(new IOException("File Not Readable: " + file));
                
                String json = Files.readString(file, StandardCharsets.UTF_8);
                return BirdBrain.fromJson(json);
                
            } catch (IOException e) {
                throw new CompletionException(e);
            } catch (BadFileFormatException e) {
                throw new CompletionException(e);
            }
        }, PERSISTENCE_EXECUTOR);
    }
	
	// API Methods
	
    // No I/O, asincrono
	public static String createManualSaveFileName(GameStats gameStats) {
	    return SaveFileNaming.createManualSaveFileName(gameStats);
	}

}