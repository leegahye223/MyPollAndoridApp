package com.uclacsm117.mysimlpepoll;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class VoteActivity extends AppCompatActivity {

    private ArrayAdapter<String> optionArrayAdapter;

    public static final String SELECTION = "selection";

    public  static final String QUESTION_OPTION_TEXT = "question_option_text";

    private String q_str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();

        String t = b.getString(QUESTION_OPTION_TEXT);

        String [] q_array = t.split("\007");
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.content_vote);
        setResult(Activity.RESULT_CANCELED);
        optionArrayAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice);
        LinearLayout vote_layout = (LinearLayout) findViewById(R.id.vote_activity_layout);
        EditText question_text = new EditText(this);
        question_text.setKeyListener(null);
        question_text.setText(q_array[1]);
        vote_layout.addView(question_text);

        ListView option_list = (ListView) findViewById(R.id.option_list);
        option_list.setAdapter(optionArrayAdapter);
        option_list.setOnItemClickListener(optionListListener);





//        TextView t_view = (TextView) findViewById(R.id.option_question);
//        t_view.setText( q_array[1]);

        for(int i = 2;i<q_array.length;i++){
            optionArrayAdapter.add(q_array[i]);
        }



    }


    private AdapterView.OnItemClickListener optionListListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect


            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();


            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(SELECTION, info);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
}
