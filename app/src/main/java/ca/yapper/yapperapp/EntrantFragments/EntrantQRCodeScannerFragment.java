package ca.yapper.yapperapp.EntrantFragments;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
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
    private Object cameraSetup;
    private CameraManager cameras;
    private String theCamera;
    private int realCameraFacing;

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

            // Here we switch fragments
            getEvent(db, String.valueOf(QRScanResult));
        };
        barcodeScan.resume();
        barcodeScan.decodeContinuous(scanningResult);
        //barcodeScan.resume();
    }

    private void initializeScan(View view) throws CameraAccessException {
        barcodeScan = view.findViewById(R.id.barcode_view);
        settings = barcodeScan.getCameraSettings();
        checkCameraPermissions(); // We have to ask for camera permissions otherwise it only shows a black screen

        // Here we get access to the cameras, for dealing with camera bug across devices
        // and different camera setups.
//        cameraSetup = getActivity().getSystemService(Context.CAMERA_SERVICE);
//        cameras = (CameraManager) cameraSetup;
//
//        String[] cameraIds = cameras.getCameraIdList();
//
//        int count = 0;
//
//        for (int i = 0; i <= cameraIds.length; i++){
//            theCamera = cameraIds[i];
//
//            CameraCharacteristics cameraConfig = cameras.getCameraCharacteristics(theCamera);
//            realCameraFacing = cameraConfig.get(CameraCharacteristics.LENS_FACING);
//            Log.d("QR Code", "The Front face camera Id: " + realCameraFacing + "\nThe actual camera: " + theCamera + " cameraIds" + cameraIds);
//            count++;
//
//            if (realCameraFacing == 1){
//                // realCameraId is the actual camera id associated with the selected camera we have
//                // This value changes based on the users device
//                if (settings.getRequestedCameraId() != Integer.parseInt(theCamera)){ // Here we are certain that
//                    // the realCameraId is for the back facing camera, regardless of how the device stores it.
//                    // Sets the default camera to be the front facing camera, in case its not
//                    settings.setRequestedCameraId(Integer.parseInt(theCamera));
//                }
//
//            }
//        }

        if (settings.getRequestedCameraId() != -1) {
            // Sets the default camera to be the front facing camera, in case its not
            settings.setRequestedCameraId(-1); // NOTE: If your scanner is displaying pixelated thing, change both 1's to 0's
        }

        //settings.setRequestedCameraId(1);
        settings.setAutoFocusEnabled(true);
        barcodeScan.setCameraSettings(settings);
    }

    private void showOverlay(View view){
        overlay = view.findViewById(R.id.viewfinder);
        // attaching overlay to currently opened camera preview(the barcodeView extends camera preview)
        overlay.setCameraPreview(barcodeScan);
    }

    private void checkCameraPermissions() {
        String[] permissions = {"android.permission.CAMERA"};
        if (ContextCompat.checkSelfPermission(getContext(), "android.permission.CAMERA") != 0) {
            // This launches the pop up showing options for the camera permissions.
            requestPermissions(permissions, 1); // The request code is just like a tag, can be any integer
            Log.d("Camera", "Camera Permissions Given");
        }
        //Log.d("S", Integer.toString(ContextCompat.checkSelfPermission(this.getContext(), "android.permission.CAMERA");)); // FOR TESTING
    }

    private void getEvent(FirebaseFirestore db, String QRScanResult) {
        // Check if QRScanResult contains extra segments, and extract the last segment if needed
        String[] segments = QRScanResult.split("/");  // Split by "/" to get segments
        String documentId = segments[segments.length - 1]; // Use the last segment as the document ID

        db.collection("Events").document(documentId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                eventData = new Bundle();
                eventData.putString("0", documentId); // Pass just the document ID

                EventDetailsFragment newFragment = new EventDetailsFragment();
                newFragment.setArguments(eventData);
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, newFragment).commit();
            } else {
                Log.d("A", "Event not found for QRScanResult: " + documentId, task.getException());
            }
        });
    }
}
