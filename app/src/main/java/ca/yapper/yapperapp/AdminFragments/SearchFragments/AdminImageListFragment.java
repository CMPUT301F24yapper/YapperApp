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

public class AdminImageListFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminImageAdapter adapter;
    private List<String> imageList;
    private LinearLayout emptyStateLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_imagelist, container, false);

        recyclerView = view.findViewById(R.id.images_recycler_view);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        imageList = new ArrayList<>();
        adapter = new AdminImageAdapter(imageList, getContext());
        recyclerView.setAdapter(adapter);

        loadImages();

        return view;
    }

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