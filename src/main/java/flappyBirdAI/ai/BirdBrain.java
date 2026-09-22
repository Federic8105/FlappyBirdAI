/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.ai;

import flappyBirdAI.persistence.BadFileFormatException;
import flappyBirdAI.utils.Matrix;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.StringJoiner;

public class BirdBrain implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	// Random condiviso per tutte le operazioni di mutazione
	private static final Random RANDOM = new Random();

	// --- Costanti di Configurazione ---
	
	public static final Set<String> V_INPUT_KEYS = Set.of("yBird", "vyBird", "yCenterTubeHole", "xDistBirdTube");
    public static final int NUM_INPUT = V_INPUT_KEYS.size();

    // numero di neuroni per ogni layer della rete neurale
    private static final List<Integer> V_NEURONS = List.of(4, 4, 1);
    private static final int NUM_LAYERS = V_NEURONS.size();

    private static final int WEIGHT_MIN_VALUE = -1, WEIGHT_MAX_VALUE = 1;
    private static final double WEIGHT_UPDATE_STEP = 0.0001;
    
    // --- Campi di Stato ---

    // una matrice per ogni layer della rete neurale
    // num righe = numero di neuroni nel layer (num output), num colonne = num di input del layer (num neuroni layer precedente o num input se primo layer)
    private final Matrix[] weights = new Matrix[NUM_LAYERS];
    
    // --- Costruttori ---

    public BirdBrain() {
        setRandomWeights();
    }
    
    public BirdBrain(BirdBrain otherBrain) throws NullPointerException {
    	Objects.requireNonNull(otherBrain, "Brain Cannot be Null");
    	
    	// copia profonda dei pesi della rete neurale
    	for (int i = 0; i < otherBrain.weights.length; ++i) {
            Matrix otherMatrix = otherBrain.weights[i];
            Matrix newMatrix = new Matrix(otherMatrix.getNRows(), otherMatrix.getNCols());
            for (int r = 0; r < otherMatrix.getNRows(); ++r) {
                for (int c = 0; c < otherMatrix.getNCols(); ++c) {
                    newMatrix.set(r, c, otherMatrix.get(r, c));
                }
            }
            weights[i] = newMatrix;
        }
	}
    
    // --- Factory Method da JSON ---
    
    // Converte una stringa JSON in un BirdBrain
  	public static BirdBrain fromJson(String json) throws NullPointerException, BadFileFormatException {
  		Objects.requireNonNull(json, "JSON String Cannot be Null");

  		try {
  			JsonObject brainJson = new Gson().fromJson(json, JsonObject.class);
  			return fromJsonObject(brainJson);
  		} catch (JsonSyntaxException e) {
  			throw new BadFileFormatException("Invalid JSON: " + e.getMessage(), e);
  		}
  	}
    
    private static BirdBrain fromJsonObject(JsonObject brainJson) throws NullPointerException, BadFileFormatException {
    	Objects.requireNonNull(brainJson, "JSON Object Cannot be Null");
    	
    	// Verifica la presenza dei campi richiesti nel JSON
    	if (!brainJson.has("nInputs")) {
    		throw new BadFileFormatException("Missing 'nInputs' Field in JSON");
	    }
    	if (!brainJson.has("inputKeys")) {
			throw new BadFileFormatException("Missing 'inputKeys' Field in JSON");
	    }
    	if (!brainJson.has("nNeurons")) {
    		throw new BadFileFormatException("Missing 'nNeurons' Field in JSON");
    	}
    	if (!brainJson.has("weights")) {
			throw new BadFileFormatException("Missing 'weights' Field in JSON");
		}
    	
	    int jsonNInputs = brainJson.get("nInputs").getAsInt();
	    if (jsonNInputs != NUM_INPUT) {
	        throw new BadFileFormatException("Incompatible Input Size: Expected " + NUM_INPUT + ", Found " + jsonNInputs);
	    }
	    
	    Gson gson = new Gson();
	    Type typeStringList = new TypeToken<List<String>>() {}.getType();
	    List<String> jsonInputKeys = gson.fromJson(brainJson.get("inputKeys"), typeStringList);
	    
	    // Converte List a Set per il confronto con V_INPUT_KEYS
	    if (!new HashSet<>(jsonInputKeys).equals(V_INPUT_KEYS)) {
	        throw new BadFileFormatException("Incompatible Input Keys: Expected " + V_INPUT_KEYS + ", Found " + jsonInputKeys);
	    }
	    
	    Type typeIntegerList = new TypeToken<List<Integer>>() {}.getType();
	    List<Integer> jsonNNeurons = gson.fromJson(brainJson.get("nNeurons"), typeIntegerList);
	    if (!jsonNNeurons.equals(V_NEURONS)) {
	        throw new BadFileFormatException("Incompatible Neural Network Structure: Expected " + V_NEURONS + ", Found " + jsonNNeurons);
	    }
	    
	    BirdBrain brain = new BirdBrain(); // ha dei pesi random ma vengono sovrascritti
	    JsonArray weightsArray = brainJson.getAsJsonArray("weights");
	    if (weightsArray.size() != NUM_LAYERS) {
	        throw new BadFileFormatException("Incompatible Number of Layers: Expected " + NUM_LAYERS + ", Found " + weightsArray.size());
	    }
	    
	    Matrix m;
	    for (int i = 0; i < NUM_LAYERS; ++i) {
	        try {
	            m = Matrix.fromJson(weightsArray.get(i).getAsJsonObject());
	        } catch (IllegalArgumentException | IllegalStateException e) {
	            throw new BadFileFormatException("Invalid Weights Matrix " + i + ": " + e.getMessage(), e);
	        }

	        int expectedCols = i > 0 ? V_NEURONS.get(i - 1) : NUM_INPUT;
	        if (m.getNRows() != V_NEURONS.get(i) || m.getNCols() != expectedCols) {
	            throw new BadFileFormatException("Incompatible Weights Matrix " + i + " Size");
	        }
	        
	        brain.weights[i] = m;
	    }
	    
	    return brain;
	}
    
    // --- Logica Rete Neurale ---
    
    public boolean think(Map<String, Double> vInputs) throws NullPointerException, IllegalArgumentException {
		Matrix mInputs = buildInputMatrix(vInputs), tempInputs = mInputs, tempResult = null;

        for (int i = 0; i < NUM_LAYERS; ++i) {
        	tempResult = weights[i].multiply(tempInputs);
            tempResult = tempResult.applyFunction(this::sigmoid);
            tempInputs = tempResult;
        }

        return tempResult.get(0, 0) > 0.5;
    }
    
    // --- Gestione Input ---
    
    private Matrix buildInputMatrix(Map<String, Double> vInputs) {
        Objects.requireNonNull(vInputs, "Inputs Map Cannot be Null");
        if (vInputs.size() != NUM_INPUT) {
            throw new IllegalArgumentException("Incorrect Number of Inputs");
        }
        for (String key : vInputs.keySet()) {
            if (!V_INPUT_KEYS.contains(key)) {
                throw new IllegalArgumentException("Incorrect Input Key: " + key);
            }
        }
        
        // Normalizzazione degli Input
        Map<String, Double> vInputsNormalized = normalize(vInputs);
        
        // Creazione Matrice degli Input
        Matrix mInputs = new Matrix(NUM_INPUT, 1);
        int i = 0;
        for (Double value : vInputsNormalized.values()) {
            mInputs.set(i++, 0, value);
        }
        return mInputs;
    }
    
    // --- Gestione Pesi ---
    
    private void setRandomWeights() {
        int nRows, nCols;
        for (int i = 0; i < NUM_LAYERS; ++i) {
            nRows = V_NEURONS.get(i);
            nCols = i > 0 ? V_NEURONS.get(i - 1) : NUM_INPUT;
            weights[i] = new Matrix(nRows, nCols);
            for (int j = 0; j < weights[i].getNRows(); ++j) {
                for (int k = 0; k < weights[i].getNCols(); ++k) {
                    weights[i].set(j, k, WEIGHT_MIN_VALUE + (WEIGHT_MAX_VALUE - WEIGHT_MIN_VALUE) * RANDOM.nextDouble());
                }
            }
        }

    }

    public void updateWeights() {
        double updateWeightValue;

        for (Matrix mWeight : weights) {
            for (int j = 0; j < mWeight.getNRows(); ++j) {
                for (int k = 0; k < mWeight.getNCols(); ++k) {

                    if (RANDOM.nextInt(0, 1 + 1) == 1) {
                        updateWeightValue = -WEIGHT_UPDATE_STEP;
                    } else {
                        updateWeightValue = WEIGHT_UPDATE_STEP;
                    }

                    if (mWeight.get(j, k) + updateWeightValue > WEIGHT_MAX_VALUE || mWeight.get(j, k) + updateWeightValue < WEIGHT_MIN_VALUE) {
                        updateWeightValue = -updateWeightValue;
                    }

                    mWeight.set(j, k, mWeight.get(j, k) + updateWeightValue);
                }
            }
        }
    }
    
    // --- Funzioni di Supporto Interne alla Rete Neurale ---

    // Normalizzazione dei Valori di Input Tra -1 e +1
    private Map<String, Double> normalize(Map<String, Double> list) {
        Map<String, Double>  normalizedList = new HashMap<>(list.size());

        // Ottenere Valore Massimo e Minimo da Lista di Input
        double max = Collections.max(list.values());
        double min = Collections.min(list.values());

        // Normalizzare i Valori di Input Tra -1 e +1
        for (Map.Entry<String, Double> entry : list.entrySet()) {
            normalizedList.put(entry.getKey(), 2 * ((entry.getValue() - min) / (max - min)) - 1);
        }

        return normalizedList;
    }

    // Funzione di Attivazione (Sigmoid) --> Result Range: 0 - 1
    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }
    
    // --- JSON Serialization ---
    
    public String toJson() {
    	// setPrettyPrinting() crea Json con indentazione (altrimenti tutto su una riga)
    	Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(createJsonObject());
    }
    
    private JsonObject createJsonObject() {
        Gson gson = new Gson();
        JsonObject brainJson = new JsonObject();
        
        brainJson.addProperty("nInputs", NUM_INPUT);
        brainJson.add("inputKeys", gson.toJsonTree(V_INPUT_KEYS));
        brainJson.add("nNeurons", gson.toJsonTree(V_NEURONS));
        brainJson.addProperty("nLayers", NUM_LAYERS);
        brainJson.addProperty("maxValue", WEIGHT_MAX_VALUE);
        brainJson.addProperty("minValue", WEIGHT_MIN_VALUE);
        brainJson.addProperty("updateWeightABSValue", WEIGHT_UPDATE_STEP);
        
        JsonArray weightsArray = new JsonArray();
        for (Matrix m : weights) {
            weightsArray.add(m.toJson());
        }
        brainJson.add("weights", weightsArray);
        
        return brainJson;
    }
    
    // --- Object Methods Override ---
    
    @Override
	public int hashCode() {
		return Arrays.hashCode(weights);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		BirdBrain other = (BirdBrain) obj;
		// basta Arrays.equals perché Matrix implementa correttamente equals e hashCode e array è di solo oggetti Matrix e non un array di array
		return Arrays.equals(weights, other.weights);
	}

	@Override
    public String toString() {
		StringJoiner sj = new StringJoiner(System.lineSeparator(), "Brain --> Weights:" + System.lineSeparator(), "");

		for (int i = 0; i < weights.length; ++i) {
			sj.add("Layer " + (i + 1) + ":" + System.lineSeparator() + weights[i]);
		}

		return sj.toString();
	}
    
}