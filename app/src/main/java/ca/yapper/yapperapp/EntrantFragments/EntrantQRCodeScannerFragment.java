package ca.yapper.yapperapp.EntrantFragments;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import ca.yapper.yapperapp.EventDetailsFragment;
import ca.yapper.yapperapp.R;

public class EntrantQRCodeScannerFragment extends Fragment {

    private BarcodeView barcodeScan;
    private CameraSettings settings;
    private ViewfinderView overlay;
    private FirebaseFirestore db;
    private Bundle eventData;
    private BarcodeCallback scanningResult;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_qrscanner, container, false);

        db = FirebaseFirestore.getInstance(); // For Checking what event the QRCode is from

        try {
            initializeScan(view);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
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

            getEvent(db, String.valueOf(QRScanResult));
        };
        barcodeScan.resume();
        barcodeScan.decodeContinuous(scanningResult);
    }



    private void initializeScan(View view) throws CameraAccessException {
        barcodeScan = view.findViewById(R.id.barcode_view);
        settings = barcodeScan.getCameraSettings();
        checkCameraPermissions();

        if (settings.getRequestedCameraId() != 1) {
            settings.setRequestedCameraId(1);
            // If your scanner is displaying pixelated front Camera, change both 1's to 0's
        }

        settings.setAutoFocusEnabled(true);
        barcodeScan.setCameraSettings(settings);
    }



    private void showOverlay(View view){
        overlay = view.findViewById(R.id.viewfinder);
        overlay.setCameraPreview(barcodeScan);
    }



    private void checkCameraPermissions() {
        String[] permissions = {"android.permission.CAMERA"};
        if (ContextCompat.checkSelfPermission(getContext(), "android.permission.CAMERA") != 0) {
            requestPermissions(permissions, 1); // Request code is a tag
            Log.d("Camera", "Camera Permissions Given");
        }
    }



    private void getEvent(FirebaseFirestore db, String QRScanResult) {
        // Check if QRScanResult contains extra segments, and extract the last segment if needed
        String[] segments = QRScanResult.split("/");
        String documentId = segments[segments.length - 1];

        db.collection("Events").document(documentId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                eventData = new Bundle();
                eventData.putString("0", documentId);

                EventDetailsFragment newFragment = new EventDetailsFragment();
                newFragment.setArguments(eventData);
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, newFragment).commit();
            } else {
                Log.d("A", "Event not found for QRScanResult: " + documentId, task.getException());
            }
        });
    }
}
