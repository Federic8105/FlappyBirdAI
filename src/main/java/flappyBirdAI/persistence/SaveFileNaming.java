/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.persistence;

import flappyBirdAI.controller.GameStats;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

// Visibilità package-private: questa classe è un dettaglio di implementazione del package persistence e non deve essere visibile all'esterno
final class SaveFileNaming {
	
	// --- Costanti di Formattazione ---
	
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	
	// --- Costanti di Configurazione e Defaults ---
	
	// Directory di default in cui salvare i file di autosalvataggio: $HOME/.FlappyBirdAI/autosaves
	static final Path DEFAULT_AUTOSAVE_DIR = Path.of(System.getProperty("user.home"), ".FlappyBirdAI", "autosaves");
	
	// Template per i nomi dei file da salvare
	private static final String AUTO_SAVE_FILENAME_TEMPLATE = "autosave_gen_%d_maxTubePassed_%d_BLT_%.2f_time_%s.json";
	private static final String MANUAL_SAVE_FILENAME_TEMPLATE = "brain_gen_%d_maxTubePassed_%d_BLT_%.2f_time_%s.json";
	
	// --- Campi di Stato ---
	
	// Directory effettiva in cui salvare i file di autosalvataggio, inizializzata a DEFAULT_AUTOSAVE_DIR ma modificabile
	private static volatile Path autosaveDir = DEFAULT_AUTOSAVE_DIR;
		
	// --- Costruttori ---

	// Costruttore privato per evitare l'istanziazione
    private SaveFileNaming() {
        throw new UnsupportedOperationException("SaveFileNaming is a utility class and cannot be instantiated.");
    }
    
    // --- Generazione Nomi File di Salvataggio ---
    
    static Path createAutoSaveFilePath(GameStats stats) {
		return autosaveDir.resolve(createAutoSaveFileName(stats));
	}

	static String createManualSaveFileName(GameStats stats) {
		String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
		return String.format(MANUAL_SAVE_FILENAME_TEMPLATE, stats.nGen, stats.maxTubePassed, stats.bestLifeTime, timestamp);
	}
	
	private static String createAutoSaveFileName(GameStats stats) {
		String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
		return String.format(AUTO_SAVE_FILENAME_TEMPLATE, stats.nGen, stats.maxTubePassed, stats.bestLifeTime, timestamp);
	}
	
	// --- Getter/Setter della Directory di Autosave ---
    
    static Path getAutosaveDir() {
    	return autosaveDir;
    }
    
    static void setAutosaveDir(Path newDir) throws NullPointerException {
		autosaveDir = Objects.requireNonNull(newDir, "newDir Cannot be Null");
	}

}