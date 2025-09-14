/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.StringJoiner;
import java.util.function.DoubleUnaryOperator;

public class Matrix implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	// Factory Methods
	
    public static Matrix identity(int size) {
        Matrix m = new Matrix(size, size);
        
        for (int i = 0; i < size; ++i)  {
        	m.set(i, i, 1.0);
        }
        
        return m;
    }

    public static Matrix zeros(int nRows, int nCols) {
    	// Di default tutti gli elementi sono inizializzati a 0.0 in un Array
        return new Matrix(nRows, nCols);
    }

    public static Matrix ones(int nRows, int nCols) {
        Matrix m = new Matrix(nRows, nCols);
        double[] onesRow = new double[nCols];
        Arrays.fill(onesRow, 1.0);
        
        for (int i = 0; i < nRows; ++i) {
        	System.arraycopy(onesRow, 0, m.data[i], 0, nCols);
        }
            
        return m;
    }

    public static Matrix random(int nRows, int nCols, double minValue, double maxValue) {
        Random rand = new Random();
        Matrix m = new Matrix(nRows, nCols);
        
        for (int i = 0; i < nRows; ++i) {
        	for (int j = 0; j < nCols; ++j) {
        		m.set(i, j, minValue + (maxValue - minValue) * rand.nextDouble());
        	}
        }
            
        return m;
    }

	public static Matrix fromJson(JsonObject jsonMatrix) throws NullPointerException, IllegalArgumentException {
		Objects.requireNonNull(jsonMatrix, "JSON Matrix Object Cannot be Null");
		
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
        JsonArray jsonRow;
        for (int i = 0; i < nRows; ++i) {
            jsonRow = jsonData.get(i).getAsJsonArray();
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

    // Constructors
    
    // Inizializza tutti gli elementi a 0.0 di default
    public Matrix(int nRows, int nCols) throws IllegalArgumentException {
    	if (nRows <= 0) {
    		throw new IllegalArgumentException("Number of Rows Must be Greater than Zero");
		}
		if (nCols <= 0) {
			throw new IllegalArgumentException("Number of Columns Must be Greater than Zero");
    	}
		
        data = new double[nRows][nCols];
    }
    
    public Matrix(Matrix otherMatrix) throws NullPointerException {
		Objects.requireNonNull(otherMatrix, "Other Matrix Cannot be Null");
		
		data = new double[otherMatrix.getNRows()][otherMatrix.getNCols()];
		
		for (int i = 0; i < otherMatrix.getNRows(); ++i) {
			System.arraycopy(otherMatrix.getRow(i), 0, data[i], 0, otherMatrix.getNCols());
		}
	}
    
    public Matrix(double[][] dataArray) throws NullPointerException, IllegalArgumentException {
		Objects.requireNonNull(dataArray, "Input Array Cannot be Null");
		
		if (dataArray.length == 0) {
			throw new IllegalArgumentException("Input Array Must Have at Least One Row");
		}
		
		int nCols = dataArray[0].length;
		if (nCols == 0) {
			throw new IllegalArgumentException("Input Array Must Have at Least One Column");
		}
		
		for (int i = 0; i < dataArray.length; ++i) {
			Objects.requireNonNull(dataArray[i], "Data Array Row " + i + " Cannot be Null");
			if (dataArray[i].length != nCols) {
				throw new IllegalArgumentException("All Rows in Input Array Must Have the Same Number of Columns");
			}
		}
		
		data = new double[dataArray.length][nCols];
		
		for (int i = 0; i < dataArray.length; ++i) {
			System.arraycopy(dataArray[i], 0, data[i], 0, nCols);
		}
	}
    
    public Matrix(double[] vData, int nRows, int nCols) throws NullPointerException, IllegalArgumentException {
    	Objects.requireNonNull(vData, "Input Array Cannot be Null");
    	
    	if (nRows <= 0 || nCols <= 0) {
            throw new IllegalArgumentException("Dimensions Must be Positive");
        }
        
        if (vData.length != nRows * nCols) {
            throw new IllegalArgumentException("Array Length (" + vData.length + ") Must Equal nRows * nCols (" + (nRows * nCols) + ")");
        } 
    	
        data = new double[nRows][nCols];
        for (int i = 0; i < nRows; ++i) {
			for (int j = 0; j < nCols; ++j) {
				data[i][j] = vData[i * nCols + j];
			}
		}
    }
    	
    // Utility Methods
    
    public boolean checkDimensions(Matrix otherMatrix) throws NullPointerException {
    	Objects.requireNonNull(otherMatrix, "Other Matrix Cannot be Null");
    	
    	if (getNRows() == otherMatrix.getNRows() && getNCols() == otherMatrix.getNCols()) {
        	return true;
        } else {
            return false;
        }
    }
    
    public Matrix applyFunction(DoubleUnaryOperator func) throws NullPointerException {
    	Objects.requireNonNull(func, "Function Cannot be Null");
    	
    	Matrix mResult = new Matrix(getNRows(), getNCols());
        for (int i = 0; i < getNRows(); ++i) {
            for (int j = 0; j < getNCols(); ++j) {
            	mResult.set(i, j, func.applyAsDouble(get(i, j)));
            }
        }
        
        return mResult;
    }
    
    // Matrix Operations
    
    public Matrix transpose() {
        Matrix mResult = new Matrix(getNCols(), getNRows());
        for (int i = 0; i < getNRows(); ++i) {
            for (int j = 0; j < getNCols(); ++j) {
            	mResult.set(j, i, get(i, j));
            }
        }
        
        return mResult;
    }

    public Matrix multiply(Matrix otherMatrix) throws NullPointerException, IllegalArgumentException {
    	Objects.requireNonNull(otherMatrix, "Other Matrix Cannot be Null");
    	
    	if (getNCols() != otherMatrix.getNRows()) {
            throw new IllegalArgumentException("Incompatible Matrix Sizes for Matrix Multiplication");
        }

        Matrix mResult = new Matrix(getNRows(), otherMatrix.getNCols());
        for (int i = 0; i < getNRows(); ++i) {
            for (int j = 0; j < otherMatrix.getNCols(); ++j) {
                double sum = 0;
                for (int k = 0; k < getNCols(); ++k) {
                    sum += get(i, k) * otherMatrix.get(k, j);
                }
                mResult.set(i, j, sum);
            }
        }

        return mResult;
    }
    
    public Matrix divide(Matrix otherMatrix) throws NullPointerException, IllegalArgumentException, ArithmeticException {
    	Objects.requireNonNull(otherMatrix, "Other Matrix Cannot be Null");
    	
    	if (!checkDimensions(otherMatrix)) {
			throw new IllegalArgumentException("Incompatible Matrix Sizes for Matrix Subtraction");
		}

		Matrix mResult = new Matrix(getNRows(), getNCols());
		for (int i = 0; i < getNRows(); ++i) {
			for (int j = 0; j < getNCols(); ++j) {
				if (otherMatrix.get(i, j) == 0) {
					throw new ArithmeticException("Division by Zero at [" + i + "][" + j + "]");
				} else {
					mResult.set(i, j, get(i, j) / otherMatrix.get(i, j));
				}
			}
		}

		return mResult;
    }
    
    public Matrix add(Matrix otherMatrix) throws NullPointerException, IllegalArgumentException {
    	Objects.requireNonNull(otherMatrix, "Other Matrix Cannot be Null");
    	
    	if (!checkDimensions(otherMatrix)) {
			throw new IllegalArgumentException("Incompatible Matrix Sizes for Matrix Addition");
		}

		Matrix mResult = new Matrix(getNRows(), getNCols());
		for (int i = 0; i < getNRows(); ++i) {
			for (int j = 0; j < getNCols(); ++j) {
				mResult.set(i, j, get(i, j) + otherMatrix.get(i, j));
			}
		}

		return mResult;
	}
    
    public Matrix subtract(Matrix otherMatrix) throws NullPointerException, IllegalArgumentException {
    	Objects.requireNonNull(otherMatrix, "Other Matrix Cannot be Null");
    	
    	if (!checkDimensions(otherMatrix)) {
			throw new IllegalArgumentException("Incompatible Matrix Sizes for Matrix Subtraction");
		}

		Matrix mResult = new Matrix(getNRows(), getNCols());
		for (int i = 0; i < getNRows(); ++i) {
			for (int j = 0; j < getNCols(); ++j) {
				mResult.set(i, j, get(i, j) - otherMatrix.get(i, j));
			}
		}

		return mResult;
    }
    
    // Element-wise Operations
    
    public Matrix elementWiseMultiply(Matrix otherMatrix) throws NullPointerException, IllegalArgumentException {
    	Objects.requireNonNull(otherMatrix, "Other Matrix Cannot be Null");
    	
    	if (!checkDimensions(otherMatrix)) {
			throw new IllegalArgumentException("Incompatible Matrix Sizes for Matrix Element Wise Multiplication");
		}

		Matrix mResult = new Matrix(getNRows(), getNCols());
		for (int i = 0; i < getNRows(); ++i) {
			for (int j = 0; j < getNCols(); ++j) {
				mResult.set(i, j, get(i, j) * otherMatrix.get(i, j));
			}
		}

		return mResult;
    }
    
    public Matrix pow(double exp) {
        return applyFunction(x -> Math.pow(x, exp));
    }
    
    public Matrix root(double degree) throws ArithmeticException {
		if (degree == 0) {
			throw new ArithmeticException("Root Degree Cannot be Zero");
		}
		
		return applyFunction(x -> Math.pow(x, 1.0 / degree));
	}
    
    public Matrix multiplyByScalar(double scalar) {
        return applyFunction(x -> x * scalar);
    }

    public Matrix divideByScalar(double scalar) throws ArithmeticException {
        if (scalar == 0) {
        	throw new ArithmeticException("Division by Zero");
        }
        
        return multiplyByScalar(1.0 / scalar);
    }
    
    // Accessors

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
    
    // Ritorna una copia della riga per evitare modifiche esterne
    public double[] getRow(int rowIndex) throws IndexOutOfBoundsException {
    	if (rowIndex < 0 || rowIndex >= getNRows()) {
            throw new IndexOutOfBoundsException("Row Index " + rowIndex + " Out of Bounds [0, " + (getNRows() - 1) + "]");
        }
    	
    	double[] rowCopy = new double[getNCols()];
    	System.arraycopy(data[rowIndex], 0, rowCopy, 0, getNCols());
    	return rowCopy;
    }
    
    public void setRow(int rowIndex, double[] newRow) throws NullPointerException, IndexOutOfBoundsException, IllegalArgumentException {
		Objects.requireNonNull(newRow, "New Row Cannot be Null");
    	
    	if (rowIndex < 0 || rowIndex >= getNRows()) {
			throw new IndexOutOfBoundsException("Row Index " + rowIndex + " Out of Bounds [0, " + (getNRows() - 1) + "]");
		}
		
		if (newRow.length != getNCols()) {
			throw new IllegalArgumentException("Array Length (" + newRow.length + ") Must Match Number of Columns (" + getNCols() + ")");
		}
		
		System.arraycopy(newRow, 0, data[rowIndex], 0, getNCols());
    }
    
    // Ritorna una copia della colonna per evitare modifiche esterne
    public double[] getCol(int colIndex) throws IndexOutOfBoundsException {
    	if (colIndex < 0 || colIndex >= getNCols()) {
			throw new IndexOutOfBoundsException("Column Index " + colIndex + " Out of Bounds [0, " + (getNCols() - 1) + "]");
		}
		
		double[] colCopy = new double[getNRows()];
		
		for (int i = 0; i < getNRows(); ++i) {
			colCopy[i] = get(i, colIndex);
		}
		
		return colCopy;
    }
    
    public void setCol(int colIndex, double[] newCol) throws NullPointerException, IndexOutOfBoundsException, IllegalArgumentException {
    	Objects.requireNonNull(newCol, "New Column Cannot be Null");
		
		if (colIndex < 0 || colIndex >= getNCols()) {
			throw new IndexOutOfBoundsException("Column Index " + colIndex + " Out of Bounds [0, " + (getNCols() - 1) + "]");
		}
		
		if (newCol.length != getNRows()) {
			throw new IllegalArgumentException("Array Length (" + newCol.length + ") Must Match Number of Rows (" + getNRows() + ")");
		}
		
		for (int i = 0; i < getNRows(); ++i) {
			set(i, colIndex, newCol[i]);
		}
    }
   
    // Restituisce una copia dell'array per evitare modifiche esterne
    public double[][] getDataCopy() {
		double[][] dataCopyArray = new double[getNRows()][getNCols()];
		
		for (int i = 0; i < getNRows(); ++i) {
			System.arraycopy(getRow(i), 0, dataCopyArray[i], 0, getNCols());
		}
		
		return dataCopyArray;
	}
    
    public double[] toArray() {
		double[] dataArray = new double[getNRows() * getNCols()];
		
		for (int dataArrayIndex = 0, i = 0; i < getNRows(); ++i) {
			for (int j = 0; j < getNCols(); ++j) {
				dataArray[dataArrayIndex++] = get(i, j);
			}
		}
		
		return dataArray;
	}
    
    @Override
    public Matrix clone() {
		Matrix mClone = new Matrix(getNRows(), getNCols());
		
		for (int i = 0; i < getNRows(); ++i) {
			System.arraycopy(data[i], 0, mClone.data[i], 0, getNCols());
		}
		
		return mClone;
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
	
	public JsonObject toJson() {
        JsonObject jsonMatrix = new JsonObject();
        jsonMatrix.addProperty("nRows", getNRows());
        jsonMatrix.addProperty("nCols", getNCols());
        
        JsonArray jsonData = new JsonArray();
        for (int i = 0; i < getNRows(); ++i) {
            JsonArray jsonRow = new JsonArray();
            for (int j = 0; j < getNCols(); ++j) {
                jsonRow.add(get(i, j));
            }
            jsonData.add(jsonRow);
        }
        jsonMatrix.add("data", jsonData);
        
        return jsonMatrix;
    }

	@Override
    public String toString() {
		StringJoiner sj = new StringJoiner(System.lineSeparator());
	    StringBuilder rowBuilder = new StringBuilder();
	    
	    for (int i = 0; i < getNRows(); ++i) {
	        for (int j = 0; j < getNCols(); ++j) {
	            rowBuilder.append("[").append(String.format("%.3f", get(i, j))).append("]");
	        }
	        
	        sj.add(rowBuilder.toString());
	        // reset lunghezza StringBuilder per la prossima riga
	        rowBuilder.setLength(0);
	    }
	    
	    return sj.toString();
    }

}
