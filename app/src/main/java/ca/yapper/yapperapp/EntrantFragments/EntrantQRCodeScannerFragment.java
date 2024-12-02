package ca.yapper.yapperapp.EntrantFragments;

import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import ca.yapper.yapperapp.Activities.EntrantActivity;
import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.EventDetailsFragment;
import ca.yapper.yapperapp.R;

public class EntrantQRCodeScannerFragment extends Fragment {

    private BarcodeView barcodeScan;
    private CameraSettings settings;
    private ViewfinderView overlay;
    private Bundle eventData;
    private BarcodeCallback scanningResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_qrscanner, container, false);

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

            String[] segments = QRScanResult.toString().split("/");
            String documentId = segments[segments.length - 1];

            OrganizerDatabase.checkQRCodeExists(documentId).addOnSuccessListener(exists -> {
                if (!exists) {
                    Toast.makeText(getContext(), "Invalid QR code", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getContext(), EntrantActivity.class));
                    return;
                }

                EntrantDatabase.getEventByQRCode(documentId, new EntrantDatabase.OnEventFoundListener() {
                    @Override
                    public void onEventFound(String eventId) {
                        eventData = new Bundle();
                        eventData.putString("0", eventId);
                        EventDetailsFragment newFragment = new EventDetailsFragment();
                        newFragment.setArguments(eventData);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, newFragment)
                                .commit();
                    }

                    @Override
                    public void onEventNotFound() {
                        Log.d("QRScanner", "Event not found for QRCode: " + documentId);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("QRScanner", "Error finding event", e);
                    }
                });
            });
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
            requestPermissions(permissions, 1);
            Log.d("Camera", "Camera Permissions Given");
        }
    }
}