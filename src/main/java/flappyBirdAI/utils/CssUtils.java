/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class CssUtils {
	
	// --- Costruttori ---

    // Costruttore privato per evitare l'istanziazione
    private CssUtils() {
        throw new UnsupportedOperationException("CssUtils is a utility class and cannot be instantiated.");
    }
    
    // --- Utility per Conversione CSS ---

    // Convertire Stringa CSS in Data URI Base64 per poterla usare come foglio di stile esterno
    public static String toDataUri(String css) {
        return "data:text/css;base64," + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
    }
 
}