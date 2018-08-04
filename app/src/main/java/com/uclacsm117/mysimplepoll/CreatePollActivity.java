package com.uclacsm117.mysimlpepoll;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CreatePollActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private static final boolean D = true;

    private static final int REQUEST_CONNECT_DEVICE = 1;

    private static final String TAG = "Create Poll";

    private String q_text;

    private String poll_str;

    private BluetoothAdapter mBluetoothAdapter;

    private static final int REQUEST_ENABLE_BT = 4;

    private class MapNode{
        public MapNode(LinearLayout l, EditText t){
            this.container = l;
            this.text = t;
        }
        LinearLayout container;
        EditText text;
    }

    private ArrayList<BluetoothDevice> m_device_list = new ArrayList<BluetoothDevice>();

    private GoogleApiClient client;

    private HashMap<Button,MapNode> button_container_map = new HashMap<Button,MapNode>();

    private ArrayList<String> selected_device_mac_addresses;

    //private BluetoothService m_bt_service = null;

    private ArrayList<BluetoothService> m_bt_services = null;

    private ConcurrentHashMap<String,Integer> count_map = new ConcurrentHashMap<String, Integer>();

    boolean poll_started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poll);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    public void AddPollOption(View view) {
        Button delete_btn = new Button(this);
        EditText new_option = new EditText(this);
        new_option.setHint("new option");

        LinearLayout new_option_container = new LinearLayout(this);
        new_option_container.setOrientation(LinearLayout.HORIZONTAL);
        new_option_container.setHorizontalGravity(Gravity.CENTER);
        new_option_container.addView(new_option);
        new_option_container.addView(delete_btn);

        delete_btn.setText("Delete");

        delete_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button b = (Button) v;
                        DeleteOption(b);
                    }
                }

        );

        LinearLayout create_option_layout = (LinearLayout) findViewById(R.id.options_container_layout);
        create_option_layout.addView(new_option_container);

        MapNode new_node = new MapNode(new_option_container,new_option);

        this.button_container_map.put(delete_btn, new_node);
    }

    private void DeleteOption(Button b){

        LinearLayout create_option_layout = (LinearLayout) findViewById(R.id.options_container_layout);
        MapNode to_delete= this.button_container_map.get(b);
        LinearLayout option_to_delete = to_delete.container;
        create_option_layout.removeView(option_to_delete);
        this.button_container_map.remove(b);
    }


    public void SubmitNewPoll(View view) {
        EditText question_text = (EditText) findViewById(R.id.question_text);
        q_text = question_text.getText().toString();
        String body = "";
        for (Map.Entry<Button, MapNode> entry : this.button_container_map.entrySet()) {

            String option_name = entry.getValue().text.getText().toString() ;
            body = body + '\007' + option_name;
            count_map.put(option_name,0);

        }

        LinearLayout main_create_poll = (LinearLayout) findViewById(R.id.main_create_poll);
        LinearLayout create_poll_layout = (LinearLayout) findViewById(R.id.create_poll_layout);

        main_create_poll.removeView(create_poll_layout);

        EditText display_msg = new EditText(this);
        display_msg.setText(question_text.getText().toString() + "\n" + body);
        display_msg.setKeyListener(null);

        Button broadcast_button = new Button(this);
        broadcast_button.setText("start broadcasting");
        ensureDiscoverable();
        broadcast_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startBroadcast();

            }
        });


        //main_create_poll.addView(display_msg);
        poll_str = q_text +  body;
        main_create_poll.addView(broadcast_button);


    }

    private void startBroadcast(){

        sendMessage(Constants.MESSAGE_POLL+'\007'+poll_str);
        if(!poll_started){
            poll_started =true;
            LinearLayout main_create_poll = (LinearLayout) findViewById(R.id.main_create_poll);
            Button broadcast_result = new Button(this);
            broadcast_result.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            computeResult();

                        }
                    }
            );
            broadcast_result.setText("compute results");
            main_create_poll.addView(broadcast_result);
        }

    }


    private void computeResult(){
        String result_msg= q_text + ':' +'\n';

        for (Map.Entry<String, Integer> entry : this.count_map.entrySet()) {

            int op_count = entry.getValue();
            String op_name = entry.getKey();
            result_msg = result_msg + op_name + ':' + op_count + '\n';

        }

        EditText result = new EditText(this);
        result.setText(result_msg);
        result.setKeyListener(null);
        LinearLayout main_create_poll = (LinearLayout) findViewById(R.id.main_create_poll);
        main_create_poll.removeAllViews();
        main_create_poll.addView(result);
        sendMessage(Constants.MESSAGE_RESULT+'\007'+result_msg);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
//            if (m_bt_service == null) {
//                setupBluetooth();
//            }
            if(m_bt_services == null){
                setupBluetooth();
            }
        }
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
//        if(m_bt_service != null){
//            if(m_bt_service.getState() == BluetoothService.STATE_NONE){
//                m_bt_service.start();
//            }
//        }
        if(m_bt_services != null){
            for(BluetoothService b : m_bt_services){
                if(b.getState() == BluetoothService.STATE_NONE){
                    b.start();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(m_bt_service != null){
//            m_bt_service.stop();
//        }
        if(m_bt_services != null){
            for (BluetoothService b:m_bt_services){
                b.stop();
            }
        }
    }

    public void setupBluetooth(){
        m_bt_services = new ArrayList<BluetoothService>();
        BluetoothService temp1 = new BluetoothService(this,mHandler,UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
        BluetoothService temp2 = new BluetoothService(this,mHandler,UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
        m_bt_services.add(temp1);
        m_bt_services.add(temp2);
//        m_bt_service = new BluetoothService(this,mHandler,UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:

//                // When DeviceListActivity returns with a device to connect
//                if (resultCode == Activity.RESULT_OK) {
//                    this.selected_device_mac_addresses = data.getExtras().getStringArrayList(GetDevice.SELECTED_MAC_ADDRESSES);
//                    EditText display_msg = new EditText(this);
//                    String addresses_string = "\n";
//                    for(String a: this.selected_device_mac_addresses){
//                        addresses_string = addresses_string + a + "\n";
//                    }
//                    display_msg.setText(addresses_string);
//                    display_msg.setKeyListener(null);
//                    LinearLayout main_create_poll = (LinearLayout) findViewById(R.id.main_create_poll);
//
//                    main_create_poll.addView(display_msg);

//                }
                if(resultCode == Activity.RESULT_OK){
                   //should never happens in this class
                }

            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
//                    Intent serverIntent = new Intent(getApplicationContext(), GetDevice.class);
//                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                    setupBluetooth();

                }
                else {
                    // User did not enable Bluetooth or an error occurred

                    Toast.makeText(getApplicationContext(), "error enabling bluetooth",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }







    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 480);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
//        if (m_bt_service.getState() != BluetoothService.STATE_CONNECTED) {
//            Toast.makeText(getApplicationContext(), "not connected", Toast.LENGTH_SHORT).show();
//            return;
//        }

        if(m_bt_services ==null){
            Toast.makeText(getApplicationContext(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean hasSerive = false;
        for(BluetoothService b: m_bt_services){
            if (b.getState() == BluetoothService.STATE_CONNECTED) {
                hasSerive = true;
            }
        }
        if(!hasSerive){
            Toast.makeText(getApplicationContext(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }



        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
//            m_bt_service.write(send);

            for(BluetoothService b: m_bt_services){
                b.write(send);
            }

            // Reset out string buffer to zero and clear the edit text field

        }
    }





    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
//                            mTitle.setText(R.string.title_connected_to);
//                            mTitle.append(mConnectedDeviceName);
//                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            //mTitle.setText(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            //mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (readMessage.length() > 0) {
                        int val = count_map.get(readMessage);
                        count_map.put(readMessage,val+1);
                    }
//                    break;


                    break;
                case Constants.MESSAGE_DEVICE_NAME:
//                    // save the connected device's name
//                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                    Toast.makeText(getApplicationContext(), "Connected to "
//                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                    break;
                    break;
                case Constants.MESSAGE_TOAST:
                    //if (!msg.getData().getString(TOAST).contains("Unable to connect device")) {
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    //}
                    break;
            }
        }
    };



}
