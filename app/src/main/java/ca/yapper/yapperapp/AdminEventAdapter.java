package ca.yapper.yapperapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ca.yapper.yapperapp.UMLClasses.Event;

/**
 * This is the adapter for the event page that admins can browse
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {
    private final List<Event> eventList;
    private final Context context;


    /**
     *
     *
     * @param eventList an event list for the adapter
     * @param context environmental data from the system
     */
    public AdminEventAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }


    /**
     * This function inflates a layout and creates a new view for an event
     *
     * @param parent The parent view group for the new view
     * @param viewType The view type
     *
     * @return the new view
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_eventitem, parent, false);
        return new EventViewHolder(view);
    }


    /**
     * This function changes the fragment depending on what event item in the list was clicked
     *
     * @param holder The ViewHolder which will represents the data at this position
     * @param position index of item in list
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventTitle.setText(event.getName());
        holder.eventDate.setText(event.getDate_Time());
        holder.eventLocation.setText(event.getFacilityName());

        holder.itemView.setOnClickListener(v -> {
            AdminRemoveEventFragment fragment = new AdminRemoveEventFragment();
            Bundle args = new Bundle();
            args.putString("eventId", String.valueOf(event.getDocumentId()));
            fragment.setArguments(args);

            ((FragmentActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }


    /**
     * Returns the size of the list that displays the events
     *
     * @return size of the event list
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }


    /**
     * ViewHolder used for the event data
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle;
        TextView eventDate;
        TextView eventLocation;

        EventViewHolder(View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.event_title);
            eventDate = itemView.findViewById(R.id.event_date);
            eventLocation = itemView.findViewById(R.id.event_location);
        }
    }
}