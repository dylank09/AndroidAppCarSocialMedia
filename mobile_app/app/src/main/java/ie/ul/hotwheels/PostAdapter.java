package ie.ul.hotwheels;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private ArrayList<Post> posts;
    private String xCoordinate;
    private String yCoordinate;

    public PostAdapter(ArrayList<Post> posts) {
        this.posts = posts;
    } //arraylist of posts

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.car_posts,
                parent, false);
        
        return new ViewHolder(v);
    }

    //on the binding of the posts onto the recycler view
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Post post = posts.get(position);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        holder.description.setText(post.getDescription());

        holder.userID.setText(post.getUserName());
        holder.modelHolder.setText(post.getModel());
        holder.brandHolder.setText(post.getBrand());

        //gets users from database
        holder.fStore.collection("users")
                .document(post.getUserID()).collection("posts") //get user posts
                .document(post.getPostID()).collection("likes") //get posts asssociated with likes
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) { //when successful
                String numberOfLikes = String.valueOf(queryDocumentSnapshots.size()); //variable to hold the number of likes
                holder.imageLikes.setText(numberOfLikes); //set text to the total number of likes on that post
            }
        });

        DocumentReference documentReference = holder.fStore.collection("users") //does the same as above ^^
                .document(post.getUserID()).collection("posts")
                .document(post.getPostID()).collection("likes")
                .document(currentUser.getUid());
        documentReference.get().addOnSuccessListener(
                new OnSuccessListener<DocumentSnapshot>() {

                    //code for the like button on "initial load"
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        System.out.println(documentSnapshot);
                        if (documentSnapshot.exists()) {

                            holder.likeButton.setImageResource(R.drawable.ic_baseline_thumb_up_like); //the document exists(like) change to blue

                        }

                        else {

                            holder.likeButton.setImageResource(R.drawable.ic_baseline_thumb_up_24); //the document does not exist(like) change to grey
                        }
                    }
                });


        post.getImageURL().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(holder.image);
            } //add profile image
        });


        holder.fStore.collection("users")
                .document(post.getUserID()).collection("posts")
                .document(post.getPostID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override

            //code for getting the user coordinates on the post
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                xCoordinate =  documentSnapshot.getString("xCord"); //variable to hold xCoordinate
                yCoordinate =  documentSnapshot.getString("yCord"); //variable to hold yCoordinate

                //when you click on the gps icon the go to map holder
                holder.goToMapButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), MapActivity.class); //go to activity for map
                        Bundle bundle = new Bundle(); //carry in the variables
                        bundle.putString("lat", documentSnapshot.getString("xCord")); //put the xCoordinate into bundle
                        bundle.putString("long", documentSnapshot.getString("yCord"));//put the yCoordinate into bundle
                        intent.putExtras(bundle);
                        v.getContext().startActivity(intent); //start the activity
                    }
                });
            }
        });



        //when the image is clicked on the post
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                Fragment myFragment = new postsFragment(); //launch a new fragment
                Bundle args = new Bundle(); //bundle items tio carry over
                String uri = post.getImageURL().toString();
                args.putString("ImageUri", uri); //bundle the image uri
                args.putString("CarBrand", post.getBrand()); //bundle the carbrand
                args.putString("CarDescription", post.getDescription()); //bundle the car description

                myFragment.setArguments(args);
                activity.getSupportFragmentManager().beginTransaction() //launch the fragment container
                        .replace(R.id.fragment_container, myFragment).addToBackStack(null).commit();
            }
        });

        //when you select the username on the post it launched to the user profile
        holder.userID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                Fragment myFragment = new UserFragment();
                Bundle args = new Bundle();
                args.putString("username", post.getUserName());
                args.putString("userID", post.getUserID());
                myFragment.setArguments(args);
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, myFragment).addToBackStack(null).commit();
            }
        });


        //when you click the like button
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                documentReference.get().addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (!documentSnapshot.exists()) { //if the document exists add one to the like counter (like to blue)
                                    Map<String, Object> likes = new HashMap<>();
                                    documentReference.set(likes);
                                    int i = Integer.parseInt((String) holder.imageLikes.getText());
                                    holder.imageLikes.setText(String.valueOf(i + 1));
                                    holder.likeButton.setImageResource(R.drawable.ic_baseline_thumb_up_like);
                                }
                                else {
                                    documentReference.delete(); //else delete the like from the database
                                    int i = Integer.parseInt((String) holder.imageLikes.getText());
                                    holder.imageLikes.setText(String.valueOf(i - 1)); //take away one form the like counter
                                    holder.likeButton.setImageResource(R.drawable.ic_baseline_thumb_up_24); //change to grey
                                }
                            }
                        });
            }
        });


    }

    @Override
    public int getItemCount() {
        if (posts != null) {
            return posts.size();
        } else {
            return 0;
        }
    }

    //Recycler view uses a ViewHolder to store references to the views for one entry in the recycler view
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView description;
        public final ImageView image;
        public final TextView userID;
        private ImageView likeButton;
        private TextView imageLikes;
        private TextView modelHolder;
        private TextView brandHolder;
        private ImageView goToMapButton;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();

        public ViewHolder(View view) {

            super(view);
            this.view = view;
            description = view.findViewById(R.id.descriptionTextView);
            image = view.findViewById(R.id.image);
            userID = view.findViewById(R.id.UserNameHome);
            likeButton = view.findViewById(R.id.likeButtonFeed);
            imageLikes = view.findViewById(R.id.counter);
            modelHolder = view.findViewById(R.id.carModelTextView);
            brandHolder = view.findViewById(R.id.carBrandTextView);
            goToMapButton = view.findViewById(R.id.goToMapButton);

        }
    }
}


