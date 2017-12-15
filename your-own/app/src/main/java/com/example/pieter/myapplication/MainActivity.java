package com.example.pieter.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    int MIN_PASSWORD_LENGTH = 6;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * This function is called when a user wants to log in.
     * @param view The button that was clicked to trigger this function.
     */
    public void signIn(View view) {
        // get combination of email and password from the edit-texts
        tuple emailAndPassword = getEmailPassword();

        // if 'emailAndPassword' equals null, the given email and password to not comply
        // with the rules (not a real email or password is shorter than 6 characters
        if (emailAndPassword == null) {
            displayError("signIn");
        } else {
            final String email = emailAndPassword.email;
            final String password = emailAndPassword.password;

            //
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new logInListener());
        }
    }

    /**
     * Listener class that waits for the database to log the user in, and then updates the UI.
     */
    private class logInListener implements OnCompleteListener<AuthResult> {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            String TAG = "log in";

            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithEmail:success");
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithEmail:failure", task.getException());
                Toast.makeText(MainActivity.this, "Authentication failed.",
                Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        }
    }

    /**
     * This function is called when a user wants to sign up for an account.
     * @param view The button that was clicked to trigger this function.
     */
    public void createAccount(View view) {
        tuple emailAndPassword = getEmailPassword();

        if (emailAndPassword == null) {
            displayError("passwordlength");
        } else {
            final String email = emailAndPassword.email;
            final String password = emailAndPassword.password;

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new signUpListener());
        }
    }

    /**
     * Listener class that waits for the database to sign up a new user, and then updates the UI.
     */
    private class signUpListener implements OnCompleteListener<AuthResult> {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            String TAG = "sign_up";
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "createUserWithEmail:success");
                FirebaseUser user = mAuth.getCurrentUser();
                initUser();
                updateUI(user);
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                Toast.makeText(MainActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        }
    }

    /**
     * Initializes 'score' and 'name' fields in the database for a new user.
     */
    private void initUser() {
        // get user id, user email and database reference
        String user = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        // initialize name and score in the database
        db.child("userscores").child(user).child("score").setValue(0);
        db.child("userscores").child(user).child("name").setValue(getNameFromEmail(email));
    }

    /**
     * This function takes an email-address as input and returns the part before the '@'.
     * @param email The email-address of the user.
     * @return The part of the email-address before the '@'.
     */
    private String getNameFromEmail(String email) {
        String name = "";

        for (int i = 0; i < email.length(); i++) {
            char letter = email.charAt(i);
            if (letter != '@') {
                name = name + email.charAt(i);
            } else {
                return name;
            }
        }

        // default name
        return "Anonymous";
    }

    /**
     * Gets the email and password from the EditTexts. Checks if the password is long
     * enough (six characters).
     * @return Tuple with the name and password from the input fields if the password is long
     * enough, else returns 'null'.
     */
    private tuple getEmailPassword() {
        EditText email_et = findViewById(R.id.email_login_et);
        EditText password_et = findViewById(R.id.password_login_et);

        String email = email_et.getText().toString();
        String password = password_et.getText().toString();

        // check if password is long enough
        if (password.length() >= MIN_PASSWORD_LENGTH) {
            return new tuple(email, password);
        } else {
            return null;
        }
    }

    /**
     * Updates the UI based on validity of the given 'currentUser'.
     * @param currentUser The current user, null on error
     */
    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            start_game();
        } else {
            displayError("signIn");
        }
    }

    /**
     * Start a game of trivia in a new activity.
     */
    public void start_game() {
        Intent intent = new Intent(this, home.class);
        startActivity(intent);
    }

    /**
     * Function to display an error in a specified TextField on the home screen.
     * @param error String with the type of error.
     */
    private void displayError(String error) {
        TextView error_tv = findViewById(R.id.error_msg_tv);
        error_tv.setTextColor(Color.RED);

        // set errortext based on given error
        switch (error) {
            case "passwordlength":
                error_tv.setText(R.string.password_error);
                break;
            case "wrongpassword":
                error_tv.setText(R.string.invalid_password_error);
                break;
            case "signIn":
                error_tv.setText(R.string.general_error);
                break;
            default:
                error_tv.setText(R.string.uninformed_error);
                break;
        }

        // make the TextView visible
        error_tv.setVisibility(View.VISIBLE);
    }

    /**
     * Class that can hold an email-password combination of a user.
     */
    private class tuple {
        private String email;
        private String password;

        public tuple(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}
