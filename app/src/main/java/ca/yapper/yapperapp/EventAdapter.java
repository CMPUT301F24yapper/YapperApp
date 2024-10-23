//package ca.yapper.yapperapp;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.firebase.ui.database.FirebaseRecyclerAdapter;
//import com.firebase.ui.database.FirebaseRecyclerOptions;
//import com.google.firebase.database.DatabaseReference;
//
///** Create a custom adapter for RecyclerView by extending FirebaseRecyclerAdapter.
// This will bind the Firebase data to the RecyclerView. **/
//public class EventAdapter extends FirebaseRecyclerAdapter<Event, EventAdapter.EventViewHolder> {
//    // implement method...
//    // e.g... (chatGPT):
//    /**   // Constructor
//     public EventAdapter(@NonNull FirebaseRecyclerOptions<Event> options) {
//     super(options);
//     }
//
//     @Override
//     protected void onBindViewHolder(@NonNull EventViewHolder holder, int position, @NonNull Event model) {
//     holder.eventName.setText(model.getName());
//     holder.eventDescription.setText(model.getDescription());
//     holder.eventDate.setText(model.getDate());
//     }
//
//     @NonNull
//     @Override
//     public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//     View view = LayoutInflater.from(parent.getContext())
//     .inflate(R.layout.item_event, parent, false);
//     return new EventViewHolder(view);
//     }
//
//     public static class EventViewHolder extends RecyclerView.ViewHolder {
//     TextView eventName, eventDescription, eventDate;
//
//     public EventViewHolder(@NonNull View itemView) {
//     super(itemView);
//     eventName = itemView.findViewById(R.id.event_name);
//     eventDescription = itemView.findViewById(R.id.event_description);
//     eventDate = itemView.findViewById(R.id.event_date);
//     }
//     }
//     **/
//}
