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

import ca.yapper.yapperapp.UMLClasses.Event;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> {

    private List<Event> eventList;
    private Context context;

    public EventsAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    @NonNull
    @Override
    public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem_event, parent, false);
        return new EventsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventsViewHolder holder, int position) {
        Event event = eventList.get(position);

        // Set the text views
        holder.eventNameTextView.setText(event.getEventName());
        holder.eventDateTextView.setText(event.getEventDateTime());
        holder.eventLocationTextView.setText(event.getEventFacilityName());

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();

            // Create bundle with all event details
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

            eventDetailsFragment.setArguments(bundle);

            // Replace current fragment with event details fragment
            FragmentTransaction transaction = ((FragmentActivity) context)
                    .getSupportFragmentManager()
                    .beginTransaction();

            transaction.replace(R.id.fragment_container, eventDetailsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventsViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView;
        TextView eventDateTextView;
        TextView eventLocationTextView;

        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.event_name);
            eventDateTextView = itemView.findViewById(R.id.event_date);
            eventLocationTextView = itemView.findViewById(R.id.event_location);
        }
    }
}