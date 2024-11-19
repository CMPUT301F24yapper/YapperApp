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

import ca.yapper.yapperapp.AdminUserAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.User;
import ca.yapper.yapperapp.Databases.AdminDatabase;

public class AdminProfileListFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private List<User> userList;
    private EditText searchBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_profilelist, container, false);

        recyclerView = view.findViewById(R.id.profiles_recycler_view);
        searchBar = view.findViewById(R.id.search_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        adapter = new AdminUserAdapter(userList, getContext());
        recyclerView.setAdapter(adapter);

        loadUsers();
        setupSearch();

        return view;
    }

    private void loadUsers() {
        AdminDatabase.getAllUsers()
                .addOnSuccessListener(users -> {
                    userList.clear();
                    userList.addAll(users);
                    adapter.notifyDataSetChanged();
                });
    }

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

    private void filterUsers(String searchText) {
        List<User> filteredList = new ArrayList<>();
        for (User user : userList) {
            if (user.getName().toLowerCase().contains(searchText)) {
                filteredList.add(user);
            }
        }
        adapter = new AdminUserAdapter(filteredList, getContext());
        recyclerView.setAdapter(adapter);
    }
}