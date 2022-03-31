package ie.ul.hotwheels;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class postsFragment extends Fragment {
    private ImageView postImage;
    private ImageView likeButton;
    private TextView modeledit;
    private TextView yearEdit;
    private TextView engineEdit;
    private TextView LocationEdit;

    private TextView imageLikes;
    private StorageReference storageReference;
    private String userPostName;
    private String usersPostId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        postImage = view.findViewById(R.id.postPicture);
        likeButton = view.findViewById(R.id.likeButton);
        TextView carBrandedit = view.findViewById(R.id.carNameEdit);
        imageLikes = view.findViewById(R.id.imageLikes);
        TextView carDescriptionedit = view.findViewById(R.id.carDescEdit);
        modeledit = view.findViewById(R.id.modelDescrEdit);
        yearEdit = view.findViewById(R.id.yearDescrEdit);
        engineEdit = view.findViewById(R.id.carEngineDesrcEdit);

        storageReference = FirebaseStorage.getInstance().getReference();
        TextView username = view.findViewById(R.id.userNameForPost);
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String imgPath = getArguments().getString("ImageUri");
        String[] splitString = imgPath.split("/");
        imgPath = "";
        usersPostId = "";
        userPostName = "";

        //Gets the fourth instance of the fifth of the forward slash
        for (int x = 3 ; x < splitString.length; x++) {
            if(x == 4) usersPostId = splitString[x];
            imgPath += splitString[x] + "/";
        }

        StorageReference postRef = storageReference.child(imgPath);
        postRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(postImage);
            } //load the image for the post
        });
        String CarBrand = getArguments().getString("CarBrand"); //gets the car brand from db and puts it in string
        String CarDescription = getArguments().getString("CarDescription");

        carBrandedit.setText(CarBrand); //set the carbrand to the textview
        carDescriptionedit.setText(CarDescription); //set car description to the textview

        //gets the user name from the db
        DocumentReference userNameReference = fStore.collection("users")
                .document(usersPostId);
        userNameReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value,
                                @Nullable FirebaseFirestoreException error) {
                userPostName = value.getString("username");
                username.setText(userPostName);  //sets the username to the textview
            }
        });

        fStore.collection("users").document(usersPostId).collection("posts")
                .document(splitString[splitString.length-1]).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                yearEdit.setText(value.getString("year")); //set the text view to have the year from database
                modeledit.setText(value.getString("model")); //set the text view to have the model
                engineEdit.setText(value.getString("engine")); //set the text view to have the engine

            }
        });

        /*when the username is clicked launch the user fragment */
        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment myFragment = new UserFragment();
                Bundle args = new Bundle();
                args.putString("username", userPostName);
                args.putString("userID", usersPostId);
                myFragment.setArguments(args);
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, myFragment).addToBackStack(null).commit();
            }
        });

        DocumentReference documentReference = fStore.collection("users")
                .document(usersPostId).collection("posts")
                .document(splitString[splitString.length - 1]).collection("likes")
                .document(currentUser.getUid());

        /*if the document exists change to blue...if not change to grey on load */
        documentReference.get().addOnSuccessListener(
                new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            likeButton.setImageResource(R.drawable.ic_baseline_thumb_up_like);
                        } else {
                            likeButton.setImageResource(R.drawable.ic_baseline_thumb_up_24);
                        }
                    }
                });

        /*if the document exists change to blue...if not change to grey when clicked */
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                documentReference.get().addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            Map<String, Object> likes = new HashMap<>();
                            documentReference.set(likes);
                            int i = Integer.parseInt((String) imageLikes.getText());
                            imageLikes.setText(String.valueOf(i + 1));
                            likeButton.setImageResource(R.drawable.ic_baseline_thumb_up_like);
                        } else {
                            documentReference.delete();
                            int i = Integer.parseInt((String) imageLikes.getText());
                            imageLikes.setText(String.valueOf(i - 1));
                            likeButton.setImageResource(R.drawable.ic_baseline_thumb_up_24);
                        }
                    }
                });

            }
        });

        Task<QuerySnapshot> documentReference2 = fStore.collection("users")
                .document(usersPostId).collection("posts")
                .document(splitString[splitString.length - 1]).collection("likes")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                String temp = String.valueOf(queryDocumentSnapshots.size());
                imageLikes.setText(temp);
            }
        });


        return view;
    }
}
