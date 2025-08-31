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

public class BirdBrain implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final List<String> vInputsKeys = List.of("yBird", "vyBird", "yCenterTubeHole", "xDistBirdTube");
    public static final int nInputs = vInputsKeys.size();

    private static final List<Integer> vNeurons = new ArrayList<>(List.of(4, 4, 1));
    private static final int nLayers = vNeurons.size();

    private static final int maxValue = 1, minValue = -1;
    private static final double updateWeightABSValue = 0.0001;
    
    private static BirdBrain fromJsonObject(JsonObject brainJson) {
	    // Validazione parametri del cervello
	    int jsonNInputs = brainJson.get("nInputs").getAsInt();
	    if (jsonNInputs != nInputs) {
	        throw new IllegalArgumentException("Incompatible Input Size: Expected " + nInputs + ", Found " + jsonNInputs);
	    }
	    
	    Gson gson = new Gson();
	    Type typeStringList = new TypeToken<List<String>>() {}.getType();
	    List<String> jsonInputKeys = gson.fromJson(brainJson.get("inputKeys"), typeStringList);
	    if (!jsonInputKeys.equals(vInputsKeys)) {
	        throw new IllegalArgumentException("Incompatible Input Keys");
	    }
	    
	    Type typeIntegerList = new TypeToken<List<Integer>>() {}.getType();
	    List<Integer> jsonNNeurons = gson.fromJson(brainJson.get("nNeurons"), typeIntegerList);
	    if (!jsonNNeurons.equals(vNeurons)) {
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
	
	public static BirdBrain loadFromFile(Path file) throws IOException {
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
	    	Gson gson = new Gson();
	    	
	    	JsonObject brainJson = gson.fromJson(reader, JsonObject.class);
	    	brainJson = Objects.requireNonNull(brainJson, "JSON Object Cannot be Null or Empty");
	    	
	    	return fromJsonObject(brainJson);
	    	
	    } catch (JsonSyntaxException e) {
	        throw new IllegalArgumentException("Invalid JSON in File: " + e.getMessage(), e);
	    } catch (IOException e) {
	        throw new IOException("Error Reading File: " + e.getMessage(), e);
	    }
	}

    private final List<Matrix> vmWeights = new ArrayList<>();
    private Matrix mInputs;

    public BirdBrain() {
        setRandomWeights();
    }
    
    public BirdBrain(BirdBrain otherBrain) {
    	otherBrain = Objects.requireNonNull(otherBrain, "Brain Not Initialized");

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
        Map<String, Double>  normalizedList = new HashMap<>();

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

    public void setInputs(Map<String, Double> vInputs) {
        if (vInputs.size() != nInputs) {
            throw new IllegalArgumentException("Incorrect Number of Inputs");
        }
        for (String key : vInputs.keySet()) {
            if (!vInputsKeys.contains(key)) {
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
        Random rand = new Random();

        // Creazione Lista di Matrici dei Pesi
        for (int i = 0; i < nLayers; ++i) {
            int nRows = vNeurons.get(i);
            int nCols = i > 0 ? vNeurons.get(i - 1) : nInputs;
            vmWeights.add(new Matrix(nRows, nCols));
            for (int j = 0; j < vmWeights.get(i).getNRows(); ++j) {
                for (int k = 0; k < vmWeights.get(i).getNCols(); ++k) {
                    vmWeights.get(i).set(j, k, minValue + (maxValue - minValue) * rand.nextDouble());
                }
            }
        }

    }

    public void updateWeights() {
        Random rand = new Random();
        double updateWeightValue;

        for (Matrix mWeight : vmWeights) {
            for (int j = 0; j < mWeight.getNRows(); ++j) {
                for (int k = 0; k < mWeight.getNCols(); ++k) {

                    if (rand.nextInt(0, 1 + 1) == 1) {
                        updateWeightValue = -updateWeightABSValue;
                    } else {
                        updateWeightValue = updateWeightABSValue;
                    }

                    if (mWeight.get(j, k) + updateWeightValue > maxValue || mWeight.get(j, k) + updateWeightValue < minValue) {
                        updateWeightValue = -updateWeightValue;
                    }

                    mWeight.set(j, k, mWeight.get(j, k) + updateWeightValue);
                }
            }
        }
    }

    public boolean think() {
    	Objects.requireNonNull(mInputs, "Inputs Not Initialized");
        if (vmWeights.isEmpty()) {
            throw new IllegalArgumentException("Weights Not Initialized");
        }

        Matrix tempWeights;
        Matrix tempInputs = mInputs;
        Matrix tempResult = null;

        for (int i = 0; i < nLayers; ++i) {
            tempWeights = vmWeights.get(i);

            tempResult = tempWeights.multiply(tempInputs);
            for (int j = 0; j < tempResult.getNRows(); ++j) {
                tempResult.set(j, 0, sigmoid(tempResult.get(j, 0)));
            }

            if (i < nLayers - 1) {
                tempInputs = tempResult;
            }
        }

        return tempResult.get(0, 0) > 0.5;
    }
    
    private JsonObject createJsonObject() {
        Gson gson = new Gson();
        JsonObject brainJson = new JsonObject();
        
        brainJson.addProperty("nInputs", nInputs);
        brainJson.add("inputKeys", gson.toJsonTree(vInputsKeys));
        brainJson.add("nNeurons", gson.toJsonTree(vNeurons));
        brainJson.addProperty("nLayers", nLayers);
        brainJson.addProperty("maxValue", maxValue);
        brainJson.addProperty("minValue", minValue);
        brainJson.addProperty("updateWeightABSValue", updateWeightABSValue);
        
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
    
    public boolean saveToFile(Path file) {
    	try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject brainJson = createJsonObject();
            
            // Scrivere direttamente dal JsonObject al writer
            gson.toJson(brainJson, writer);
            writer.flush();

        } catch (IOException e) {
            System.err.println("Error Writing File: " + e.getMessage());
            return false;
        }
        
        return true;
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
		StringBuilder sb = new StringBuilder();
		
		sb.append("Brain --> ").append("Weights:").append(System.lineSeparator());
		for (int i = 0; i < vmWeights.size(); ++i) {
			sb.append("Layer ").append(i + 1).append(":").append(System.lineSeparator()).append(vmWeights.get(i)).append(System.lineSeparator());
		}
		
		// Rimuovere Ultima System.lineSeparator()
		if (sb.length() > 0) {
        	int lineSepLen = System.lineSeparator().length();
        	
        	if (sb.substring(sb.length() - lineSepLen).equals(System.lineSeparator())) {
        		sb.delete(sb.length() - lineSepLen, sb.length());
        	}
        }
		
		return sb.toString();
	}
    
    /*
    public void saveToFileAsync(Path file, java.util.function.Consumer<Boolean> callback) {
        CompletableFuture.supplyAsync(() -> saveToFile(file, true))
            .thenAccept(callback)
            .exceptionally(throwable -> {
                System.err.println("Error in Asynchronous Save: " + throwable.getMessage());
                callback.accept(false);
                return null;
            });
    }

    
    public static void loadFromFileAsync(Path file, java.util.function.Consumer<BirdBrain> callback) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return loadFromFile(file);
            } catch (IOException e) {
                System.err.println("Error in Asynchronous Load: " + e.getMessage());
                return null;
            }
        }).thenAccept(callback);
    }
     */
}