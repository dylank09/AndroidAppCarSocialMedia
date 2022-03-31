package ie.ul.hotwheels;


import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private final ArrayList<Post> posts;

    public ProfileAdapter(ArrayList<Post> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.profile_posts, parent, false);

        return new ViewHolder(v);
    }

    //on the binding of the user onto the recycler view
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);

        post.getImageURL().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(holder.image);
            }
        });

        //when the image is clicked launch a new fragment
        /*
        Same steps as other fragments
        create a new fragment
        bundle the data to carry over
        launch the new fragment
         */
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                Fragment myFragment = new postsFragment();
                Bundle args = new Bundle();
                String uri = post.getImageURL().toString();
                args.putString("ImageUri", uri);
                args.putString("CarBrand", post.getBrand());
                args.putString("CarDescription", post.getDescription());

                myFragment.setArguments(args);
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, myFragment).addToBackStack(null).commit();
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
        public final ImageView image;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            image = view.findViewById(R.id.image);
        }
    }
}


