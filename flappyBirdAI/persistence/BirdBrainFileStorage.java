/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.persistence;

import flappyBirdAI.ai.BirdBrain;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public final class BirdBrainFileStorage {
	
	// Costruttore privato per evitare l'istanziazione
    private BirdBrainFileStorage() {
        throw new UnsupportedOperationException("BirdBrainFileStorage is a utility class and cannot be instantiated.");
    }
    
    public static void save(BirdBrain brain, Path file) throws NullPointerException, IOException {
		Objects.requireNonNull(brain, "Brain Cannot be Null");
		Objects.requireNonNull(file, "File Path Cannot be Null");

		Path parentDir = file.toAbsolutePath().getParent();
		
		// Crea la directory se non esiste (/autosaves)
		if (parentDir != null) {
			Files.createDirectories(parentDir);
		}

		// Scrive il contenuto del BirdBrain in formato JSON nel file
		// carica il contenuto in memoria in unica stringa, nessun buffering (nessun problema per file di piccole dimensioni)
		Files.writeString(
			file,
			brain.toJson(),
			StandardCharsets.UTF_8,
			StandardOpenOption.CREATE,
			StandardOpenOption.WRITE,
			StandardOpenOption.TRUNCATE_EXISTING
		);
	}

	public static BirdBrain load(Path file) throws NullPointerException, IOException {
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

}