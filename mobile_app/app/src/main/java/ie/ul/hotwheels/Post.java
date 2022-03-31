package ie.ul.hotwheels;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.storage.StorageReference;

import java.time.LocalDateTime;


//Creates a Post Object
public class Post implements Comparable<Post> {
    private String brand;
    private String description;
    private StorageReference imageURL;
    private LocalDateTime dateAdded;
    private String userID;
    private String userName;
    private String PostID;
    private String Model;
    private int numberOfLikes;


    public Post(String Model, String PostID, String userName, String userID, String brand, String description, StorageReference imageURL, LocalDateTime dateAdded) {
        this.brand = brand;
        this.description = description;
        this.imageURL = imageURL;
        this.dateAdded = dateAdded;
        this.userID = userID;
        this.userName = userName;
        this.PostID = PostID;
        this.Model = Model;
    }

    public Post(int numberOfLikes, String Model, String PostID, String userName, String userID, String brand, String description, StorageReference imageURL) {
        this.Model = Model;
        this.numberOfLikes = numberOfLikes;
        this.brand = brand;
        this.description = description;
        this.imageURL = imageURL;
        this.userID = userID;
        this.userName = userName;
        this.PostID = PostID;
    }

    public Post(String Model, String brand, String description, StorageReference imageURL, LocalDateTime dateAdded) {
        this.brand = brand;
        this.description = description;
        this.imageURL = imageURL;
        this.dateAdded = dateAdded;
        this.Model = Model;
    }



    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void SetUser(String userName){this.userName = userName;}

    public String getUserName(){
        return userName;
    }

    public void SetPostID(String PostID){this.PostID = PostID;}

    public String getPostID(){
        return PostID;
    }

    public void setID(String userID){this.userID = userID;}

    public String getUserID(){
        return userID;
    }

    public void setModel(String userID){this.Model = Model;}

    public String getModel(){
        return Model;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StorageReference getImageURL() {
        return imageURL;
    }

    public void setImageURL(StorageReference imageURL) {
        this.imageURL = imageURL;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }

    public int getNumberOfLikes() {
        return numberOfLikes;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int compareTo(Post o) {
        return o.getDateAdded().compareTo(getDateAdded());
    }
}
