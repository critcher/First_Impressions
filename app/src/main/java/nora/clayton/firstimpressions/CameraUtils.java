package nora.clayton.firstimpressions;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

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

    public static int getRequiredRotation(int id, boolean preview){
        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(id,camInfo);
        int offset = 0;
        if (preview){
            offset = 180;
        }
        return (camInfo.orientation + offset) % 360;
    }

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
