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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TrendingFragment extends Fragment {
    private RecyclerView recyclerView;
    private StorageReference storageReference;
    private ArrayList<Post> trendingPosts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trending, container, false);
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        //Gets the currentUser from FireBase
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        //Gets Firebase storage, where we store images
        storageReference = FirebaseStorage.getInstance().getReference();
        //Following Posts stores all post that are uploaded
        trendingPosts = new ArrayList<>();

        //Gets all the posts from the firestore
        fStore.collection("users").get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSuccess(QuerySnapshot documentSnapshot) {
                //Goes through each user that exists
                documentSnapshot.getDocuments().forEach( user -> {
                    //Gets the ID of that particular user
                    int startOfID = user.toString().indexOf("users/");
                    int endOfID = user.toString().indexOf(", ");
                    //Gets username from the firestore
                    String username = user.getString("username");
                    String userID = user.toString().substring(startOfID+6, endOfID);
                    //Goes through all the posts that user has created
                    fStore.collection("users").document(userID).collection("posts")
                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            //Goes through each post that user has
                            queryDocumentSnapshots.getDocuments().forEach( post -> {
                                //Gets the number of likes that post has
                                post.getReference().collection("likes").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        //Gets the document size, i.e. number of likes
                                        int numberOfLikes = queryDocumentSnapshots.size();
                                        //Gets the storageReference for that post
                                        StorageReference postRef =  storageReference
                                                .child("users/"+ userID
                                                        +"/posts/"+ post.getString("postId"));
                                        //Creates a new post object which stores all the relevant data of the post
                                        Post userPost = new Post(numberOfLikes, post.getString("model"),post.getString("postId"), username, userID, post.getString("brand"),
                                                post.getString("description"), postRef);
                                        //Gets the date when the post was added
                                        String dateTime = post.getString("dateAdded");
                                        LocalDateTime dateAdded = LocalDateTime.parse(dateTime);
                                        //Sets the date to the date it was added
                                        userPost.setDateAdded(dateAdded);
                                        //Adds each post to the array list
                                        trendingPosts.add(userPost);

                                        //For the trending fragment, sort the arraylist number of likes
                                        //Most liked posts at the top
                                        Collections.sort(trendingPosts, new Comparator<Post>() {
                                            @Override
                                            public int compare(Post o1, Post o2) {
                                                return Integer.compare(o2.getNumberOfLikes(), o1.getNumberOfLikes());
                                            }
                                        });
                                        //Adds the arraylist of posts into the recycler
                                        recyclerView = view.findViewById(R.id.trendingPosts);
                                        recyclerView.setHasFixedSize(true);
                                        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
                                        recyclerView.setAdapter(new PostAdapter(trendingPosts));
                                    }
                                });
                            });
                        }
                    });
                });
            }
        });
        return view;
    }
}
