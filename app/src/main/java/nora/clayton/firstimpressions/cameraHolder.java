package nora.clayton.firstimpressions;

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
    private Camera mCamera;
    private int camId;
    Camera.PreviewCallback mRawPreviewCallback;

    public cameraHolder(Context c, Camera cam, Camera.PreviewCallback cb, int id){
        super(c);
        mCamera = cam;
        camId = id;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mRawPreviewCallback = cb;
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    public void surfaceCreated(SurfaceHolder sh){
        Log.e("holder", "created");

    }
    public void surfaceDestroyed (SurfaceHolder sh){
        Log.e("holder", "destroyed");
    }
    public void surfaceChanged (SurfaceHolder sh, int w, int h, int x){
        Log.e("holder", "changed");
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
            Log.e("holder", "changed error 1");
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewCallback(mRawPreviewCallback);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setDisplayOrientation(CameraUtils.getRequiredRotation(camId, true));
            mCamera.startPreview();

        } catch (Exception e){
            Log.e("holder", "changed error 2 " + (mCamera==null), e);
        }

    }
}
