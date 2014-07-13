package nora.clayton.firstimpressions;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Clayton on 7/11/2014.
 */
public class cameraHolder extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder mHolder;
    private Activity activity;
    private ThreadedCamera threadedCam;

    public cameraHolder(Context c, int camId, Activity act){
        super(c);
        mHolder = getHolder();
        mHolder.addCallback(this);
        activity = act;
        threadedCam = new ThreadedCamera(camId, act);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    public void surfaceCreated(SurfaceHolder sh){
        Log.e("holder", "created");

    }
    public void surfaceDestroyed (SurfaceHolder sh){
        Log.e("holder", "destroyed");
        if(threadedCam != null) {
            threadedCam.stopCam();
        }
    }

    public void surfaceChanged (SurfaceHolder sh, int w, int h, int x){
        Log.e("holder", "changed");
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            threadedCam.getCamera().stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
            Log.e("holder", "changed error 1");
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            threadedCam.startCam();
            threadedCam.getCamera().setPreviewDisplay(mHolder);
            threadedCam.getCamera().setDisplayOrientation(CameraUtils.getRequiredRotation(threadedCam.getCamera(),
                    activity, threadedCam.getId(), true));
            threadedCam.getCamera().startPreview();

        } catch (Exception e){
            Log.e("holder", "changed error 2 " + (threadedCam.getCamera()==null), e);
        }

    }

    public ThreadedCamera getThreadedCam(){
        return threadedCam;
    }
}
