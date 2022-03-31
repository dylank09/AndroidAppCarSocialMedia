package ie.ul.hotwheels;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FollowersFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<User> users;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_followers,container,false);

        //Gets the current user and the firestore
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();

        //An arraylist of users
        users = new ArrayList<>();
        storageReference = FirebaseStorage.getInstance().getReference();

        fstore.collection("users")
            .document(currentUser.getUid()).collection("followers")
            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onSuccess(QuerySnapshot documentSnapshot) {
                    documentSnapshot.getDocuments().forEach( t -> {

                        //Gets the users ID and userName
                        String username = t.getString("username");
                        String userID =  t.getString("userID");
                        //Gets the users profile picture
                        StorageReference profRef = storageReference.child("users/" + userID + "/profile.jpg");
                        //Creates a new user
                        User user = new User(username, userID, profRef);
                        //Adds the user to the arraylist
                        users.add(user);

                    });

                    //Adds the arraylist of posts into the recycler
                    recyclerView = v.findViewById(R.id.userRecycler);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));
                    recyclerView.setAdapter(new UserAdapter(users));
                }
            });
        return v;
    }
}

