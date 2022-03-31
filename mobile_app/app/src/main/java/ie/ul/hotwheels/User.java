package ie.ul.hotwheels;

import com.google.firebase.storage.StorageReference;


public class User {
    private String username;
    private String userID;
    private StorageReference profileImage;

    /*
    Constructors for the user class
     */

    public User(String username, String userID, StorageReference prof) {
        this.userID = userID;
        this.username = username;
        this.profileImage = prof;
    }

    public User(String username, String userID) {
        this.userID = userID;
        this.username = username;
    }

    //gets username
    public String getUsername() {
        return username;
    }

    //sets username
    public void setUsername(String name) {
        this.username = name;
    }

    //gets userID
    public String getUserID() {
        return userID;
    }

    //sets userID
    public void setUserID(String userID) {
        this.userID = userID;
    }

    //get ProfileImage
    public StorageReference getProfileImage() {
        return profileImage;
    }

    //set ProfileImage
    public void setProfileImage(StorageReference profileImage) {
        this.profileImage = profileImage;
    }

    //prints the user items to string
    @Override
    public String toString() {
        return "username = " + username + " userID = " + userID + " store ref ="+ profileImage;
    }
}
