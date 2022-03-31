package ie.ul.hotwheels;


import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final ArrayList<User> users;
    public UserAdapter(ArrayList<User> users) {
        this.users = users;
    }


    /*when the view holder is created use layout user */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.users, parent, false);

        return new ViewHolder(v);
    }

    //on the binding of the user onto the recycler view
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);

        holder.username.setText(user.getUsername()); //set the text to the username

        /*Sets the profile image image along side the textview*/
        user.getProfileImage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(holder.profileImage);
            }
        });

        //when you click the item in the recyclerview it launches the user fragment
        View.OnClickListener vOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment myFragment = new UserFragment(); //new temp user fragment
                Bundle args = new Bundle(); //pass variables into user fragment
                args.putString("username", user.getUsername());
                args.putString("userID", user.getUserID());
                myFragment.setArguments(args);
                activity.getSupportFragmentManager().beginTransaction() //launch the user fragment
                        .replace(R.id.fragment_container, myFragment).addToBackStack(null).commit();


            }
        };

        holder.username.setOnClickListener(vOnClick); //set an onclick listener on the image
        holder.profileImage.setOnClickListener(vOnClick); //set an onlick listener on the text

    }

    @Override
    public int getItemCount() {
        if (users != null) {
            return users.size();
        } else {
            return 0;
        }
    }

    //Recycler view uses a ViewHolder to store references to the views for one entry in the recycler view
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView username;
        public final ImageView profileImage;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            username = view.findViewById(R.id.username);
            profileImage = view.findViewById(R.id.profile);
        }
    }

}


