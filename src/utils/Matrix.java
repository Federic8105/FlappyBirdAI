/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.utils;

public class Matrix {
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
