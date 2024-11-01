package ca.yapper.yapperapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class EntrantEventFragment extends Fragment {

    private FirebaseFirestore db;
    private String eventId;
    private TextView eventTitle;
    private Bundle eventParameters;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_event, container, false);

        eventTitle = view.findViewById(R.id.event_title);

        db = FirebaseFirestore.getInstance();
        eventParameters = getArguments();
        eventId = eventParameters.getString("0");

        eventTitle.setText(eventId);

        return view;
    }
}
