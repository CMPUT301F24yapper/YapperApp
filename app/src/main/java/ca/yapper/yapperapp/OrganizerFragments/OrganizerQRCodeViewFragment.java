package ca.yapper.yapperapp.OrganizerFragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.WriterException;

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


    /**
     * Inflates the fragment layout, retrieves the event ID from arguments,
     * generates a QR code for the event, and displays it in an ImageView.
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_viewqrcode, container, false);

        imageView = view.findViewById(R.id.qr_code_view);

        Bundle args = getArguments();
        if (args == null || !args.containsKey("0")) {
            Toast.makeText(getContext(), "Error: Event not found", Toast.LENGTH_SHORT).show();
            return view;
        }
        eventId = args.getString("0");

        try {
            QRCode = new qrCode(eventId);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }

        QRCodeIMG = QRCode.convertToIMG();
        imageView.setImageBitmap(QRCodeIMG);

        return view;
    }
}
