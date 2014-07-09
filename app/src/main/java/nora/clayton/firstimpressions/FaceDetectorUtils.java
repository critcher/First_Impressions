package nora.clayton.firstimpressions;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.FaceDetector;
import android.util.Log;

/**
 * Created by Clayton on 7/6/2014.
 *
 * Provides functions to find faces in an image.
 */
public class FaceDetectorUtils {

    public static Bitmap getFace(Bitmap img){
        Bitmap evenImg;
        //if the width is odd, cut off a column
        if(img.getWidth() % 2 == 1) {
            evenImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight());
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

        float eye_width = faces[0].eyesDistance() * 2;
        PointF midpoint = new PointF();
        faces[0].getMidPoint(midpoint);

        Rect faceRect = getFaceRect(eye_width, midpoint, evenImg.getWidth(), evenImg.getHeight());
        return Bitmap.createBitmap(evenImg, faceRect.left, faceRect.top, faceRect.width(),
                                              faceRect.height());
    }

    public static Rect getFaceRect(float eye_width, PointF midpoint, int originalWidth,
                               int originalHeight){
        int x, y, width, height;
        if(midpoint.x - eye_width < 0) {
            x = 0;
        }
        else{
            x = (int) (midpoint.x - eye_width);
        }
        if(midpoint.y - eye_width < 0) {
            y = 0;
        }
        else{
            y = (int) (midpoint.y - eye_width);
        }
        if(x + eye_width * 2 > originalWidth){
            width = originalWidth - x;
        }
        else{
            width = (int) eye_width * 2;
        }
        if(y + eye_width * 2 > originalHeight){
            height = originalHeight - y;
        }
        else{
            height = (int) eye_width * 2;
        }
        return new Rect(x, y, x + width, y + height);
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


}
