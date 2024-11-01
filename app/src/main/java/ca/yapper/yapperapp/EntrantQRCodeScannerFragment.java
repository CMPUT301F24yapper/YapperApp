package ca.yapper.yapperapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

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

        //ADD METHOD TO CHECK FOR CAMERA PERMISSIONS (CheckSelfPermissions and RequestPermissions)

        db = FirebaseFirestore.getInstance(); // For Checking what event the QRCode is from

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

            // Here we switch fragments
            getEvent(db, String.valueOf(QRScanResult)); // What happens if we scan a qr code that's not from an existing event?
        };
        barcodeScan.decodeContinuous(scanningResult);
        barcodeScan.resume();
    }

    private void initializeScan(View view){
        barcodeScan = view.findViewById(R.id.barcode_view);
        settings = barcodeScan.getCameraSettings();

//        if (settings.getRequestedCameraId() != 1){
//            // Sets the default camera to be the front facing camera, in case its not
//            settings.setRequestedCameraId(0);
//        }

        settings.setRequestedCameraId(0);
        settings.setAutoFocusEnabled(true);
        barcodeScan.setCameraSettings(settings);
    }

    private void showOverlay(View view){
        overlay = view.findViewById(R.id.viewfinder);
        // attaching overlay to currently opened camera preview(the barcodeView extends camera preview)
        overlay.setCameraPreview(barcodeScan);
    }



    private void getEvent(FirebaseFirestore db, String QRScanResult){
        //REPLACE SAMPLEEVENTID WITH QRSCANRESULT ONCE QR GENERATION IS WORKING

        db.collection("Events").document("sampleEventId").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    eventData = new Bundle();
                    eventData.putString("0", QRScanResult);

                    EntrantEventFragment newFragment = new EntrantEventFragment();
                    newFragment.setArguments(eventData);
                    getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, newFragment).commit();
                } else {
                    Log.d("A","QRScanResult does not exist", task.getException());
                }
            });
    }
}
