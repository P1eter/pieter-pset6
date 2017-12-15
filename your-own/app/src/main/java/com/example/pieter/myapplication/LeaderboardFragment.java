/**
 * Pieter Kronemeijer
 * 11064838
 *
 * This fragment shows the leaderboard on top of the home screen.
 */

package com.example.pieter.myapplication;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * A simple {@link Fragment} subclass.
 */
public class LeaderboardFragment extends DialogFragment {
    private DatabaseReference mDatabase;
    private leaderboardAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // initialize database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // add listener to set the leaderboard-listview of this fragment
        ValueEventListener getLeaderboard = new leaderboardListener();
        mDatabase.addListenerForSingleValueEvent(getLeaderboard);
    }

    /**
     * Listener class for getting the leaderboard data from the database.
     */
    private class leaderboardListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // resulting list of name-score combinations of users
            ArrayList<leaderboardTuple> leaderboard = new ArrayList<>();

            // get score and name from database for every user, and store is as tuple in 'leaderboard'
            for (DataSnapshot userdata : dataSnapshot.child("userscores").getChildren()) {
                int score = userdata.child("score").getValue(Integer.class);
                String name = userdata.child("name").getValue(String.class);
                leaderboard.add(new leaderboardTuple(name, score));
            }

            // call function that sets the adapter for the listview
            setLeaderboardAdapter(leaderboard);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting the data failed, log a message
            Log.w("retrieving leaderboard", "Something went wrong:", databaseError.toException());
        }
    }

    /**
     * This function sets the leaderboard adapter with the data in the argument 'leaderboard'.
     * @param leaderboard List of tuples with name-score combinations of all users.
     */
    private void setLeaderboardAdapter(ArrayList<leaderboardTuple> leaderboard) {
        adapter = new leaderboardAdapter(leaderboard);
        ListView leaderboard_lv = getView().findViewById(R.id.leaderboard_lv);
        leaderboard_lv.setAdapter(adapter);
    }

    /**
     * Custom adapter for the leaderboard.
     */
    private class leaderboardAdapter extends BaseAdapter {
        private ArrayList<String> names = new ArrayList<>();
        private ArrayList<Integer> scores = new ArrayList<>();

        /**
         * Constructor, that extracts a list of names and scores from the input tuples, after
         * sorting it.
         * @param leaderboard Unsorted user-score pairs.
         */
        public leaderboardAdapter(ArrayList<leaderboardTuple> leaderboard) {
            // sort leaderboard entries based on the score in the tuple
            leaderboard = mySort(leaderboard);

            // extract names and scores
            for (leaderboardTuple user : leaderboard) {
                names.add(user.user);
                scores.add(user.score);
            }
        }

        /**
         * Sorting function for a list of tuples with names and scores of users. Sorts based
         * on the score for each user, descending.
         * @param leaderboard List of user-score tuples.
         * @return Sorted list of user-score tuples.
         */
        private ArrayList<leaderboardTuple> mySort(ArrayList<leaderboardTuple> leaderboard) {
            Collections.sort(leaderboard, new Comparator<leaderboardTuple>() {
                @Override
                public int compare(leaderboardTuple t1, leaderboardTuple t2) {
                    return t2.score - t1.score;
                }
            });

            return leaderboard;
        }

        @Override
        public int getCount() {
            return names.size();
        }

        @Override
        public Object getItem(int i) {
            return new leaderboardTuple(names.get(i), scores.get(i));
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // if rowView is null, inflate new layout, else use recycled 'convertView'
            View rowView = convertView;
            if (rowView == null) {
                rowView = inflater.inflate(R.layout.leaderboard_row, parent, false);
            }

            TextView user_tv = rowView.findViewById(R.id.lb_name_tv);
            TextView score_tv = rowView.findViewById(R.id.lb_score_tv);

            // set names and scores in the custom rows for the listview
            user_tv.setText(names.get(position));
            score_tv.setText(Integer.toString(scores.get(position)));

            return rowView;
        }
    }

    /**
     * Tuple class for storing a combination of a user and a score.
     */
    private class leaderboardTuple {
        String user;
        int score;

        public leaderboardTuple(String user, int score) {
            this.user = user;
            this.score = score;
        }
    }
}
