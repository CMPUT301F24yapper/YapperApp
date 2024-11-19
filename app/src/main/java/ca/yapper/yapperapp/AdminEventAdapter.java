package ca.yapper.yapperapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ca.yapper.yapperapp.UMLClasses.Event;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {
    private List<Event> eventList;
    private Context context;

    public AdminEventAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_eventitem, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventTitle.setText(event.getName());
        holder.eventDate.setText(event.getDate_Time());
        holder.eventLocation.setText(event.getFacilityName());

        holder.itemView.setOnClickListener(v -> {
            // Implement removal confirmation dialog here
            showRemoveConfirmationDialog(event);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    private void showRemoveConfirmationDialog(Event event) {
        // Show dialog using admin_deleteconfirmation.xml
        // On confirm, call AdminDatabase.removeEvent(event.getQRCode().getHashData())
    }

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
