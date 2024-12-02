package ca.yapper.yapperapp.EntrantFragments;

import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.EventDetailsFragment;
import ca.yapper.yapperapp.R;

/**
 * The fragment for entrants to scan QR Codes.
 */
public class EntrantQRCodeScannerFragment extends Fragment {

    private BarcodeView barcodeScan;
    private CameraSettings settings;
    private ViewfinderView overlay;
    private Bundle eventData;
    private BarcodeCallback scanningResult;

    /**
     *
     * Inflates the fragments layout, sets up views, sets up camera permissions and starts the QR Code scanner
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     *
     * @return
     */
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


    /**
     * Method for updates
     */
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

            EntrantDatabase.getEventByQRCode(documentId, new EntrantDatabase.OnEventFoundListener() {
                /**
                 * This function changes fragments when an event is detected by the QR code scanner
                 *
                 * @param eventId an existing event id
                 */
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
        };
        barcodeScan.resume();
        barcodeScan.decodeContinuous(scanningResult);
    }


    /**
     * This function initializes the barcode view and sets camera settings including which camera is opened
     * NOTE: if the wrong camera is opened or a black screen appears, change the 1 in settings.setRequestedCameraId(1); and
     * settings.getRequestedCameraId() != 1 to 0.
     *
     * @param view the barcode views parent view
     * @throws CameraAccessException For when the camera fails to start or cannot be accessed
     */
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


    /**
     * The screen overlay for the QR Code scanner
     * @param view The parent view for the screen overlay to use
     */
    private void showOverlay(View view){
        overlay = view.findViewById(R.id.viewfinder);
        overlay.setCameraPreview(barcodeScan);
    }


    /**
     * This function checks if the user has given camera permissions, and if not obtains them.
     */
    private void checkCameraPermissions() {
        String[] permissions = {"android.permission.CAMERA"};
        if (ContextCompat.checkSelfPermission(getContext(), "android.permission.CAMERA") != 0) {
            requestPermissions(permissions, 1);
            Log.d("Camera", "Camera Permissions Given");
        }
    }
}