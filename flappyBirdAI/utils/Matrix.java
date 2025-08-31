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
        for (int i = 0; i < nRows; ++i) {
        	for (int j = 0; j < nCols; ++j) {
        		m.set(i, j, 1.0);
        	}
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
    	if (nRows <= 0) {
    		throw new IllegalArgumentException("Number of Rows Must be Greater than Zero");
		}
		if (nCols <= 0) {
			throw new IllegalArgumentException("Number of Columns Must be Greater than Zero");
    	}
		
        data = new double[nRows][nCols];
    }
    
    public boolean checkDimensions(Matrix otherMatrix) {
        if (this.getNRows() == otherMatrix.getNRows() && this.getNCols() == otherMatrix.getNCols()) {
        	return true;
        } else {
            return false;
        }
    }
    
    public Matrix applyFunction(DoubleUnaryOperator func) {
        Matrix mResult = new Matrix(getNRows(), getNCols());
        for (int i = 0; i < getNRows(); ++i) {
            for (int j = 0; j < getNCols(); ++j) {
            	mResult.set(i, j, func.applyAsDouble(this.get(i, j)));
            }
        }
        
        return mResult;
    }
    
    // Matrix Operations
    
    public Matrix transpose() {
        Matrix mResult = new Matrix(getNCols(), getNRows());
        for (int i = 0; i < getNRows(); ++i) {
            for (int j = 0; j < getNCols(); ++j) {
            	mResult.set(j, i, this.get(i, j));
            }
        }
        
        return mResult;
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
    
    public Matrix divide(Matrix otherMatrix) {
    	if (!checkDimensions(otherMatrix)) {
			throw new IllegalArgumentException("Incompatible Matrix Sizes for Matrix Subtraction");
		}

		Matrix mResult = new Matrix(getNRows(), getNCols());
		for (int i = 0; i < this.getNRows(); ++i) {
			for (int j = 0; j < this.getNCols(); ++j) {
				if (otherMatrix.get(i, j) == 0) {
					throw new ArithmeticException("Division by Zero at [" + i + "][" + j + "]");
				} else {
					mResult.set(i, j, this.get(i, j) / otherMatrix.get(i, j));
				}
			}
		}

		return mResult;
    }
    
    public Matrix add(Matrix otherMatrix) {
		if (!checkDimensions(otherMatrix)) {
			throw new IllegalArgumentException("Incompatible Matrix Sizes for Matrix Addition");
		}

		Matrix mResult = new Matrix(getNRows(), getNCols());
		for (int i = 0; i < this.getNRows(); ++i) {
			for (int j = 0; j < this.getNCols(); ++j) {
				mResult.set(i, j, this.get(i, j) + otherMatrix.get(i, j));
			}
		}

		return mResult;
	}
    
    public Matrix subtract(Matrix otherMatrix) {
    	if (!checkDimensions(otherMatrix)) {
			throw new IllegalArgumentException("Incompatible Matrix Sizes for Matrix Subtraction");
		}

		Matrix mResult = new Matrix(getNRows(), getNCols());
		for (int i = 0; i < this.getNRows(); ++i) {
			for (int j = 0; j < this.getNCols(); ++j) {
				mResult.set(i, j, this.get(i, j) - otherMatrix.get(i, j));
			}
		}

		return mResult;
    }
    
    // Element-wise Operations
    
    public Matrix elementWiseMultiply(Matrix otherMatrix) {
    	if (!checkDimensions(otherMatrix)) {
			throw new IllegalArgumentException("Incompatible Matrix Sizes for Matrix Element Wise Multiplication");
		}

		Matrix mResult = new Matrix(getNRows(), getNCols());
		for (int i = 0; i < this.getNRows(); ++i) {
			for (int j = 0; j < this.getNCols(); ++j) {
				mResult.set(i, j, this.get(i, j) * otherMatrix.get(i, j));
			}
		}

		return mResult;
    }
    
    public Matrix pow(double exp) {
        return applyFunction(x -> Math.pow(x, exp));
    }
    
    public Matrix root(double degree) {
		if (degree == 0) {
			throw new ArithmeticException("Root Degree Cannot be Zero");
		}
		
		return applyFunction(x -> Math.pow(x, 1.0 / degree));
	}
    
    public Matrix multiplyByScalar(double scalar) {
        return applyFunction(x -> x * scalar);
    }

    public Matrix divideByScalar(double scalar) {
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
                jsonRow.add(data[i][j]);
            }
            jsonData.add(jsonRow);
        }
        jsonMatrix.add("data", jsonData);
        
        return jsonMatrix;
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
