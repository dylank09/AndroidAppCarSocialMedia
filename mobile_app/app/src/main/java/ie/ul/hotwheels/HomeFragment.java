package ie.ul.hotwheels;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private StorageReference storageReference;
    private ArrayList<Post> followingPosts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        //Gets the currentUser from FireBase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        //Gets Firebase storage, where we store images
        storageReference = FirebaseStorage.getInstance().getReference();
        //Following Posts only stores post of people you follow
        followingPosts = new ArrayList<>();

        //Gets all the posts from the firestore of people you follow
        fStore.collection("users").document(currentUser.getUid())
                .collection("following")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshot) {
                        //Checks if following exists for the user, do you follow anyone?
                        if(!documentSnapshot.isEmpty()) {
                            //If it does, it goes through each user that you follow
                            documentSnapshot.getDocuments().forEach( user -> {
                                //Gets the userName and userID of the user that you follow
                                String userName = user.getString("username");
                                String userID = user.getString("userID");
                                //We now get all the posts that user has created
                                fStore.collection("users")
                                        .document(user.getString("userID")).collection("posts")
                                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @RequiresApi(api = Build.VERSION_CODES.O)
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                //We loop through all that users posts
                                                queryDocumentSnapshots.getDocuments().forEach( post -> {
                                                    //Gets the Firebase storage reference of the image
                                                    //for the post
                                                    StorageReference postRef =  storageReference
                                                            .child("users/"+ user.getString("userID")
                                                                    +"/posts/"+ post.getString("postId"));
                                                    //Creates a new post object which stores all the relevant data of the post
                                                    Post userPost = new Post(post.getString("model"), post.getString("postId"), userName, userID, post.getString("brand"),
                                                            post.getString("description"), postRef, LocalDateTime.now());
                                                    //Gets the date when the post was added
                                                    String dateTime = post.getString("dateAdded");
                                                    LocalDateTime dateAdded = LocalDateTime.parse(dateTime);
                                                    //Sets the date to the date it was added
                                                    userPost.setDateAdded(dateAdded);
                                                    //Adds each post to the array list
                                                    followingPosts.add(userPost);
                                                });
                                                //For the home fragment, sort the arraylist by date
                                                //added, this sorts the newest to the oldest, newest at the top
                                                Collections.sort(followingPosts);
                                                //Adds the arraylist of posts into the recycler
                                                recyclerView = view.findViewById(R.id.PostR);
                                                recyclerView.setHasFixedSize(true);
                                                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
                                                recyclerView.setAdapter(new PostAdapter(followingPosts));
                                            }
                                        });
                            });
                        //If the user does not follow anyone, it alerts them
                        } else Toast.makeText(getContext(), "Follow People to see posts here.",
                                    Toast.LENGTH_SHORT).show();
                    }
                });
        return view;
    }
}
