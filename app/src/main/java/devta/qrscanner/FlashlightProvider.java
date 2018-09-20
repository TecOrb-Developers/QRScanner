package devta.qrscanner;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

/**
 * author @Divyanshu Tayal
 */

class FlashlightProvider {

    private static final String TAG = FlashlightProvider.class.getSimpleName();
    private Camera mCamera;
    private Camera.Parameters parameters;
    private CameraManager camManager;
    private Context context;
    private boolean isFlashOn;

    FlashlightProvider(Context context) {
        this.context = context;
    }

    public void turnFlashlightOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                camManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                String cameraId = null; // Usually front camera is at 0 position.
                if (camManager != null) {
                    cameraId = camManager.getCameraIdList()[0];
                    camManager.setTorchMode(cameraId, true);
                    isFlashOn = true;
                }
            } catch (CameraAccessException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            mCamera = Camera.open();
            parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            isFlashOn = true;
        }
    }

    public void turnFlashlightOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                String cameraId;
                camManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                if (camManager != null) {
                    cameraId = camManager.getCameraIdList()[0]; // Usually front camera is at 0 position.
                    camManager.setTorchMode(cameraId, false);
                    isFlashOn = false;
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            mCamera = Camera.open();
            parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
            mCamera.stopPreview();
            isFlashOn = false;
        }
    }

    public boolean isFlashOn() {
        return isFlashOn;
    }
}
