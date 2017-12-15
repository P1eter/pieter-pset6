package com.example.pieter.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class play extends AppCompatActivity {
    private ArrayList<String> questions;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String category;
    private String difficulty;
    private String type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        setQuestionType();

        // Request a question from the api and set
        getQuestion();
    }

    private void setQuestionType() {
        Intent intent = getIntent();

        difficulty = intent.getStringExtra("difficulty");
        type = intent.getStringExtra("type");

        String category_str = intent.getStringExtra("category");
        if (category_str.equals("any")) {
            category = "any";
        } else {
            int category_basevalue = 8;
            String[] categories = getResources().getStringArray(R.array.categories);
            int category_value = getIndexOfElement(categories, category_str) + category_basevalue;
            if (category_value > category_basevalue) {
                category = Integer.toString(category_value);
            } else {
                category = "any";
            }
        }
    }

    private int getIndexOfElement(String[] array, String element) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(element)) {
                return i;
            }
        }
        return -1;
    }

    private void getQuestion() {
        String url = getLink();
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new responseListener(),
                new errorListener()
        );

        queue.add(stringRequest);
    }

    private String getLink() {
        String url = "https://opentdb.com/api.php?amount=1";

        if (!category.equals("any")) {
            url = url + "&category=" + category;
        }
        if (!difficulty.equals("any")) {
            url = url + "&difficulty=" + difficulty;
        }
        if (!type.equals("any")) {
            url = url + "&type=" + type;
        }

        return url;
    }

    public void goToNextQuestion(View view) {
        getQuestion();
    }

    private class responseListener implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {
            tuple question = parseJson(response);
            setAdapter(question);
        }
    }

    private class errorListener implements Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            String error_string = "Error!";
            setAdapter(new tuple(error_string, new ArrayList<String>(), ""));
        }
    }

    private tuple parseJson(String response) {
        // initialize return object
        String question = "";
        ArrayList<String> wrong_answers = new ArrayList<>();
        String correct_answer = "";
        try {
            JSONObject theResponse = new JSONObject(response);
            int returnvalue = theResponse.getInt("response_code");
            switch (returnvalue) {
                case 0:
                    // request always contains only one element, at index 0 (due to unique TOKEN of the api)
                    JSONObject question_obj = theResponse.getJSONArray("results").getJSONObject(0);
                    question = question_obj.getString("question");
                    correct_answer = question_obj.getString("correct_answer");

                    JSONArray wrong_answers_obj = question_obj.getJSONArray("incorrect_answers");
                    for (int i = 0; i < wrong_answers_obj.length(); i++) {
                        wrong_answers.add(wrong_answers_obj.getString(i));
                    }
                default:
                    break;
            }
        } catch (JSONException e) {
            question = "Error!";
        }
        return new tuple(question, wrong_answers, correct_answer);
    }

    private void setAdapter(tuple question) {
        questions = question.wrong_answers;
        questions.add(question.correct_answer);
        java.util.Collections.shuffle(questions);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, questions);
        ListView answers_lv = findViewById(R.id.answers_lv);
        answers_lv.setAdapter(adapter);
        answers_lv.setOnItemClickListener(new answerQuestionClickListener());

        TextView questiontv = this.findViewById(R.id.question_tv);
        questiontv.setText(question.question);

        setNextButtonVisibility(false);

        storeCurrentQuestion(question);
    }

    private void storeCurrentQuestion(tuple question) {
        SharedPreferences prefs = getSharedPreferences("question", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();

        editor.putString("correct_answer", question.correct_answer);

        for (int i = 0; i < question.wrong_answers.size(); i++) {
            editor.putString("wrong_answer_" + i, question.wrong_answers.get(i));
        }
        editor.commit();
    }

    private class answerQuestionClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            SharedPreferences prefs = getSharedPreferences("question", play.MODE_PRIVATE);

            String correct_answer = prefs.getString("correct_answer", null);
            String selected_answer = adapterView.getItemAtPosition(i).toString();

            if (selected_answer.equals(correct_answer)) {
                view.setBackgroundColor(Color.GREEN);
                incrementScore();
            } else {
                view.setBackgroundColor(Color.RED);
                setColorCorrectAnswer(correct_answer);
            }

            setNextButtonVisibility(true);
        }
    }

    private void incrementScore() {
        final String user = mAuth.getCurrentUser().getUid();

        ValueEventListener getCurrentScoreListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int good_answer_score = 1;
                int current_score = dataSnapshot.child("userscores").child(user).child("score").getValue(Integer.class);
                mDatabase.child("userscores").child(user).child("score").setValue(current_score + good_answer_score);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.addListenerForSingleValueEvent(getCurrentScoreListener);
    }

    private void setColorCorrectAnswer(String correct_answer) {
        ListView answers_lv = findViewById(R.id.answers_lv);
        for (int child_index = 0; child_index < answers_lv.getChildCount(); child_index++) {
            if (answers_lv.getItemAtPosition(child_index).toString().equals(correct_answer)) {
                answers_lv.getChildAt(child_index).setBackgroundColor(Color.GREEN);
            }
        }
    }

    private void setNextButtonVisibility(boolean visible) {
        // Make the 'next button' visible, so the user has time to look at
        // what the correct answer was.
        Button next_button = findViewById(R.id.next_button);

        if (visible) {
            next_button.setVisibility(View.VISIBLE);
        } else {
            next_button.setVisibility(View.INVISIBLE);
        }
    }

    private class tuple {
        private String question;
        private ArrayList<String> wrong_answers;
        private String correct_answer;

        public tuple() {
            question = "";
            wrong_answers = new ArrayList<>();
            correct_answer = "";
        }

        public tuple(String question, ArrayList<String> wrong_answers, String correct_answer) {
            this.question = question;
            this.wrong_answers = wrong_answers;
            this.correct_answer = correct_answer;
        }
    }
}
