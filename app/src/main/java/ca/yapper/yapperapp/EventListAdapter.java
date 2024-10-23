package ca.yapper.yapperapp;

 /** Separate FirebaseRecyclerAdapters for Event and EventList
 Event Adapter: This would handle displaying all the events that a user has signed up for.
 EventList Adapter: This would handle displaying the different lists (e.g., WaitingList, ConfirmedList, etc.) for each event.
 **/
 // re. EntrantViewHolder: The EntrantViewHolder is a custom ViewHolder class used in the EventListAdapter (which handles the lists of entrants, like the WaitingList in an event).
 // ...The ViewHolder pattern is used in RecyclerViews to improve performance by recycling view components instead of creating new ones for each item. In your case, each EntrantViewHolder represents...
 // ...an individual item view for a single Entrant in the RecyclerView.
 public class EventListAdapter extends FirebaseRecyclerAdapter<Entrant, EventListAdapter.EntrantViewHolder> {
    // implementation
    // e.g. from ChatGPT:
    /**
     public EventListAdapter(@NonNull FirebaseRecyclerOptions<Entrant> options) {
     super(options);
     }

     @Override
     protected void onBindViewHolder(@NonNull EntrantViewHolder holder, int position, @NonNull Entrant model) {
     holder.entrantName.setText(model.getName());
     holder.entrantStatus.setText(model.getStatus());
     }

     @NonNull
     @Override
     public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
     View view = LayoutInflater.from(parent.getContext())
     .inflate(R.layout.item_entrant, parent, false);
     return new EntrantViewHolder(view);
     }

     public static class EntrantViewHolder extends RecyclerView.ViewHolder {
     TextView entrantName, entrantStatus;

     public EntrantViewHolder(@NonNull View itemView) {
     super(itemView);
     entrantName = itemView.findViewById(R.id.entrant_name);
     entrantStatus = itemView.findViewById(R.id.entrant_status);
     }
     }
     **/
}
