package nora.clayton.firstimpressions;

import Jama.Matrix;

/**
 * Created by Clayton on 7/14/2014.
 */
public class MatrixUtils {
    static String str(Matrix m, int decimals){
        String decString = "";
        if(decimals != -1){
            decString = "." + decimals;
        }
        double[][] mArray = m.getArray();
        String matrixString = "" + mArray.length + "x" + mArray[0].length + " matrix:\n[";
        for(int y = 0; y < mArray.length; y++){
            matrixString += "[";
            if(y != 0){
                matrixString += " ";
            }
            for(int x = 0; x < mArray[0].length; x++){
                matrixString += "  " + String.format("%" + decString + "f",mArray[y][x]);
            }
            if(y != mArray.length - 1) {
                matrixString += "   ]\n";
            }
            else{
                matrixString += "   ]";
            }
        }
        return matrixString + "]";
    }

    public static String str(Matrix m){
        return str(m, -1);
    }
}
