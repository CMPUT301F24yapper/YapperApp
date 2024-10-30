package ca.yapper.yapperapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

public class EntrantQRCodeScannerFragment extends Fragment {

    private BarcodeView barcodeScan;
    private CameraSettings settings;
    private ViewfinderView overlay;
    private BarcodeCallback scanningResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_qrscanner, container, false);

        initializeScan(view);
        showOverlay(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        scanningResult = QRScanResult -> {
            Log.d("QR Code Scanned","Results from QR code scan " + QRScanResult);
            barcodeScan.pause();
            barcodeScan.setVisibility(View.GONE);
            overlay.setVisibility(View.GONE);
        };

        barcodeScan.resume();
        barcodeScan.decodeContinuous(scanningResult);
    }

    private void initializeScan(View view){
        barcodeScan = view.findViewById(R.id.barcode_view);
        settings = barcodeScan.getCameraSettings();

        if (settings.getRequestedCameraId() != 1){
            // Sets the default camera to be the front facing camera, in case its not
            settings.setRequestedCameraId(1);
        }
        settings.setAutoFocusEnabled(true);

        barcodeScan.setCameraSettings(settings);
    }

    private void showOverlay(View view){
        overlay = view.findViewById(R.id.viewfinder);
        // attaching overlay to currently opened camera preview
        overlay.setCameraPreview(barcodeScan);
    }
}
