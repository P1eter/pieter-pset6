package com.example.pieter.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class home extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // initialize firebase authorization instance and the database itself
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // this keeps the 'score' text field in the app updated
        setUserScoreListener();
    }

    /**
     * This function adds a listener to the 'score' field in the database, which updates the
     * TextView once the score has changed.
     */
    private void setUserScoreListener() {
        ValueEventListener scoreListener = new userScoreListener();
        mDatabase.addValueEventListener(scoreListener);
    }

    /**
     * Listener class that keeps track of changes in the 'score' of the user.
     */
    private class userScoreListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // get the score data from the database
            String user = mAuth.getCurrentUser().getUid();
            int score = dataSnapshot.child("userscores").child(user).child("score").getValue(Integer.class);

            // set the score in the TextView
            TextView score_tv = findViewById(R.id.score_tv);
            score_tv.setText("Your score: " + score);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting the data failed, log a message
            Log.w("score listener", "Something went wrong:", databaseError.toException());
        }
    };

    public void logout(View view) {
        mAuth.signOut();

        // return to the login/signup screen once singed out
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Go to the actual game activity.
     * @param view The button that was clicked to trigger this function.
     */
    public void play(View view) {
        // get the views of the category, difficulty and type spinners
        Spinner category_spinner = findViewById(R.id.category_spinner);
        Spinner difficulty_spinner = findViewById(R.id.difficulty_spinner);
        Spinner type_spinner = findViewById(R.id.type_spinner);

        // get the value from all three spinners
        String category = category_spinner.getSelectedItem().toString();
        String difficulty = difficulty_spinner.getSelectedItem().toString();
        String type = type_spinner.getSelectedItem().toString();

        // create intent with the data to call the api correctly
        Intent intent = new Intent(this, play.class);
        intent.putExtra("category", category);
        intent.putExtra("difficulty", difficulty);
        intent.putExtra("type", type);

        // go to the 'play' activity
        startActivity(intent);
    }

    /**
     * Function that shows the leaderboard dialog fragment.
     * @param view The button that was clicked to trigger this function.
     */
    public void showLeaderboard(View view) {
        // show the dialog fragment with the leaderboard
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        LeaderboardFragment fragment = new LeaderboardFragment();
        fragment.show(ft, "dialog");
    }
}
