package devta.qrscanner;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActivityDashboard extends AppCompatActivity
        implements PreviewCallbacks, View.OnClickListener{

    private final int REQ_CAMERA = 111;
    private GraphicOverlay mGraphicOverlay;
    private CameraSource mCameraSource;
    private CameraSourcePreview mCameraPreview;
    private FlashlightProvider flashlightProvider;
    private ImageView vFlash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        vFlash = findViewById(R.id.btn_flash);
        vFlash.setOnClickListener(this);
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            vFlash.setVisibility(View.GONE);
        }else {
            flashlightProvider = new FlashlightProvider(this);
        }
        mGraphicOverlay = findViewById(R.id.overlay);
        mGraphicOverlay.add(new BarcodeGraphic(mGraphicOverlay));
        mCameraPreview = findViewById(R.id.camera_view);
        requestCameraPermissions();
    }

    private void requestCameraPermissions(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
        } else {
            startBarcodeReader();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_flash:
                if(flashlightProvider.isFlashOn()){
                    flashlightProvider.turnFlashlightOff();
                    vFlash.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_flash_off));
                }else {
                    flashlightProvider.turnFlashlightOn();
                    vFlash.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_flash_on));
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if(requestCode == REQ_CAMERA){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBarcodeReader();
            } else {
                showErrorDialog();
            }
        }
    }

    private void showInfoDialog(){
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
        infoDialog.setTitle(R.string.app_name);
        infoDialog.setMessage("This app need Camera permissions to function," +
                " please allow this app to use Camera in the next dialog.");
        infoDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                requestCameraPermissions();
            }
        });
        infoDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        infoDialog.show();
    }

    private void showErrorDialog(){
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
        errorDialog.setTitle(R.string.app_name);
        errorDialog.setMessage("You have not enabled this app to use Camera, please allow this app" +
                "to use camera from the app setting.");
        errorDialog.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        errorDialog.show();
    }

    private void startBarcodeReader(){
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay);
        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());

        if(!barcodeDetector.isOperational()) return;

        mCameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build();

        try{
            mCameraPreview.start(mCameraSource, mGraphicOverlay);
        }catch (IOException ie){
            Log.e("Camera Preview", ie.getMessage());
        }

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> barcodeList = detections.getDetectedItems();
                if(barcodeList == null || barcodeList.size() == 0) return;

                String[] barcodeValues = new String[barcodeList.size()];

                for(int i=0; i<barcodeList.size(); i++){
                    barcodeValues[i] = barcodeList.get(barcodeList.keyAt(i)).displayValue;
                }
                Intent intent = new Intent(ActivityDashboard.this, ActivityResult.class);
                intent.putExtra(ActivityResult.kResults, barcodeValues);
                startActivity(intent);
            }
        });
    }

    private void startCameraSource() {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) return;
        if (mCameraSource != null) {
            try {
                mCameraPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e("Camera Source", "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    @Override
    public void onCameraPermissionsRequired() {
        requestCameraPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraPreview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }
}
