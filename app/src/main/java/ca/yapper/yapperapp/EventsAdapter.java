package ca.yapper.yapperapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.yapper.yapperapp.EventDetailsFragment;
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

        holder.eventNameTextView.setText(event.getName());
        holder.eventDateTextView.setText(event.getDate_Time());
        holder.eventLocationTextView.setText(event.getFacilityName());

        holder.itemView.setOnClickListener(v -> {
            EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();

            String eventId = event.getQRCode() != null ?
                    event.getQRCode().getQRCodeValue() :
                    null;

            if (eventId == null) {
                Toast.makeText(v.getContext(), "Error: Invalid event", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("0", eventId);
            eventDetailsFragment.setArguments(bundle);

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