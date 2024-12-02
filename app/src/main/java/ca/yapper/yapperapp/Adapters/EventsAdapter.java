package ca.yapper.yapperapp.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

import ca.yapper.yapperapp.EntrantFragments.EventDetailsFragment;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;
/**
 * The EventsAdapter class provides a RecyclerView adapter for displaying a list of events.
 * It binds event data to the view items and handles user interactions with the event items.
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> {

    private final List<Event> eventList;
    private final Context context;


    /**
     * Constructs a new {@code EventsAdapter} with the specified list of events and context.
     *
     * @param eventList The list of {@link Event} objects to display in the RecyclerView.
     * @param context The context in which the adapter is used, typically an activity or fragment.
     */
    public EventsAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }


    /**
     * Creates a new {@link EventsViewHolder} by inflating the event item layout.
     * This method is called when a new view holder is needed for the RecyclerView.
     *
     * @param parent The parent view group into which the new view will be added.
     * @param viewType The type of view to create. (Not used in this implementation.)
     * @return A new {@link EventsViewHolder} for the event item.
     */
    @NonNull
    @Override
    public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem_event, parent, false);
        return new EventsViewHolder(view);
    }


    /**
     * Binds data from the specified {@link Event} object to the views in the {@link EventsViewHolder}.
     * This method is called to display data for a specific event in the RecyclerView.
     *
     * @param holder The view holder that holds the views for the event item.
     * @param position The position of the event in the event list.
     */
    @Override
    public void onBindViewHolder(@NonNull EventsViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.eventNameTextView.setText(event.getName());
        holder.eventDateTextView.setText(event.getDate_Time());
        holder.eventLocationTextView.setText(event.getFacilityName());

        holder.itemView.setOnClickListener(v -> {
            if (event.getQRCode() == null) {
                Toast.makeText(v.getContext(), "Error: Event QR code not found", Toast.LENGTH_SHORT).show();
                return;
            }


            //EVENT ID RETRIVED
            String eventId = event.getDocumentId();


            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(v.getContext(), "Error: Invalid event ID", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("0", eventId);
                eventDetailsFragment.setArguments(bundle);

                if (context instanceof FragmentActivity) {
                    FragmentTransaction transaction = ((FragmentActivity) context)
                            .getSupportFragmentManager()
                            .beginTransaction();

                    transaction.replace(R.id.fragment_container, eventDetailsFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    Log.e("EventsAdapter", "Context is not FragmentActivity");
                }
            } catch (Exception e) {
                Log.e("EventsAdapter", "Error navigating to event details", e);
                Toast.makeText(v.getContext(), "Error opening event details", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Returns the total number of events in the event list.
     *
     * @return The number of items in the event list.
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }
    /**
     * A view holder class for holding references to the views for a single event item in the RecyclerView.
     * This class is used to efficiently reuse views within the RecyclerView.
     */
    public static class EventsViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView;
        TextView eventDateTextView;
        TextView eventLocationTextView;
        /**
         * Constructs a new {@code EventsViewHolder} and binds views to the specified item view.
         *
         * @param itemView The view representing an individual event item.
         */
        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.event_name);
            eventDateTextView = itemView.findViewById(R.id.event_date);
            eventLocationTextView = itemView.findViewById(R.id.event_location);
        }
    }
}