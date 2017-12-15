package com.example.pieter.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        setUserScoreListener();
    }

    private void setUserScoreListener() {
        ValueEventListener scoreListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String user = mAuth.getCurrentUser().getUid();
                int score = dataSnapshot.child("userscores").child(user).child("score").getValue(Integer.class);

                TextView score_tv = findViewById(R.id.score_tv);
                score_tv.setText("Your score: " + score);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting the data failed, log a message
                Log.w("score listener", "Something went wrong:", databaseError.toException());
            }
        };

        mDatabase.addValueEventListener(scoreListener);
    }

    public void logout(View view) {
        mAuth.signOut();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void play(View view) {
        Spinner category_spinner = findViewById(R.id.category_spinner);
        Spinner difficulty_spinner = findViewById(R.id.difficulty_spinner);
        Spinner type_spinner = findViewById(R.id.type_spinner);

        String category = category_spinner.getSelectedItem().toString();
        String difficulty = difficulty_spinner.getSelectedItem().toString();
        String type = type_spinner.getSelectedItem().toString();

        Intent intent = new Intent(this, play.class);
        intent.putExtra("category", category);
        intent.putExtra("difficulty", difficulty);
        intent.putExtra("type", type);
        startActivity(intent);
    }
}
