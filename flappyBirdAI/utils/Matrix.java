/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.util.Arrays;
import java.util.Objects;

public class Matrix {
	
	public static Matrix fromJson(JsonObject jsonMatrix) {
		jsonMatrix = Objects.requireNonNull(jsonMatrix, "JSON Matrix Object Cannot be Null");
		
		if (!jsonMatrix.isJsonObject()) {
			throw new IllegalArgumentException("Invalid JSON: Expected a JSON Object");
		}
		if (jsonMatrix.size() == 0) {
			throw new IllegalArgumentException("Malformed JSON: Empty Object");
		}
		if (!jsonMatrix.has("nRows")) {
			throw new IllegalArgumentException("Malformed JSON: Missing 'nRows' Field");
		}
		if (!jsonMatrix.has("nCols")) {
			throw new IllegalArgumentException("Malformed JSON: Missing 'nCols' Field");
		}
		if (!jsonMatrix.has("data")) {
			throw new IllegalArgumentException("Malformed JSON: Missing 'data' Field");
		}
		
		int nRows;
		try {
			nRows = jsonMatrix.get("nRows").getAsInt();
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Malformed JSON: 'nRows' Must be Integer", e);
		}
		
		int nCols;
		try {
			nCols = jsonMatrix.get("nCols").getAsInt();
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Malformed JSON: 'nCols' Must be Integer", e);
		}
		
		JsonArray jsonData;
		try {
			jsonData = jsonMatrix.getAsJsonArray("data");
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Malformed JSON: 'data' Must be an Array", e);
		}
        
        Matrix matrix = new Matrix(nRows, nCols);
        
        for (int i = 0; i < nRows; ++i) {
            JsonArray jsonRow = jsonData.get(i).getAsJsonArray();
            try {
                jsonRow = jsonData.get(i).getAsJsonArray();
            } catch (Exception e) {
                throw new IllegalArgumentException("Row " + i + " is Not a Valid JSON Array", e);
            }

            if (jsonRow.size() != nCols) {
                throw new IllegalArgumentException("Row " + i + " has " + jsonRow.size() + " Columns Instead of " + nCols);
            }
            
            for (int j = 0; j < nCols; ++j) {
            	try {
                    matrix.set(i, j, jsonRow.get(j).getAsDouble());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Element at [" + i + "][" + j + "] is Not a Valid Double Number", e);
                }
            }
        }
        
        return matrix;
    }
	
    private final double[][] data;

    public Matrix(int nRows, int nCols) {
        data = new double[nRows][nCols];
    }

    public int getNRows() {
        return data.length;
    }

    public int getNCols() {
        return data[0].length;
    }

    public double get(int row, int col) {
        return data[row][col];
    }

    public void set(int row, int col, double value) {
        data[row][col] = value;
    }

    public Matrix multiply(Matrix otherMatrix) {
        if (this.getNCols() != otherMatrix.getNRows()) {
            throw new IllegalArgumentException("Incompatible Matrix Sizes for Matrix Multiplication");
        }

        Matrix mResult = new Matrix(getNRows(), otherMatrix.getNCols());
        for (int i = 0; i < this.getNRows(); ++i) {
            double sum = 0;
            for (int j = 0; j < this.getNCols(); ++j) {
                sum += this.get(i, j) * otherMatrix.get(j, 0);
            }
            mResult.set(i, 0, sum);
        }

        return mResult;
    }
    
    public JsonObject toJson() {
        JsonObject jsonMatrix = new JsonObject();
        jsonMatrix.addProperty("nRows", getNRows());
        jsonMatrix.addProperty("nCols", getNCols());
        
        JsonArray jsonData = new JsonArray();
        for (int i = 0; i < getNRows(); ++i) {
            JsonArray jsonRow = new JsonArray();
            for (int j = 0; j < getNCols(); ++j) {
                jsonRow.add(data[i][j]);
            }
            jsonData.add(jsonRow);
        }
        jsonMatrix.add("data", jsonData);
        
        return jsonMatrix;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(data);
		return result;
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
		
		Matrix other = (Matrix) obj;
		return Arrays.deepEquals(data, other.data);
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (double[] row : data) {
            for (double value : row) {
                sb.append("[").append(String.format("%.3f", value)).append("]");
            }
            sb.append(System.lineSeparator());
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
