package ca.yapper.yapperapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.yapper.yapperapp.EntrantFragments.EntrantEventFragment;
import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.UMLClasses.User;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {
    private List<User> userList;
    private Context context;

    public UsersAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UsersAdapter.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_participants_item, parent, false);
        return new UsersAdapter.UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.UsersViewHolder holder, int position) {
        User user = userList.get(position);

        // Set the text views
        holder.userNameTextView.setText(user.getName());
        // other text views relevant to User?

        // Set click listener
        /**
        holder.itemView.setOnClickListener(v -> {
            EntrantEventFragment entrantEventFragment = new EntrantEventFragment();

            // Create bundle with all event details
            // is the bundle necessary (shouldn't it just be deviceId?)
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getEventQRCode() != null ?
                    event.getEventQRCode().getQRCodeValue() :
                    "sampleEventId"); // Fallback ID if no QR code
            bundle.putString("eventName", event.getEventName());
            bundle.putString("eventDateTime", event.getEventDateTime());
            bundle.putString("eventFacility", event.getEventFacilityName());
            bundle.putString("eventLocation", event.getEventFacilityLocation());
            bundle.putString("eventDeadline", event.getEventRegDeadline());
            bundle.putInt("eventAttendees", event.getEventAttendees());
            bundle.putInt("eventWaitlistCapacity", event.getEventWlCapacity());
            bundle.putBoolean("geolocationEnabled", event.isEventGeolocEnabled());

            entrantEventFragment.setArguments(bundle);

            // Replace current fragment with event details fragment
            FragmentTransaction transaction = ((FragmentActivity) context)
                    .getSupportFragmentManager()
                    .beginTransaction();

            transaction.replace(R.id.fragment_container, entrantEventFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }); **/
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.event_name);
        }
    }
}
