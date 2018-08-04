package com.uclacsm117.mysimlpepoll;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void CreateNewPoll(View view) {
        Intent create_poll_intent = new Intent(this,CreatePollActivity.class);
        startActivity(create_poll_intent);
    }

    public void ConnectVote(View view){
        Intent connect_to_vote = new Intent(this,ConnectToVote.class);
        startActivity(connect_to_vote);
    }
}
