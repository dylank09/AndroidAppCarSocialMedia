package ie.ul.hotwheels;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;



import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UserFragment extends Fragment {
    private CircularImageView profileImage; //circular for ImageView plugin..users profile page image
    private StorageReference storageReference;
    private RecyclerView recyclerView;
    private String currentUserName;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        //uses the fragment user xml
        View view = inflater.inflate(R.layout.fragment_user,container,false);
        profileImage = view.findViewById(R.id.profileImage);
        storageReference = FirebaseStorage.getInstance().getReference();
        currentUserName = "";

        //gets userID string from database
        String userID = getArguments().getString("userID");

        //gets username string from database
        String userName = getArguments().getString("username");

        //gets the profile picture related to the user in the database
        StorageReference profileRef = storageReference.child("users/" + userID + "/profile.jpg");

        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Button followButton = view.findViewById(R.id.followButton);

        DocumentReference currentUserRef = fstore.collection("users")
                .document(currentUser.getUid());

        currentUserRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value,
                                @Nullable FirebaseFirestoreException error) {
               currentUserName = value.getString("username");
            }
        });

        //Loads all the profiles posts into a recycler view
        ArrayList<Post> posts = new ArrayList<>();
        fstore.collection("users").document(userID).collection("posts")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                queryDocumentSnapshots.getDocuments().forEach( post -> {
                    StorageReference postRef =  storageReference
                            .child("users/"+ userID
                                    +"/posts/"+ post.getString("postId"));
                    Post userPost = new Post(post.getString("model"),post.getString("brandName"),
                            post.getString("description"), postRef, LocalDateTime.now());
                    String dateTime = post.getString("dateAdded");
                    LocalDateTime dateAdded = LocalDateTime.parse(dateTime);
                    userPost.setDateAdded(dateAdded);
                    posts.add(userPost);
                });

                /*Calls the recycler to display each post on that the user has post on there page
                * GridLayout manager allows the recycler to appear as a grid rather than linear list layout
                * number of Columns specify the amount of items per column
                */
                recyclerView = view.findViewById(R.id.recyclerViewProfile);
                recyclerView.setHasFixedSize(true);
                int numberOfColumns = 2;
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),numberOfColumns));
                recyclerView.setAdapter(new ProfileAdapter(posts));
            }
        });

        //Code to Follow/Unfollow a user
        DocumentReference profileToFollow = fstore.collection("users")
                .document(currentUser.getUid()).collection("following")
                .document(userID);

        DocumentReference profileFollowing = fstore.collection("users")
                .document(userID).collection("followers")
                .document(currentUser.getUid());

        profileToFollow.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) {
                    followButton.setText("Follow");
                } else {
                    followButton.setText("UnFollow");
                }
            }


        });

        createNotificationChannel(); //call notification channel to display notification


        /*
        * Build the notification channel
        * Set up components/content of the notification
        * */
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "hello")
                .setSmallIcon(R.drawable.ic_baseline_add_alert_24)
                .setContentTitle("Follower")
                .setContentText("You have followed "+ userName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());

        //Set on click listener for follow button
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileToFollow.get().addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) { //if the follow does not exist in the database
                            Map<String, Object> following = new HashMap<>();
                            following.put("userID", userID); //put the userID into the database
                            following.put("username", userName); //put the user name into the database
                            profileToFollow.set(following); //change text to follow
                            notificationManager.notify(100,builder.build()); //send user the follow notification
                            followButton.setText("UnFollow");
                        } else { //if the follow does exist
                            profileToFollow.delete(); //remove the follower
                            followButton.setText("Follow"); //change text back to follow
                        }


                    }
                });

                /* loads into users followers list on profile page  */
                profileFollowing.get().addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            Map<String, Object> followers = new HashMap<>();
                            followers.put("userID", currentUser.getUid()); //puts the userID into the other users followers list
                            followers.put("username", currentUserName);
                            profileFollowing.set(followers);
                        } else {
                            profileFollowing.delete(); //removes the user form the followers list
                        }
                    }
                });
            }
        });


        //Loads the users profileImage into image holder
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        });

        //Loads the profiles username under the image
        DocumentReference profile = fstore.collection("users").document(userID);
        profile.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value,
                                @Nullable FirebaseFirestoreException error) {
                TextView userName = view.findViewById(R.id.userName);
                userName.setText(value.getString("username"));
            }
        });
        return view;
    }

    /*If the notification was loaded on android O it would not display.
    * It needs a notification channel
    * The following method allows the creation of the notification channel */
    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            CharSequence name = "Test";
            String description = " channel.............";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("hello",name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
