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
import ca.yapper.yapperapp.EntrantFragments.EntrantEventFragment;

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

        holder.eventNameTextView.setText(event.getEventName());
        holder.eventDateTextView.setText(event.getEventDateTime());
        holder.eventLocationTextView.setText(event.getEventFacilityName());

        // Set an OnClickListener for the item
        holder.itemView.setOnClickListener(v -> {
            // Create a new instance of EntrantEventFragment
            EntrantEventFragment entrantEventFragment = new EntrantEventFragment();

            // Create a bundle and add the event name
            Bundle bundle = new Bundle();
            bundle.putString("0", event.getEventName()); // Key "0" with the event name
            entrantEventFragment.setArguments(bundle);

            // Replace the current fragment with EntrantEventFragment
            FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, entrantEventFragment);
            transaction.addToBackStack(null); // Add to back stack to allow navigation back
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
