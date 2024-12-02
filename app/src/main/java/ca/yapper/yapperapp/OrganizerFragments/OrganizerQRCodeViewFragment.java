package ca.yapper.yapperapp.OrganizerFragments;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.qrCode;

/**
 * OrganizerQRCodeViewFragment displays a QR code for a specific event.
 * The fragment generates a QR code based on the event ID and displays it
 * in an ImageView for the organizer to share with participants.
 */
public class OrganizerQRCodeViewFragment extends Fragment {
    private String eventId;
    private qrCode QRCode;
    private Bitmap QRCodeIMG;
    private ImageView imageView;
    private View shareButton;
    private View saveButton;


    /**
     * Inflates the fragment layout, retrieves the event ID from arguments,
     * generates a QR code for the event, and displays it in an ImageView.
     *
     * @param inflater           LayoutInflater used to inflate the fragment layout.
     * @param container          The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_viewqrcode, container, false);

        imageView = view.findViewById(R.id.qr_code_view);
        shareButton = view.findViewById(R.id.share_button);
        saveButton = view.findViewById(R.id.save_button);

        Bundle args = getArguments();
        if (args == null || !args.containsKey("0")) {
            Toast.makeText(getContext(), "Error: Event not found", Toast.LENGTH_SHORT).show();
            return view;
        }
        eventId = args.getString("0");

        try {
            QRCode = new qrCode(eventId);
            QRCodeIMG = QRCode.convertToIMG();
            imageView.setImageBitmap(QRCodeIMG);
        } catch (WriterException e) {
            Toast.makeText(getContext(), "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        shareButton.setOnClickListener(v -> shareQRCode());
        saveButton.setOnClickListener(v -> saveQRCodeToGallery());

        return view;
    }

    private void shareQRCode() {
        try {
            // Save bitmap to cache directory
            File cachePath = new File(getContext().getCacheDir(), "images");
            cachePath.mkdirs(); // Create directory if not exists
            File file = new File(cachePath, "qrcode.png");
            FileOutputStream stream = new FileOutputStream(file);
            QRCodeIMG.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // Get URI using FileProvider
            Uri contentUri = FileProvider.getUriForFile(getContext(), "ca.yapper.yapperapp.fileprovider", file);

            if (contentUri != null) {
                // Create intent to share image
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, getContext().getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to share QR Code", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void saveQRCodeToGallery() {
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "QRCode_" + eventId + ".png");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/YapperApp");
                Uri uri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = getContext().getContentResolver().openOutputStream(uri);
            } else {
                // For older versions
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/YapperApp";
                File file = new File(imagesDir);
                if (!file.exists()) {
                    file.mkdir();
                }
                File image = new File(imagesDir, "QRCode_" + eventId + ".png");
                fos = new FileOutputStream(image);
            }
            QRCodeIMG.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(getContext(), "QR Code saved to gallery.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to save QR Code.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
