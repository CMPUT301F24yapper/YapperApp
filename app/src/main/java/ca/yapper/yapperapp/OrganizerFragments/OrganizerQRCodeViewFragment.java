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

public class OrganizerQRCodeViewFragment extends Fragment {

    private String eventId;
    private qrCode QRCode;
    private Bitmap QRCodeIMG;
    private ImageView imageView;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_viewqrcode, container, false);

        imageView = view.findViewById(R.id.qr_code_view);

        Bundle args = getArguments();
        if (args == null || !args.containsKey("0")) {
            Toast.makeText(getContext(), "Error: Event not found", Toast.LENGTH_SHORT).show();
            return view;
        }

        eventId = args.getString("0"); // getting eventID(which is also the hashdata)

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
