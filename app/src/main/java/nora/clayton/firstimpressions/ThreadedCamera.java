package nora.clayton.firstimpressions;

import android.content.Context;
import android.hardware.Camera;
import android.os.ConditionVariable;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Clayton on 7/10/2014.
 */
public class ThreadedCamera {
    private static int WAIT_FOR_COMMAND_TO_COMPLETE = 10000;  // Milliseconds.

    private RawPreviewCallback mRawPreviewCallback;
    private ShutterCallback mShutterCallback;
    private RawPictureCallback mRawPictureCallback;

    private Looper mLooper;
    private final ConditionVariable mPreviewDone;
    private final ConditionVariable mSnapshotDone;

    public cameraHolder ch;

    private Context mContext;

    private Camera mCamera;
    private int camId;

    public ThreadedCamera(int id, Context context){
        mRawPreviewCallback = new RawPreviewCallback();
        mShutterCallback = new ShutterCallback();
        mRawPictureCallback = new RawPictureCallback();

        mLooper = null;
        mPreviewDone = new ConditionVariable();
        mSnapshotDone = new ConditionVariable();

        mCamera = null;
        camId = id;

        mContext = context;
    }


    /*
     * Initializes the message looper so that the Camera object can
     * receive the callback messages.
     */
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
                mLooper = Looper.myLooper();
                openCamera();
                startDone.open();
                Looper.loop();  // Blocks forever until Looper.quit() is called.
                Log.e("threaded cam", "initializeMessageLooper: quit.");
            }
        }.start();

        if (!startDone.block(WAIT_FOR_COMMAND_TO_COMPLETE)) {
            return false;
        }
        if(mCamera == null){
            return false;
        }
        ch = new cameraHolder(mContext,mCamera, mRawPreviewCallback);
        Log.e("threaded cam","started cam well");
        return true;
    }


    private boolean openCamera() {
        try {
            mCamera = Camera.open(camId);
        } catch (RuntimeException e) {
            Log.e("CameraError", "Camera failed to open: " + e.getLocalizedMessage());
            return false;
        }
        Log.e("threaded cam", "opened: " +(mCamera != null));
        return true;
    }


    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }


    public void takePicture(){
        if(waitForPreviewDone()) {
            mCamera.takePicture(null, null, mRawPictureCallback);
        }
    }


    /*
     * Terminates the message looper thread.
     */
    public boolean stopCam(){
        mLooper.quit();
        try {
            mLooper.getThread().join();
        }
        catch (InterruptedException e){
            return false;
        }
        releaseCamera();
        Log.e("threaded cam", "released: " +(mCamera == null));
        return true;
    }


    //Implement the previewCallback
    private final class RawPreviewCallback implements Camera.PreviewCallback {
        public void onPreviewFrame(byte [] rawData, Camera camera) {
            mPreviewDone.open();
        }
    }


    //Implement the shutterCallback
    private final class ShutterCallback implements Camera.ShutterCallback {
        public void onShutter() {
            //TODO: fill in
            Log.e("threaded cam", "onShutter called");
        }
    }


    //Implement the RawPictureCallback
    private final class RawPictureCallback implements Camera.PictureCallback {
        public void onPictureTaken(byte [] rawData, Camera camera) {
            //TODO: fill in
            Log.e("taking pic","" + android.os.Process.myTid());
            Log.e("threaded cam", "RawPictureCallback callback");
        }
    }


    private void waitForSnapshotDone() {
        if (!mSnapshotDone.block(WAIT_FOR_COMMAND_TO_COMPLETE)) {
            // timeout could be expected or unexpected. The caller will decide.
            Log.e("threaded cam", "waitForSnapshotDone: timeout");
        }
        mSnapshotDone.close();
    }


    private boolean waitForPreviewDone() {
        if (!mPreviewDone.block(WAIT_FOR_COMMAND_TO_COMPLETE)) {
            Log.e("threaded cam", "waitForPreviewDone: timeout " + android.os.Process.myTid());
            return false;
        }
        mPreviewDone.close();
        return true;
    }
}
