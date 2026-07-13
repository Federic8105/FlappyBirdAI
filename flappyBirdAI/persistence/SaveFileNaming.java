/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.persistence;

import flappyBirdAI.controller.GameStats;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Visibilità package-private: questa classe è un dettaglio di implementazione del package persistence e non deve essere visibile all'esterno
final class SaveFileNaming {
	
	// Directory in cui salvare i file di autosalvataggio
	public static final Path AUTOSAVE_DIR = Path.of("autosaves");
	
	// Template per i nomi dei file da salvare
	private static final String AUTO_SAVE_FILENAME_TEMPLATE = "autosave_gen_%d_maxTubePassed_%d_BLT_%.2f_time_%s.json";
	private static final String MANUAL_SAVE_FILENAME_TEMPLATE = "brain_gen_%d_maxTubePassed_%d_BLT_%.2f_time_%s.json";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

	// Costruttore privato per evitare l'istanziazione
    private SaveFileNaming() {
        throw new UnsupportedOperationException("SaveFileNaming is a utility class and cannot be instantiated.");
    }
    
    static Path createAutoSaveFilePath(GameStats stats) {
		return AUTOSAVE_DIR.resolve(createAutoSaveFileName(stats));
	}

	static String createManualSaveFileName(GameStats stats) {
		String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
		return String.format(MANUAL_SAVE_FILENAME_TEMPLATE, stats.nGen, stats.maxTubePassed, stats.bestLifeTime, timestamp);
	}
	
	private static String createAutoSaveFileName(GameStats stats) {
		String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
		return String.format(AUTO_SAVE_FILENAME_TEMPLATE, stats.nGen, stats.maxTubePassed, stats.bestLifeTime, timestamp);
	}

}