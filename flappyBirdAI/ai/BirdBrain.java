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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class BirdBrain {
	
	public static final List<String> vInputsKeys = List.of("yBird", "vyBird", "yCenterTubeHole", "xDistBirdTube");
    public static final int nInputs = vInputsKeys.size();

    private static final List<Integer> vNeurons = new ArrayList<>(List.of(4, 4, 1));
    private static final int nLayers = vNeurons.size();

    private static final int maxValue = 1, minValue = -1;
    private static final double updateWeightABSValue = 0.0001;
	
	public static BirdBrain fromJson(String json) {
		json = Objects.requireNonNull(json, "JSON String Cannot be Null");
        if (json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON String Cannot be Empty");
        }
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        try {
            JsonObject brainJson = gson.fromJson(json, JsonObject.class);
            
            // Validazione Parametri del Cervello
            int jsonNInputs = brainJson.get("nInputs").getAsInt();
            if (jsonNInputs != nInputs) {
                throw new IllegalArgumentException("Incompatible Input Size: Expected " + nInputs + ", Found " + jsonNInputs);
            }
            
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
            
            // Creare Nuovo Cervello per Template
            BirdBrain tempBrain = new BirdBrain();
            // Rimuovere Pesi Inizializzati
            tempBrain.vmWeights.clear();
            
            JsonArray weightsArray = brainJson.getAsJsonArray("weights");
            for (int i = 0; i < weightsArray.size(); i++) {
                JsonObject matrixJson = weightsArray.get(i).getAsJsonObject();
                tempBrain.vmWeights.add(Matrix.fromJson(matrixJson));
            }
            
            return new BirdBrain(tempBrain);
            
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON Format: " + e.getMessage(), e);
        }
    }
	
	//TODO: scrittura migliore
	public static BirdBrain loadFromFile(Path file) throws IOException {
		try {
            String json = Files.readString(file);
            return fromJson(json);
        } catch (IOException e) {
            throw new IOException("Errore nella Lettura del File: " + e.getMessage(), e);
        }
	}

    private final List<Matrix> vmWeights = new ArrayList<>();
    private Matrix mInputs;

    public BirdBrain() {
        setRandomWeights();
    }
    
    public BirdBrain(BirdBrain otherBrain) {
    	otherBrain = Objects.requireNonNull(otherBrain, "Brain Non Inizializzato");

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
            throw new IllegalArgumentException("Numero di Input Non Corretto");
        }
        for (String key : vInputs.keySet()) {
            if (!vInputsKeys.contains(key)) {
                throw new IllegalArgumentException("Chiave di Input Non Corretta");
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
    	Objects.requireNonNull(mInputs, "Input Non Inizializzati");
        if (vmWeights.isEmpty()) {
            throw new IllegalArgumentException("Pesi Non Inizializzati");
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
    
    public String toJson() {
    	Gson gson = new GsonBuilder().setPrettyPrinting().create();
    	JsonObject brainJson = new JsonObject();
        brainJson.addProperty("nInputs", nInputs);
        brainJson.add("inputKeys", gson.toJsonTree(vInputsKeys));
        brainJson.add("nNeurons", gson.toJsonTree(vNeurons));
        brainJson.addProperty("nLayers", nLayers);
        brainJson.addProperty("maxValue", maxValue);
        brainJson.addProperty("minValue", minValue);
        brainJson.addProperty("updateWeightABSValue", updateWeightABSValue);
        
        JsonArray weightsArray = new JsonArray();
        for (Matrix matrix : vmWeights) {
            weightsArray.add(matrix.toJson());
        }
        brainJson.add("weights", weightsArray);
        
        return gson.toJson(brainJson);
    }
    
    //TODO: scrittura migliore
    public boolean saveToFile(Path file) {
    	try {
    		String json = toJson();
            Files.writeString(file, json);
            return true;
		} catch (IOException e) {
			System.err.println("Errore nella Scrittura del File: " + e.getMessage());
			return false;
		}
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

}