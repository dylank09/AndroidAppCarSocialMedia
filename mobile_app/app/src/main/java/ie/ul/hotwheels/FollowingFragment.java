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

public class FollowingFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<User> users;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_following,container,false);

        //Gets the current user and the firestore
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        //An arraylist of users
        users = new ArrayList<>();
        storageReference = FirebaseStorage.getInstance().getReference();

        //Gets all the users that you follow
        fStore.collection("users")
            .document(currentUser.getUid()).collection("following")
            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onSuccess(QuerySnapshot documentSnapshot) {
                    //Loops through all the users that you follow
                    documentSnapshot.getDocuments().forEach( user -> {
                        //Gets the users ID
                        String userID =  user.getString("userID");
                        //Gets the users profile picture
                        StorageReference profRef = storageReference.child("users/" + userID + "/profile.jpg");
                        //Creates a new user
                        User userFollowing = new User(user.getString("username"), userID, profRef);
                        //Adds the user to the arraylist
                        users.add(userFollowing);
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

