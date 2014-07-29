package nora.clayton.firstimpressions;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;

/**
 * Created by Clayton on 7/14/2014.
 */
public class MatrixUtils {
    static String str(Matrix m, int decimals){
        if(m == null){
            return "is null";
        }
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

    public static Matrix importMatrix(String fileName, Context c){
        AssetManager assetManager = c.getAssets();
        int height = 0;
        int width = 0;
        double[] nums;
        try {
            InputStream stream = assetManager.open(fileName);
            //get the number of rows
            stream.skip(3);
            height = parseNum(stream).intValue();
            stream.skip(1);
            width = parseNum(stream).intValue();
            nums = new double[width * height];
            stream.skip(2);
            Double nextNum = null;
            int index = 0;
            while((nextNum = parseNum(stream)) != null){
                nums[index++] = nextNum.doubleValue();
            }
        }
        catch (Exception e) {
            Log.e("import matrix", e.toString());
            return null;
        }
        return new Matrix(nums, width).transpose();
    }

    private static Double parseNum(InputStream stream){
        String currentNum = "";
        char nextChar;
        try {
            while (((nextChar = (char) stream.read()) >=  '0' && nextChar <= '9') || nextChar == '-' || nextChar == '.' || nextChar == '\n' || nextChar == 'e') {
                if(nextChar != '\n') {
                    currentNum += nextChar;
                }
            }
        }
        catch (IOException e){
            return null;
        }
        if(currentNum == ""){
            return null;
        }
        if(currentNum.contains("e")){
            String[] parts = currentNum.split("e");
            return new Double(new Double(parts[0]) * (Math.pow(10,new Double(parts[1]))));
        }
        return new Double(currentNum);
    }
}