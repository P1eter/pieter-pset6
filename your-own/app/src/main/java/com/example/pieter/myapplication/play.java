package com.example.pieter.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

        // set global parameters about the type of question (category, difficulty and type)
        setQuestionType();

        // Request a question from the api and set it in the listview
        getQuestion();
    }

    /**
     * This function parses and maps the data given by the spinners that the user set about
     * the preferred category, difficulty and type of the questions.
     */
    private void setQuestionType() {
        Intent intent = getIntent();

        // difficulty and type have can be mapped to the url-parameters directly
        difficulty = intent.getStringExtra("difficulty");
        type = intent.getStringExtra("type");

        // the string categories have to be mapped to an integer (as string) or the string "any"
        // when no category is specified.
        String category_str = intent.getStringExtra("category");
        if (category_str.equals("any")) {
            category = "any";
        } else {
            // starting value of all categories (the first one is 8, second one 9 etc.)
            int category_basevalue = 8;
            String[] categories = getResources().getStringArray(R.array.categories);

            // get index of category from string-array resources
            int category_value = getIndexOfElement(categories, category_str) + category_basevalue;

            if (category_value > category_basevalue) {
                category = Integer.toString(category_value);
            } else {
                // when something goes wrong, choose 'any' (should never happen)
                category = "any";
            }
        }
    }

    /**
     * Function to get the index of an element in an array.
     * @param array Array to search in.
     * @param element Element to look for in the array.
     * @return Index of the element in the array, -1 if it doesn't exist.
     */
    private int getIndexOfElement(String[] array, String element) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(element)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * OnClick function for 'next' button. Neccessary because getQuestion() can't accept a view.
     * @param view Button that was pressed to trigger this function.
     */
    public void goToNextQuestion(View view) {
        getQuestion();
    }

    /**
     * Request a question from the trivia api.
     */
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

    /**
     * Build the api url with the category, difficulty and type that the user selected.
     * @return Callable api url.
     */
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

    /**
     * Response listener class for the getQuestion function. Parses response and then sets the
     * adapter accordingly.
     */
    private class responseListener implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {
            tuple question = parseJson(response);
            setAdapter(question);
        }
    }

    /**
     * Error listener class for the getQuestion function. Sets the adapter with the "Error!" string
     * to signal the user something went wrong. Possibly the phone has no internet.
     */
    private class errorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            String error_string = "Error!";
            setAdapter(new tuple(error_string, new ArrayList<String>(), ""));
        }
    }

    /**
     * This function parses a JSON response that the api sends.
     * @param response JSON string with response from api.
     * @return Tuple with the question, the correct answer and the incorrect answers.
     */
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
            // set error text
            question = "Error!";
        }

        return new tuple(question, wrong_answers, correct_answer);
    }

    /**
     * This function sets the adapter for the listview of the question and answers.
     * @param question Tuple containing the question, wrong answers and the correct answer.
     */
    private void setAdapter(tuple question) {
        // extract all the answers and put them in one big list
        questions = question.wrong_answers;
        questions.add(question.correct_answer);

        // shuffle the answer options so the correct answer isn't always at the same location
        // in the list
        java.util.Collections.shuffle(questions);

        // create ArrayAdapter for the questions
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, questions);

        // get the answers listview and set adapter and onItemClickListener
        ListView answers_lv = findViewById(R.id.answers_lv);
        answers_lv.setAdapter(adapter);
        answers_lv.setOnItemClickListener(new answerQuestionClickListener());

        // set the question in the TextView
        TextView questiontv = this.findViewById(R.id.question_tv);
        questiontv.setText(question.question);

        // hide 'next' button so the question can't be skipped
        setNextButtonVisibility(false);

        // save current question, so it can be requested again when the app closes
        storeCurrentQuestion(question);
    }

    /**
     * This function saves the current question in shared preferences so it can be reinstantiated
     * once the app closes and opens again.
     * @param question
     */
    private void storeCurrentQuestion(tuple question) {
        SharedPreferences prefs = getSharedPreferences("question", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // remove the previous question from the sharedpreferences
        editor.clear();

        // add correct answer
        editor.putString("correct_answer", question.correct_answer);

        // add wrong answers
        for (int i = 0; i < question.wrong_answers.size(); i++) {
            editor.putString("wrong_answer_" + i, question.wrong_answers.get(i));
        }

        editor.commit();
    }

    /**
     * Click listener class for the answers listview. If the correct answer is selected, the
     * background of that answer is set to green. If not, then the background is set to red and
     * the background of the correct answer is set to green.
     */
    private class answerQuestionClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            SharedPreferences prefs = getSharedPreferences("question", play.MODE_PRIVATE);

            String correct_answer = prefs.getString("correct_answer", null);
            String selected_answer = adapterView.getItemAtPosition(i).toString();

            // check if answer is correct and act accordingly
            if (selected_answer.equals(correct_answer)) {
                view.setBackgroundColor(Color.GREEN);
                incrementScore();
            } else {
                view.setBackgroundColor(Color.RED);
                setColorCorrectAnswer(correct_answer);
            }

            // reveal next-button to go to the next question
            setNextButtonVisibility(true);
        }
    }

    /**
     * Add points to the score of the user.
     */
    private void incrementScore() {
        ValueEventListener getCurrentScoreListener = new incrementScoreListener();
        mDatabase.addListenerForSingleValueEvent(getCurrentScoreListener);
    }

    /**
     * Listener class for that gets a snapshot of the current user in the database and adds
     * one point to his/her score.
     */
    private class incrementScoreListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // score for a good answer
            int good_answer_score = 1;
            final String user = mAuth.getCurrentUser().getUid();

            // get current score
            int current_score = dataSnapshot.child("userscores").child(user).child("score").getValue(Integer.class);

            // store new score
            mDatabase.child("userscores").child(user).child("score").setValue(current_score + good_answer_score);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            System.out.println("Cancelled database request!");
        }
    }

    /**
     * This function sets the color of the view of the correct answer to green.
     * @param correct_answer String containing the correct answer.
     */
    private void setColorCorrectAnswer(String correct_answer) {
        ListView answers_lv = findViewById(R.id.answers_lv);

        // loop over all childs of the listview until the correct answer is found
        for (int child_index = 0; child_index < answers_lv.getChildCount(); child_index++) {
            if (answers_lv.getItemAtPosition(child_index).toString().equals(correct_answer)) {
                answers_lv.getChildAt(child_index).setBackgroundColor(Color.GREEN);
            }
        }
    }

    /**
     * Make the 'next' button either visible or invisible for the user.
     * @param visible Boolean, true equals make visible, false means make invisible.
     */
    private void setNextButtonVisibility(boolean visible) {
        // make the 'next button' visible, so the user has time to look at
        // what the correct answer was.
        Button next_button = findViewById(R.id.next_button);

        // set visibility
        if (visible) {
            next_button.setVisibility(View.VISIBLE);
        } else {
            next_button.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Class that can hold a question, wrong answers to that question and the correct answer.
     */
    private class tuple {
        private String question;
        private ArrayList<String> wrong_answers;
        private String correct_answer;

        public tuple(String question, ArrayList<String> wrong_answers, String correct_answer) {
            this.question = question;
            this.wrong_answers = wrong_answers;
            this.correct_answer = correct_answer;
        }
    }
}
