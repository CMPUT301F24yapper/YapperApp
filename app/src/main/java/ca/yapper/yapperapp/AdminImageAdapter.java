package ca.yapper.yapperapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import ca.yapper.yapperapp.Databases.AdminDatabase;

/**
 * This is the adapter for the images page that admins can browse
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageViewHolder> {
    private final List<ImageData> images;
    private final Context context;


    /**
     * Class that holds image data
     */
    public static class ImageData {
        String base64Image;
        String documentId;
        String documentType; // "event" or "user"
        String fieldName;    // "posterBase64" or "profileImage"

        /**
         * Constructor for the image data class
         *
         * @param base64Image a compressed image
         * @param documentId id of the document we wish to go to
         * @param documentType type of the document we wish to go to
         * @param fieldName name of the field we wish to interact with
         */
        public ImageData(String base64Image, String documentId, String documentType, String fieldName) {
            this.base64Image = base64Image;
            this.documentId = documentId;
            this.documentType = documentType;
            this.fieldName = fieldName;
        }
    }


    /**
     * Adapter for images
     *
     * @param images a list of images
     * @param context the environmental data from the system
     */
    public AdminImageAdapter(List<ImageData> images, Context context) {
        this.images = images;
        this.context = context;
    }


    /**
     * This function inflates a layout and creates a new view for an image
     *
     * @param parent The parent view group for the new view
     * @param viewType The view type
     *
     * @return the new view
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_imageitem, parent, false);
        return new ImageViewHolder(view);
    }


    /**
     * This function changes the fragment depending on what image item in the list was clicked
     *
     * @param holder The ViewHolder which will represents the data at this position
     * @param position index of item in list
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageData imageData = images.get(position);
        if (imageData.base64Image != null) {
            byte[] decodedString = Base64.decode(imageData.base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imagePlaceholder.setImageBitmap(decodedByte);
        }

        holder.deleteIcon.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                ImageData currentImage = images.get(adapterPosition);
                AdminDatabase.deleteImage(currentImage.documentId, currentImage.documentType, currentImage.fieldName)
                        .addOnSuccessListener(aVoid -> {
                            images.remove(adapterPosition);
                            notifyItemRemoved(adapterPosition);
                        });
            }
        });
    }


    /**
     * Returns the size of the list that displays the events
     *
     * @return size of the image list
     */
    @Override
    public int getItemCount() {
        return images.size();
    }


    /**
     * ViewHolder used for the image data
     */
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imagePlaceholder;
        ImageView deleteIcon;

        ImageViewHolder(View itemView) {
            super(itemView);
            imagePlaceholder = itemView.findViewById(R.id.image_placeholder);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }
    }
}