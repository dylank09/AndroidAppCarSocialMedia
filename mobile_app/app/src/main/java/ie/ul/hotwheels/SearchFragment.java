package ie.ul.hotwheels;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;


public class SearchFragment extends Fragment {

    private SearchView searchView;
    private RecyclerView userRecycler;
    private RecyclerView recyclerView;
    private ArrayList<User> users;
    private UserAdapter userAdapter;
    private ArrayList<User> filteredList;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search,container,false);

        storageReference = FirebaseStorage.getInstance().getReference();
        users = fetchUsers();
        filteredList = new ArrayList<>();
        userAdapter = new UserAdapter(users);
        userRecycler = v.findViewById(R.id.userRecycler);

        //gets and sets the layout parameters
        searchView = v.findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(true);
        searchView.setFocusable(true);
        searchView.setIconified(false);

        /*sets up recyclerview */
        userRecycler.setHasFixedSize(true);
        userRecycler.setLayoutManager(new LinearLayoutManager(v.getContext()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter users list when query submitted
                filteredList.clear();
                for (User row : users) {

                    if (row.getUsername().toLowerCase().contains(query.toLowerCase())) {
                        filteredList.add(row);
                    }
                }
                if(filteredList.size() > 0) {
                    userRecycler.setAdapter(new UserAdapter(filteredList));
                }
                else {
                    Toast.makeText(getActivity(), "No users found!",
                            Toast.LENGTH_LONG).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return false;
            }
        });

        return v;
    }

    /* gets the list of users on the database */
    protected ArrayList<User> fetchUsers() {
        ArrayList<User> usersList = new ArrayList<>(); //arraylist of users
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();

        Task<QuerySnapshot> documentReference = fstore.collection("users")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshot) {
                        documentSnapshot.getDocuments().forEach( t -> {

                            //get the user required from the database
                            int startOfID = t.toString().indexOf("users/");
                            int endOfID = t.toString().indexOf(", ");
                            String username = t.getString("username");
                            String userID = t.toString().substring(startOfID+6, endOfID);
                            StorageReference profRef;
                            profRef = storageReference.child("users/" + userID + "/profile.jpg"); //place image next to username in search
                            User user = new User(username, userID, profRef);
                            usersList.add(user); //add the user to the list
                        });
                    }
                });
        return usersList;
    }
}

