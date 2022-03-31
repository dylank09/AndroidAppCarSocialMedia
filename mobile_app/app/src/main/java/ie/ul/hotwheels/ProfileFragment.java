package ie.ul.hotwheels;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

public class ProfileFragment extends Fragment {
    private CircularImageView profileImage; //circular for ImageView plugin
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        View view = inflater.inflate(R.layout.fragment_profile,container,false);
        profileImage = view.findViewById(R.id.profileImage);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        StorageReference profileRef = storageReference.child("users/" +
                firebaseAuth.getCurrentUser().getUid() + "/profile.jpg");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();

        Button viewFollowing = view.findViewById(R.id.viewFollowingButton);
        Button viewFollowers = view.findViewById(R.id.viewFollowersButton);
        Button logOut = view.findViewById(R.id.logoutButton);

        //Lists all the posts you have posted on your profile
        ArrayList<Post> posts = new ArrayList<>();
        fstore.collection("users").document(currentUser.getUid()).collection("posts")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                queryDocumentSnapshots.getDocuments().forEach( post -> {
                    StorageReference postRef =  storageReference
                            .child("users/"+ currentUser.getUid()
                                    +"/posts/"+ post.getString("postId"));
                    Post userPost = new Post(post.getString("model"),post.getString("brand"),
                            post.getString("description"), postRef, LocalDateTime.now());
                    String dateTime = post.getString("dateAdded");
                    LocalDateTime dateAdded = LocalDateTime.parse(dateTime);
                    userPost.setDateAdded(dateAdded);
                    posts.add(userPost);
                });
                Collections.sort(posts);

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

        //Loads your profiles image into image holder
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        });

        //Brings you to the people you follow, a list
        viewFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment myFragment = new FollowingFragment();
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, myFragment).addToBackStack(null).commit();
            }
        });

        //Logs user out and brings you to the login page
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LoginActivity.class); //go to activity for login
                v.getContext().startActivity(intent); //start the activity
                mAuth.signOut(); //logs user out of firebase
            }
        });

        //Brings you to the people that follow you, returns a list.
        viewFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment myFragment = new FollowersFragment();
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, myFragment).addToBackStack(null).commit();
            }
        });

        //Loads your profiles name into text holder
        DocumentReference profile = fstore.collection("users")
                .document(currentUser.getUid());

        profile.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value,
                                @Nullable FirebaseFirestoreException error) {
                TextView userName = view.findViewById(R.id.userName);
                userName.setText(value.getString("username"));
            }
        });

        //Clicking on profile image will offer you to change the profile pic from gallery
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)  {
                Intent i;
                i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                startActivityForResult(i, 1000);
            }
        });
        return view;
    }

    //Creates an image in firestore and changes photo
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000) {
            if(resultCode == Activity.RESULT_OK) {
                Uri imageUri = data.getData();
                uploadImageToFirebase(imageUri);
            }
        }
    }

    //Uploads the image to the users profile in fire storage
    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference fileRef;
        fileRef = storageReference.child("users/"
                + firebaseAuth.getCurrentUser().getUid() + "/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri){
                         Toast.makeText(getContext(), "Image Uploaded.",
                                 Toast.LENGTH_SHORT).show();
                         Picasso.get().load(uri).into(profileImage);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Image upload failure try again",
                        Toast.LENGTH_SHORT).show();
            }
       });
    }

}
