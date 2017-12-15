package com.example.pieter.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
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
                    });
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
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
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
                    });
        }

    }

    private void initUser() {
        String user = mAuth.getCurrentUser().getUid();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.child("userscores").child(user).child("score").setValue(0);
    }

    private tuple getEmailPassword() {
        EditText email_et = findViewById(R.id.email_login_et);
        EditText password_et = findViewById(R.id.password_login_et);

        String email = email_et.getText().toString();
        String password = password_et.getText().toString();

        if (password.length() >= 6) {
            return new tuple(email, password);
        } else {
            return null;
        }
    }

    private class tuple {
        private String email;
        private String password;

        public tuple(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            start_game();
        } else {
            displayError("signIn");
        }
    }

    public void start_game() {
        Intent intent = new Intent(this, home.class);
        startActivity(intent);
    }

    private void displayError(String error) {
        TextView error_tv = findViewById(R.id.error_msg_tv);
        error_tv.setTextColor(Color.RED);

        System.out.println("THE-ERROR:" + error);

        switch (error) {
            case "passwordlength":
                error_tv.setText("Password must be at least 6 characters!");
                break;
            case "wrongpassword":
                error_tv.setText("Incorrect password!");
                break;
            case "signIn":
                error_tv.setText("Error while trying to sign you in!");
                break;
            default:
                error_tv.setText("Error!");
                break;
        }
        error_tv.setVisibility(View.VISIBLE);
    }
}
