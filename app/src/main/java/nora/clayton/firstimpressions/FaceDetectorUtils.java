package nora.clayton.firstimpressions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.FaceDetector;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import jjil.algorithm.Gray8DetectHaarMultiScale;
import jjil.algorithm.Gray8Rgb;
import jjil.algorithm.HaarClassifierCascade;
import jjil.algorithm.RgbAvgGray;
import jjil.android.RgbImageAndroid;
import jjil.core.*;

/**
 * Created by Clayton on 7/6/2014.
 *
 * Provides functions to find faces in an image.
 */
public class FaceDetectorUtils {
    //constants for how many eye_widths wide and tall the face rectangle should be
    private static final float WIDTH_MULTIPLIER = 2.0f;
    private static final float HEIGHT_MULTIPLIER = 3.5f;

    //finds one face in an image and returns a Bitmap with just the face in it
    public static Bitmap getFace(Bitmap img, Context c){
        Bitmap evenImg;
        //if the width is odd, cut off a column
        if(img.getWidth() % 2 == 1) {
            evenImg = Bitmap.createBitmap(img, 0, 0, img.getWidth() - 1, img.getHeight());
        }
        else{
            evenImg = img;
        }
        Log.e("dimensions:", ""+ img.getHeight() + "x" + img.getWidth());
        Log.e("dimensions:", ""+ evenImg.getHeight() + "x" + evenImg.getWidth());
        FaceDetector face_detector = new FaceDetector(evenImg.getWidth(), evenImg.getHeight(), 1);

        FaceDetector.Face[] faces = new FaceDetector.Face[1];
        int count = face_detector.findFaces(evenImg, faces);

        if (count < 1 || faces[0].confidence() < .25){
            return null;
        }
        float eye_width = faces[0].eyesDistance();
        PointF midpoint = new PointF();
        faces[0].getMidPoint(midpoint);

        Rect faceRect = getFaceRect(eye_width, midpoint, evenImg.getWidth(), evenImg.getHeight());
        Bitmap facePic = Bitmap.createBitmap(evenImg, faceRect.left, faceRect.top, faceRect.width(),
                                              faceRect.height());

        RgbImage jjilFace = RgbImageAndroid.toRgbImage(facePic);
        RgbAvgGray toGray = new RgbAvgGray();
        List<jjil.core.Rect> eyes = null;
        try {
            toGray.push(jjilFace);
            InputStream eyeStream = c.getAssets().open("heyes.txt");
            Gray8DetectHaarMultiScale detectHaar = new Gray8DetectHaarMultiScale(eyeStream, 1, 10);
            eyes = detectHaar.pushAndReturn(toGray.getFront());
        }
        catch (IOException e){
            Log.e("eye stuff", e.toString());
            return null;
        }
        catch (jjil.core.Error e){
            Log.e("eye stuff", e.toString());
            return null;
        }
        double angle = 0.0;
        if(eyes.size() == 0){
            return facePic;
        }
        else if (eyes.size() == 1){
            Point intMidpoint = new Point((int) midpoint.x - faceRect.left, (int) midpoint.y - faceRect.top);
            angle = findRotation(eyes.get(0).getCenter(), intMidpoint);
        }
        else if (eyes.size() == 2){
            angle = findRotation(eyes.get(0).getCenter(), eyes.get(1).getCenter());
        }
        else{
            eyes = getBiggestRects(eyes, 2);
            angle = findRotation(eyes.get(0).getCenter(), eyes.get(1).getCenter());
        }
        angle = Math.toDegrees(angle);
        Log.e("angle", "" + angle);

        if(Math.abs(angle) > 20){
            return facePic;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate((float) angle);
        Bitmap img3 = Bitmap.createBitmap(facePic, 0, 0, facePic.getWidth(), facePic.getHeight(), matrix, true);
        Bitmap img2 = Bitmap.createBitmap(img3.getWidth(), img3.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(img2);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(img3, 0, 0, paint);
        img3.recycle();

        Bitmap evenImg2;
        if(img2.getWidth() % 2 == 1) {
            evenImg2 = Bitmap.createBitmap(img2, 0, 0, img2.getWidth() - 1, img2.getHeight());
        }
        else{
            evenImg2 = img2;
        }
        //evenImg2.reconfigure(evenImg2.getWidth(), evenImg2.getHeight(), Bitmap.Config.RGB_565);
        Log.e("dimensions:", "" + img2.getHeight() + "x" + img2.getWidth());
        Log.e("dimensions:", ""+ evenImg2.getHeight() + "x" + evenImg2.getWidth());
        FaceDetector face_detector2 = new FaceDetector(evenImg2.getWidth(), evenImg2.getHeight(), 1);

        FaceDetector.Face[] faces2 = new FaceDetector.Face[1];
        int count2 = face_detector2.findFaces(evenImg2, faces2);

        if (count2 < 1 || faces2[0].confidence() < .25){
            Log.e("OMG", "your face disappeared " + evenImg2.getConfig());
            return evenImg2;
        }
        float eye_width2 = faces2[0].eyesDistance();
        PointF midpoint2 = new PointF();
        faces2[0].getMidPoint(midpoint2);

        Rect faceRect2 = getFaceRect(eye_width2, midpoint2, evenImg2.getWidth(), evenImg2.getHeight());
        Bitmap facePic2 = Bitmap.createBitmap(evenImg2, faceRect2.left, faceRect2.top, faceRect2.width(),
                faceRect2.height());

        return facePic2;
    }

    //helper function that gets a bounding box for a detected face
    private static Rect getFaceRect(float eye_width, PointF midpoint, int originalWidth,
                               int originalHeight){
        int x, y, width, height;
        if(midpoint.x - eye_width * (WIDTH_MULTIPLIER / 2) < 0) {
            x = 0;
        }
        else{
            x = (int) (midpoint.x - eye_width * (WIDTH_MULTIPLIER / 2));
        }
        if(midpoint.y - eye_width * (HEIGHT_MULTIPLIER / 2)< 0) {
            y = 0;
        }
        else{
            y = (int) (midpoint.y - eye_width * (HEIGHT_MULTIPLIER / 2));
        }
        if(x + eye_width * WIDTH_MULTIPLIER > originalWidth){
            width = originalWidth - x;
        }
        else{
            width = (int) (eye_width * WIDTH_MULTIPLIER);
        }
        if(y + eye_width * HEIGHT_MULTIPLIER > originalHeight){
            height = originalHeight - y;
        }
        else{
            height = (int) (eye_width * HEIGHT_MULTIPLIER);
        }
        return new Rect(x, y, x + width, y + height);
    }

    //converts a drawable (from res folder in app) to a Bitmap
    public static Bitmap drawableToBitmap (Drawable drawable) {

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    // Gets the biggest 'number' rectangles from a list by area. Returns a list of the biggest
    // rectangles or null if the list is not big enough.
    private static List<jjil.core.Rect> getBiggestRects(List<jjil.core.Rect> rects, int number){
        if(rects.size() < number){
            return null;
        }
        Collections.sort(rects);
        return rects.subList(0, number);
    }

    // Finds the amount an image should be rotated clockwise to make p1 and p2 at the same height
    private static double findRotation(Point p1, Point p2){
        Log.e("angle", "p1: " + p1.getX() + ", " + p1.getY());
        Log.e("angle", "p2: " + p2.getX() + ", " + p2.getY());
        return Math.atan((double) (p2.getY() - p1.getY()) / (p1.getX() - p2.getX()));
    }
}
