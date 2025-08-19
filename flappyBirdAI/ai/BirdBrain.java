/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.ai;

import flappyBirdAI.utils.Matrix;
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

    private static final List<Integer> nNeurons = new ArrayList<>(List.of(4, 4, 1));
    private static final int nLayers = nNeurons.size();

    private static final int maxValue = 1, minValue = -1;
    private static final double updateWeightABSValue = 0.0001;

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
            int nRows = nNeurons.get(i);
            int nCols = i > 0 ? nNeurons.get(i - 1) : nInputs;
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
        if (mInputs == null) {
            throw new IllegalArgumentException("Input Non Inizializzati");
        }
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