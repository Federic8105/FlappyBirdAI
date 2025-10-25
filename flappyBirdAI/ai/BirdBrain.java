/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.ai;

import flappyBirdAI.utils.Matrix;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.StringJoiner;

public class BirdBrain implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	// Random condiviso per tutte le operazioni di mutazione
	private static final Random RANDOM = new Random();
	
	public static final List<String> V_INPUT_KEYS = List.of("yBird", "vyBird", "yCenterTubeHole", "xDistBirdTube");
    public static final int NUM_INPUT = V_INPUT_KEYS.size();

    private static final List<Integer> V_NEURONS = List.of(4, 4, 1);
    private static final int NUM_LAYERS = V_NEURONS.size();

    private static final int WEIGHT_MAX_VALUE = 1, WEIGHT_MIN_VALUE = -1;
    private static final double WEIGHT_UPDATE_STEP = 0.0001;
    
    private static BirdBrain fromJsonObject(JsonObject brainJson) throws NullPointerException, IllegalArgumentException {
    	Objects.requireNonNull(brainJson, "JSON Object Cannot be Null");
    	
	    // Validazione parametri del cervello
	    int jsonNInputs = brainJson.get("nInputs").getAsInt();
	    if (jsonNInputs != NUM_INPUT) {
	        throw new IllegalArgumentException("Incompatible Input Size: Expected " + NUM_INPUT + ", Found " + jsonNInputs);
	    }
	    
	    Gson gson = new Gson();
	    Type typeStringList = new TypeToken<List<String>>() {}.getType();
	    List<String> jsonInputKeys = gson.fromJson(brainJson.get("inputKeys"), typeStringList);
	    if (!jsonInputKeys.equals(V_INPUT_KEYS)) {
	        throw new IllegalArgumentException("Incompatible Input Keys");
	    }
	    
	    Type typeIntegerList = new TypeToken<List<Integer>>() {}.getType();
	    List<Integer> jsonNNeurons = gson.fromJson(brainJson.get("nNeurons"), typeIntegerList);
	    if (!jsonNNeurons.equals(V_NEURONS)) {
	        throw new IllegalArgumentException("Incompatible Neural Network Structure");
	    }
	    
	    // Creare nuovo cervello per template
	    BirdBrain tempBrain = new BirdBrain();
	    tempBrain.vmWeights.clear();
	    
	    JsonArray weightsArray = brainJson.getAsJsonArray("weights");
	    for (int i = 0; i < weightsArray.size(); ++i) {
	        JsonObject matrixJson = weightsArray.get(i).getAsJsonObject();
	        tempBrain.vmWeights.add(Matrix.fromJson(matrixJson));
	    }
	    
	    return new BirdBrain(tempBrain);
	}
	
	public static BirdBrain loadFromFile(Path file) throws IOException, IllegalArgumentException {
		if (!Files.exists(file)) {
	        throw new IOException("File Not Found: " + file);
	    }
	    if (!Files.isReadable(file)) {
	        throw new IOException("File Not Readable: " + file);
	    }
	    if (!Files.isRegularFile(file)) {
	        throw new IOException("Path is Not a Regular File: " + file);
	    }
	    if (Files.isDirectory(file)) {
	        throw new IOException("Path is a Direcotry, Not a File: " + file);
	    }
	    
	    try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
	    	return fromJsonObject(new Gson().fromJson(reader, JsonObject.class));
	    
	    } catch (JsonSyntaxException e) {
	        throw new IllegalArgumentException("Invalid JSON in File: " + e.getMessage(), e);
	    } catch (IOException e) {
	        throw new IOException("Error Reading File: " + e.getMessage(), e);
	    }
	}

    private final List<Matrix> vmWeights = new ArrayList<>(NUM_LAYERS);
    private Matrix mInputs;

    public BirdBrain() {
        setRandomWeights();
    }
    
    public BirdBrain(BirdBrain otherBrain) throws NullPointerException {
    	Objects.requireNonNull(otherBrain, "Brain Not Initialized");

    	for (Matrix otherMatrix : otherBrain.vmWeights) {
            Matrix newMatrix = new Matrix(otherMatrix.getNRows(), otherMatrix.getNCols());
            for (int i = 0; i < otherMatrix.getNRows(); ++i) {
                for (int j = 0; j < otherMatrix.getNCols(); ++j) {
                    newMatrix.set(i, j, otherMatrix.get(i, j));
                }
            }
            vmWeights.add(newMatrix);
        }
	}

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

    public void setInputs(Map<String, Double> vInputs) throws NullPointerException, IllegalArgumentException {
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
        mInputs = new Matrix(vInputs.size(), 1);
        int i = 0;
        for (Map.Entry<String, Double> entry : vInputsNormalized.entrySet()) {
            mInputs.set(i, 0, entry.getValue());
            ++i;
        }
    }

    private void setRandomWeights() {
        int nRows, nCols;
        
        // Creazione Lista di Matrici dei Pesi
        for (int i = 0; i < NUM_LAYERS; ++i) {
            nRows = V_NEURONS.get(i);
            nCols = i > 0 ? V_NEURONS.get(i - 1) : NUM_INPUT;
            vmWeights.add(new Matrix(nRows, nCols));
            for (int j = 0; j < vmWeights.get(i).getNRows(); ++j) {
                for (int k = 0; k < vmWeights.get(i).getNCols(); ++k) {
                    vmWeights.get(i).set(j, k, WEIGHT_MIN_VALUE + (WEIGHT_MAX_VALUE - WEIGHT_MIN_VALUE) * RANDOM.nextDouble());
                }
            }
        }

    }

    public void updateWeights() {
        double updateWeightValue;

        for (Matrix mWeight : vmWeights) {
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

    public boolean think() throws NullPointerException, IllegalArgumentException {
    	Objects.requireNonNull(mInputs, "Inputs Not Initialized");
        if (vmWeights.isEmpty()) {
            throw new IllegalArgumentException("Weights Not Initialized");
        }

        Matrix tempInputs = mInputs, tempResult = null;

        for (int i = 0; i < NUM_LAYERS; ++i) {
        	tempResult = vmWeights.get(i).multiply(tempInputs);
            tempResult = tempResult.applyFunction(this::sigmoid);
            tempInputs = tempResult;
        }

        return tempResult.get(0, 0) > 0.5;
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
        for (Matrix m : vmWeights) {
            weightsArray.add(m.toJson());
        }
        brainJson.add("weights", weightsArray);
        
        return brainJson;
    }
    
    public String toJson() {
    	Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(createJsonObject());
    }
    
    public void saveToFile(Path file) throws IOException {
    	try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject brainJson = createJsonObject();
            
            // Scrivere direttamente dal JsonObject al writer
            gson.toJson(brainJson, writer);
            writer.flush();

        } catch (IOException e) {
            throw e;
        }
    }
    
    @Override
	public int hashCode() {
		return Objects.hash(mInputs, vmWeights);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		BirdBrain other = (BirdBrain) obj;
		return Objects.equals(mInputs, other.mInputs) && Objects.equals(vmWeights, other.vmWeights);
	}

	@Override
    public String toString() {
		StringJoiner sj = new StringJoiner(System.lineSeparator(), "Brain --> Weights:" + System.lineSeparator(), "");

		for (int i = 0; i < vmWeights.size(); ++i) {
			sj.add("Layer " + (i + 1) + ":" + System.lineSeparator() + vmWeights.get(i));
		}

		return sj.toString();
	}
    
}