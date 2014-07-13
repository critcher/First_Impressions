package nora.clayton.firstimpressions;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.ConditionVariable;
import android.os.Looper;
import android.util.Log;

/**
 * Created by Clayton on 7/10/2014.
 */
public class ThreadedCamera {
    private static int WAIT_FOR_COMMAND_TO_COMPLETE = 1000;  // Milliseconds.

    //callback methods
    private RawPreviewCallback previewCallback;
    private ShutterCallback shutterCallback;
    private JpegPictureCallback picCallback;

    //threading variables
    private Looper looper;
    private final ConditionVariable previewDone;

    //camera variables
    private Camera cam;
    private int camId;
    private PictureReadyListener picListener;

    //handle back to the Activity that owns this threadedCam
    private Activity callingActivity;

    public ThreadedCamera(int id, Activity activity){
        previewCallback = new RawPreviewCallback();
        shutterCallback = new ShutterCallback();
        picCallback = new JpegPictureCallback();

        looper = null;
        previewDone = new ConditionVariable();

        cam = null;
        camId = id;

        callingActivity = activity;
        //if the calling activity does not implement a PictureReadyListener, we cannot notify
        //it of a Bitmap that is ready
        try {
            picListener = (PictureReadyListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("must implement PictureReadyListener");
        }

    }


    //starts the thread for the threaded cam and opens the camera
    //returns true if everything succeeded
    public boolean startCam() {
        final ConditionVariable startDone = new ConditionVariable();
        Log.e("threaded cam", "start looper");
        Log.e("startCam()","" + android.os.Process.myTid());
        new Thread() {
            @Override
            public void run() {
                // Set up a looper to be used by camera.
                Looper.prepare();
                Log.e("threaded cam", "start loopRun");
                Log.e("cam loopRun","" + android.os.Process.myTid());
                // Save the looper so that we can terminate this thread
                // after we are done with it.
                looper = Looper.myLooper();
                openCamera();
                cam.setPreviewCallback(previewCallback);
                startDone.open();
                Looper.loop();  // Blocks forever until Looper.quit() is called.
                Log.e("threaded cam", "initializeMessageLooper: quit.");
            }
        }.start();

        if (!startDone.block(WAIT_FOR_COMMAND_TO_COMPLETE)) {
            return false;
        }
        if(cam == null){
            return false;
        }
        Log.e("threaded cam","started cam well");
        return true;
    }

    //helper function that opens the camera safely
    private boolean openCamera() {
        try {
            cam = Camera.open(camId);
        } catch (RuntimeException e) {
            Log.e("CameraError", "Camera failed to open: " + e.getLocalizedMessage());
            return false;
        }
        Log.e("threaded cam", "opened: " +(cam != null));
        return true;
    }

    //helper function to release the camera safely
    private void releaseCamera(){
        if (cam != null){
            cam.release();        // release the camera for other applications
            cam = null;
        }
    }

    //takes the picture only if preview has started successfully (guards against a runtime exception)
    public void takePicture(){
        if(waitForPreviewDone()) {
            cam.takePicture(null, null, picCallback);
        }
    }


    //terminates the thread and releases the camera
    public boolean stopCam(){
        looper.quit();
        try {
            looper.getThread().join();
        }
        catch (InterruptedException e){
            return false;
        }
        releaseCamera();
        previewDone.close();
        Log.e("threaded cam", "released: " +(cam == null));
        return true;
    }


    //called when a preview is ready. Just releases the ConditionVariable for now
    private final class RawPreviewCallback implements Camera.PreviewCallback {
        public void onPreviewFrame(byte [] rawData, Camera camera) {
            previewDone.open();
        }
    }


    //Does nothing now, but can be used for a sound or animation that occurs when a pic is taken
    private final class ShutterCallback implements Camera.ShutterCallback {
        public void onShutter() {
            Log.e("threaded cam", "onShutter called");
        }
    }


    //Called when a picture has been taken. Gives the PictureReadyListener a properly oriented
    //Bitmap in RGB_565 format
    private final class JpegPictureCallback implements Camera.PictureCallback {
        public void onPictureTaken(byte [] picData, Camera camera) {
            Log.e("taking pic","" + android.os.Process.myTid());
            Log.e("threaded cam", "RawPictureCallback callback");
            //convert the image data into a Bitmap type in RGB_565 (for face detection)
            BitmapFactory.Options bitmapOpts=new BitmapFactory.Options();
            bitmapOpts.inPreferredConfig=Bitmap.Config.RGB_565;
            Bitmap bmap = BitmapFactory.decodeByteArray(picData,0,picData.length, bitmapOpts);

            //rotate the picture to be oriented properly
            Matrix rotation = new Matrix();
            rotation.postRotate(CameraUtils.getRequiredRotation(cam, callingActivity,camId, false));
            Bitmap orientedBitmap = Bitmap.createBitmap(bmap, 0, 0, bmap.getWidth(), bmap.getHeight(), rotation, true);

            //alert the PictureReadyListener
            picListener.onPictureReady(orientedBitmap);
        }
    }

    //A picture cannot be taken until the preview has started, so this method lets you check if
    //the preview has started
    private boolean waitForPreviewDone() {
        if (!previewDone.block(WAIT_FOR_COMMAND_TO_COMPLETE)) {
            Log.e("threaded cam", "waitForPreviewDone: timeout " + android.os.Process.myTid());
            return false;
        }
        previewDone.close();
        return true;
    }



    //getter methods

    public Camera.PreviewCallback getPreviewCallback(){
        return previewCallback;
    }

    public Camera getCamera(){
        return cam;
    }

    public int getId(){
        return camId;
    }
}
