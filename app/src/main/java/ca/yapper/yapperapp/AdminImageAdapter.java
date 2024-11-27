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

public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageViewHolder> {
    private List<String> base64Images;
    private Context context;

    public AdminImageAdapter(List<String> base64Images, Context context) {
        this.base64Images = base64Images;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_imageitem, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String base64Image = base64Images.get(position);
        if (base64Image != null) {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imagePlaceholder.setImageBitmap(decodedByte);
        }
    }

    @Override
    public int getItemCount() {
        return base64Images.size();
    }

    public void updateData(List<String> newImages) {
        this.base64Images = newImages;
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imagePlaceholder;

        ImageViewHolder(View itemView) {
            super(itemView);
            imagePlaceholder = itemView.findViewById(R.id.image_placeholder);
        }
    }
}