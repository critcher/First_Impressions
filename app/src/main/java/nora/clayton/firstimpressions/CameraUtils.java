package nora.clayton.firstimpressions;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.view.Surface;

/**
 * Several functions that help when working with the android camera
 * Created by Clayton on 7/9/2014.
 */
public class CameraUtils {
    // Check if the phone has a front camera
    public static boolean checkForFrontCam(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){
            return true;
        } else {
            return false;
        }
    }


    //get the clockwise rotation needed to display thee preview or the actual image
    //only currently works for front-facing cameras
    public static int getRequiredRotation(Camera cam, Activity act, int camId, boolean preview){
        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(camId,camInfo);
        int deviceRotation = act.getWindowManager().getDefaultDisplay().getRotation();
        int deviceAngle = 0;
        switch (deviceRotation) {
            case Surface.ROTATION_0: deviceAngle = 0; break;
            case Surface.ROTATION_90: deviceAngle = 90; break;
            case Surface.ROTATION_180: deviceAngle = 180; break;
            case Surface.ROTATION_270: deviceAngle = 270; break;
        }
        int result;
        if(preview){
            //rotate
            result = (camInfo.orientation + deviceAngle) % 360;
            //account for front-camera mirroring
            result = (360 - result) % 360;
        }
        else{
            result = (camInfo.orientation + deviceAngle) % 360;
        }
        return result;
    }


    //gets the camera id number of the front camera (-1 if none exists)
    public static int getFrontCameraId(){
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return camIdx;
            }
        }
        return -1;
    }
}
