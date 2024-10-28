package ca.yapper.yapperapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class EntrantQRCodeScannerFragment extends Fragment {

    private BarcodeView barcodeView;
    private TextView scannerResult;
    private ImageView QRCodeImage;
    private ActivityResultLauncher<ScanOptions> cameraScan;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_qrscanner, container, false);

        barcodeView = view.findViewById(R.id.barcode_view);
        scannerResult = view.findViewById(R.id.scanner_result);
        QRCodeImage = view.findViewById(R.id.image_view);
        Button testQRScannerButton = view.findViewById(R.id.QR_scanner_button);

        ScanOptions cameraOptions = new ScanOptions()
                .setCameraId(0)
                .setDesiredBarcodeFormats(String.valueOf(BarcodeFormat.QR_CODE))
                .setPrompt("Scanning for QR codes")
                .setOrientationLocked(true);

        cameraScan = registerForActivityResult(new ScanContract(), qrCodeValue -> {
            scannerResult.setText(qrCodeValue.getContents());
        });

        testQRScannerButton.setOnClickListener(v -> {
            QRCodeWriter qrcodeManager = new QRCodeWriter();
            String testData = "Ruffles test case2";
            try {
                BitMatrix qrCode = qrcodeManager.encode(testData, BarcodeFormat.QR_CODE, 500, 500);
                Bitmap codeIMG = convertingBitMatrix(qrCode);
                QRCodeImage.setImageBitmap(codeIMG);

                cameraScan.launch(cameraOptions);
            } catch (WriterException e) {
                Log.d("QR Code Error", "Unable to encode: " + testData);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        BarcodeCallback result = QRScanResult -> {
            scannerResult.setText(QRScanResult.toString());
            barcodeView.pause();
        };

        barcodeView.resume();
        barcodeView.decodeContinuous(result);
    }

    private Bitmap convertingBitMatrix(BitMatrix qrcode) {
        Bitmap img = Bitmap.createBitmap(qrcode.getWidth(), qrcode.getHeight(), Bitmap.Config.ARGB_8888);
        for (int i = 0; i < qrcode.getHeight(); i++) {
            for (int j = 0; j < qrcode.getWidth(); j++) {
                img.setPixel(i, j, qrcode.get(i, j) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return img;
    }
}
