package ca.yapper.yapperapp.AdminFragments.SearchFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.LinearLayout;

import ca.yapper.yapperapp.Adapters.AdminUserAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.User;
import ca.yapper.yapperapp.Databases.AdminDatabase;

/**
 * Fragment to display the user profiles stored in the database as lists that only admins can browse
 */
public class AdminProfileListFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private List<User> userList;
    private EditText searchBar;
    private LinearLayout emptyStateLayout;


    /**
     * Inflates the fragments layout, sets up views, and starts search function all related to
     * user profiles from the app
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
        View view = inflater.inflate(R.layout.admin_profilelist, container, false);

        recyclerView = view.findViewById(R.id.profiles_recycler_view);
        searchBar = view.findViewById(R.id.search_bar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        adapter = new AdminUserAdapter(userList, getContext());
        recyclerView.setAdapter(adapter);

        loadUsers();
        setupSearch();

        return view;
    }


    /**
     * Obtains profile information from all users and updates the list adapters.
     */
    private void loadUsers() {
        AdminDatabase.getAllUsers()
                .addOnSuccessListener(users -> {
                    userList.clear();
                    userList.addAll(users);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });
    }


    /**
     * Function that sets up the search functionality for admin lists of user profiles
     * Uses the listener to detect changes in text and update whats shown for the lists accordingly.
     */
    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().toLowerCase();
                filterUsers(searchText);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    /**
     *  Function that filters profile list based on searchText string and updates the UI with
     *  an adapter.
     * @param searchText Text from user search bar input, used to filter the profile list.
     */
    private void filterUsers(String searchText) {
        List<User> filteredList = new ArrayList<>();
        for (User user : userList) {
            if (user.getName().toLowerCase().contains(searchText)) {
                filteredList.add(user);
            }
        }
        adapter = new AdminUserAdapter(filteredList, getContext());
        recyclerView.setAdapter(adapter);
        updateEmptyState();
    }


    /**
     * Function to check if list is empty and change visibility accordingly. If list is empty,
     * it will display a custom message for user convenience .
     */
    private void updateEmptyState() {
        if (userList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}