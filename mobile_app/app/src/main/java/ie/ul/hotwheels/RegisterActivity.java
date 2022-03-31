package ie.ul.hotwheels;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {

    private EditText newAccountEmail;
    private EditText newAccountPassword;
    private EditText newAccountPassword2;
    private EditText newUserName;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private ProgressBar progressBar;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Gets all of the layout parameters
        newAccountEmail = findViewById(R.id.newUserEmail);
        newAccountPassword = findViewById(R.id.newPassword);
        newAccountPassword2 = findViewById(R.id.newPassword2);
        newUserName = findViewById(R.id.newUserName);
        newUserName = findViewById(R.id.newUserName);
        Button registerBtn = findViewById(R.id.createAccount);
        Button goToLoginPage = findViewById(R.id.goToLoginButton);

        //Initialize progress bar and FireBase
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        goToLoginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Goes to login activity if login button pressed
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Gets all user entered details
                String email = newAccountEmail.getText().toString();
                String password = newAccountPassword.getText().toString();
                String password2 = newAccountPassword2.getText().toString();
                String userName = newUserName.getText().toString();

                //Checks all values are entered
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(RegisterActivity.this,
                            "Email field is empty", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(userName)){
                    Toast.makeText(RegisterActivity.this,
                            "Username field is empty", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterActivity.this,
                            "Password field is empty", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(password2)){
                    Toast.makeText(RegisterActivity.this,
                            "Confirm password field is empty", Toast.LENGTH_SHORT).show();
                }


                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)
                        && !TextUtils.isEmpty(password2) && !TextUtils.isEmpty(userName)){
                    //If the confirm passwords match
                    if(password.equals(password2)) {
                        //Makes the progressBar visible on button press
                        progressBar.setVisibility(View.VISIBLE);
                        //Creates a user in FireBase Authentication
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener((task) -> {
                            //If it creates a user successfully
                            if(task.isSuccessful()) {
                                //Gets the userID of the recently created user
                                userId = mAuth.getCurrentUser().getUid();
                                //We want to pass the username to users in the Firestore
                                DocumentReference docRef;
                                docRef = fStore.collection("users").document(userId);
                                Map<String, Object> users = new HashMap<>();
                                users.put("username", userName);
                                docRef.set(users);

                                //Sends you to MainActivity on Login
                                Intent i;
                                i = new Intent(RegisterActivity.this, MainActivity.class);
                                startActivity(i);
                            } else {
                                //If unsuccessful it alerts the user as to why
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error : "
                                        + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        });

                    }
                    // If the passwords don't match alerts the user if everything else if correct
                    else {
                        Toast.makeText(RegisterActivity.this,
                                "Confirm Passwords Match", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
}