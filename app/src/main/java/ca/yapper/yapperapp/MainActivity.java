package ca.yapper.yapperapp;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;


public class MainActivity extends AppCompatActivity {

    private BarcodeView barcodeView;
    private TextView scannerResult;
    private ImageView QRCodeImage;
    private String qrCodeString;

    // QR Code Scanner functionality --------------------------------------
    //OPTION 1 - scanner is slightly slow but screen can look nice,
    // also requires us to ask for permissions manually first otherwise displays black screen
    @Override
    protected void onResume(){
        super.onResume();
        BarcodeCallback result = QRScanResult -> {
            scannerResult.setText(QRScanResult.toString());
            //barcodeView.pause(); // Once a QR code is found we stop the scanner and clear it from the screen
            //barcodeView.setVisibility(View.GONE);
        };

        barcodeView.resume();
        barcodeView.decodeContinuous(result); // This will run until a QR code is found
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        barcodeView = findViewById(R.id.barcode_view);
        scannerResult = findViewById(R.id.scanner_result);
        QRCodeImage = findViewById(R.id.image_view);
        ScanContract scanRules = new ScanContract();

        // QR Code Scanner functionality --------------------------------------
        //OPTION 1 - scanner is slightly slow but screen can look nice,
        // also requires us to ask for permissions manually first otherwise displays black screen

        // Using a lambda expression to define what occurs once a result is given from the
        // barcode view decoder. Once we have a result, the next sequence of code is executed.

        BarcodeCallback result = QRScanResult -> {
                scannerResult.setText(QRScanResult.toString());
                //barcodeView.pause(); // Once a QR code is found we stop the scanner and clear it from the screen
                //barcodeView.setVisibility(View.GONE);
        };

        barcodeView.resume();
        barcodeView.decodeContinuous(result); // This will run until a QR code is found

        //OPTION 2 - scanner is faster but less customizable
/*
        ActivityResultLauncher<ScanOptions> cameraScanner = registerForActivityResult(scanRules, qrCodeValue -> {
            // here we use a lambda expression because the registerForActivityResult method requires a lambda expression as
            // its second parameter in order to show what we do with the results returned from the method.
            scannerResult.setText(qrCodeValue.getContents());
        });

        ScanOptions cameraOptions = new ScanOptions()
                .setCameraId(1) // for front facing camera by default
                .setDesiredBarcodeFormats(String.valueOf(BarcodeFormat.QR_CODE))
                .setPrompt("Scanning for QR codes")
                .setOrientationLocked(true); // our app isn't built for a changing portrait orientation*/

        // QR code Generator Functionality --------------------------------------
        Button testQRScannerButton = findViewById(R.id.QR_scanner_button);
        // use of an anonymous inner class to efficiently implement button logic when clicked
        testQRScannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QRCodeWriter qrcodeManager = new QRCodeWriter();
                String testData = "Ruffles test case2";
                try {
                    BitMatrix qrCode = qrcodeManager.encode(testData, BarcodeFormat.QR_CODE, 500, 500);
                    int hashData = qrCode.hashCode();

                    Log.d("QRCode", Integer.toString(hashData)); // For testing purposes -- Displays hash data in log
                    //cameraScanner.launch(cameraOptions); - part of option 2
                    Bitmap codeIMG = bitMatrixToBitmap(qrCode);  ///////////////// DELETE LATER - FOR TESTING
                    QRCodeImage.setImageBitmap(codeIMG);

                } catch (WriterException e) {
                    Log.d("QR Code Error", "Unable to encode: " + testData);
                }
            }
        });
    }



    ///////////////// DELETE LATER - FOR TESTING how a qr code looks
    private Bitmap bitMatrixToBitmap(BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bitmap;
    }
    ///////////////// DELETE LATER - FOR TESTING  how a qr code looks
}