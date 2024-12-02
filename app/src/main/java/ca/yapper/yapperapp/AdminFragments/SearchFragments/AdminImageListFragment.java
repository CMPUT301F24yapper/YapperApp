package ca.yapper.yapperapp.AdminFragments.SearchFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import ca.yapper.yapperapp.AdminImageAdapter;
import ca.yapper.yapperapp.Databases.AdminDatabase;
import ca.yapper.yapperapp.R;

/**
 * Fragment to display the images stored in the database as lists that only admins can browse.
 */
public class AdminImageListFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminImageAdapter adapter;
    private List<AdminImageAdapter.ImageData> imageList;
    private LinearLayout emptyStateLayout;


    /**
     * Inflates the fragments layout, sets up views, and starts search function all related to
     * images from the app
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     *
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_imagelist, container, false);

        recyclerView = view.findViewById(R.id.images_recycler_view);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);

        imageList = new ArrayList<>();
        adapter = new AdminImageAdapter(imageList, getContext());
        recyclerView.setAdapter(adapter);

        loadImages();

        return view;
    }


    /**
     * Function to obtain images from the database and update the UI accordingly, if no images exist
     * then displays a custom message.
     */
    private void loadImages() {
        AdminDatabase.getAllImages()
                .addOnSuccessListener(images -> {
                    imageList.clear();
                    imageList.addAll(images);
                    adapter.notifyDataSetChanged();

                    if (imageList.isEmpty()) {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyStateLayout.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }
}