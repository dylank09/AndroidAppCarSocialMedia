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

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmailText;
    private EditText loginPassText;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        //Gets all of the layout parameters
        Button loginBtn = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);
        loginPassText = findViewById(R.id.loginPassword);
        loginEmailText = findViewById(R.id.loginName);
        progressBar = findViewById(R.id.progressBar);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Gets the login email and password that were entered
                String loginEmail = loginEmailText.getText().toString();
                String loginPass = loginPassText.getText().toString();
                //Checks if email and pass are empty
                if(TextUtils.isEmpty(loginEmail)){
                    Toast.makeText(LoginActivity.this, "Username field is empty"
                            , Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(loginPass)){
                    Toast.makeText(LoginActivity.this, "Password field is empty"
                            , Toast.LENGTH_SHORT).show();
                }

                if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPass)){
                    //Shows the progress bar
                    progressBar.setVisibility(View.VISIBLE);
                    //Gets the user from FireBase Authentication
                    mAuth.signInWithEmailAndPassword(loginEmail,loginPass)
                            .addOnCompleteListener((task) -> {
                        //If successfully finds user
                        if(task.isSuccessful()) {
                            //If you login successfully brings you to MainActivity
                            Intent intent = new Intent(LoginActivity.this,
                                    MainActivity.class);
                            startActivity(intent);
                        //If the user is not found
                        } else {
                            //Tells the user why it did not login
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(LoginActivity.this, "Error : "
                                    + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                        //Hides progress bar if successful/unsuccessful
                        progressBar.setVisibility(View.INVISIBLE);
                    });
                }
            }
        });

        //If you click register button it brings you the register screen
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,
                        RegisterActivity.class);
                startActivity(intent);
            }
        });

    }
}