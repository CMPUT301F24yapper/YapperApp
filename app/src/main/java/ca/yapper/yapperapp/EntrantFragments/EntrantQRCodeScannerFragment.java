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
/**
 * EntrantQRCodeScannerFragment enables Entrants to scan QR codes for event-related purposes.
 * The fragment manages camera permissions, QR code scanning setup, and processing scanned results.
 */
public class EntrantQRCodeScannerFragment extends Fragment {

    private BarcodeView barcodeScan;
    private CameraSettings settings;
    private ViewfinderView overlay;
    private FirebaseFirestore db;
    private Bundle eventData;
    private BarcodeCallback scanningResult;


    /**
     * Inflates the fragment layout, initializes Firebase and the QR code scanner,
     * and sets up the camera and overlay views for scanning.
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
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


    /**
     * Resumes the QR code scanning process when the fragment is in the foreground.
     * Sets the callback to handle scanned QR code results.
     */
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


    /**
     * Configures the barcode scanning view and checks for necessary camera permissions.
     * If permissions are granted, the camera is initialized for QR code scanning.
     *
     * @param view The root view of the fragment layout.
     * @throws CameraAccessException If there is an issue accessing the camera hardware.
     */
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


    /**
     * Displays an overlay on the scanning view for enhanced UI feedback during QR code scanning.
     *
     * @param view The root view of the fragment layout.
     */
    private void showOverlay(View view){
        overlay = view.findViewById(R.id.viewfinder);
        overlay.setCameraPreview(barcodeScan);
    }


    /**
     * Checks for camera permissions, requesting them if not already granted.
     * Logs the permission status for debugging purposes.
     */
    private void checkCameraPermissions() {
        String[] permissions = {"android.permission.CAMERA"};
        if (ContextCompat.checkSelfPermission(getContext(), "android.permission.CAMERA") != 0) {
            requestPermissions(permissions, 1); // Request code is a tag
            Log.d("Camera", "Camera Permissions Given");
        }
    }


    /**
     * Queries Firestore for event details based on the scanned QR code result,
     * displaying the event information in a new fragment if found.
     *
     * @param db FirebaseFirestore instance used to access Firestore.
     * @param QRScanResult The scanned QR code result, typically an event identifier.
     */
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
