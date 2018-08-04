package com.uclacsm117.mysimlpepoll;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

public class ConnectToVote extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private static final String TAG = "Connect To Vote";
    private static final boolean D = true;


    private static final int REQUEST_CONNECT_DEVICE = 1;

    private static final int REQUEST_ENABLE_BT = 2;

    private static final int VOTE_RESULT = 3;

    private BluetoothService m_bt_service = null;

    private String q_text;



    private boolean voted = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_vote);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {

            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {

        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else{
            if(m_bt_service == null){
                setupBluetooth();
            }
        }

    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
        if(m_bt_service != null){

                if(m_bt_service.getState() == BluetoothService.STATE_NONE){
                    m_bt_service.start();
                }


        }
    }

    public void setupBluetooth(){
//        m_bt_services  = new ArrayList<BluetoothService>();
//        BluetoothService temp1 =
//                new BluetoothService(getApplicationContext(),mHandler,UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
//        BluetoothService temp2 =
//                new BluetoothService(getApplicationContext(),mHandler,UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
//        m_bt_services.add(temp1);
//        m_bt_services.add(temp2);
        m_bt_service = new BluetoothService(getApplicationContext(),mHandler,UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(m_bt_service != null)
        {
            m_bt_service.stop();

        }
    }

    private void sendMessage(String message){
        if(m_bt_service.getState() != BluetoothService.STATE_CONNECTED){
            Toast.makeText(this,"not connected",Toast.LENGTH_SHORT).show();
            return;
        }
        if(message.length() > 0){
            byte[] send = message.getBytes();
            m_bt_service.write(send);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LinearLayout connect_to_vote_main = (LinearLayout) findViewById(R.id.main_connect_to_vote);
        switch (requestCode) {
            case VOTE_RESULT:
                Bundle b = data.getExtras();
                sendMessage(b.getString(VoteActivity.SELECTION));


                connect_to_vote_main.removeAllViews();

                TextView wait_msg = new TextView(this);
                wait_msg.setText("waiting for result");

                connect_to_vote_main.addView(wait_msg);

                break;
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
                    String device_address = data.getExtras().getString(GetDevice.SELECTED_DEVICE);
                    EditText display_msg = new EditText(this);

                    display_msg.setKeyListener(null);
                    display_msg.setText(device_address);
                    connect_to_vote_main.addView(display_msg);

                    connectDevice(data);
                }


                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
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

    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(GetDevice.SELECTED_DEVICE);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        m_bt_service.connect(device);
    }



    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            LinearLayout connect_to_vote_main = (LinearLayout) findViewById(R.id.main_connect_to_vote);
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
//                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
//                            Toast.makeText(getApplicationContext(), "connected",
//                            Toast.LENGTH_SHORT).show();
////                            mTitle.setText(R.string.title_connected_to);
////                            mTitle.append(mConnectedDeviceName);
////                            mConversationArrayAdapter.clear();
//                            break;
//                        case BluetoothService.STATE_CONNECTING:
//                            //mTitle.setText(R.string.title_connecting);
//                            break;
//                        case BluetoothService.STATE_LISTEN:
//                        case BluetoothService.STATE_NONE:
//                            //mTitle.setText(R.string.title_not_connected);
//                            break;
//                    }
//                    Toast.makeText(getApplicationContext(), msg.arg1,
//                            Toast.LENGTH_SHORT).show();
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
                    final String readMessage = new String(readBuf, 0, msg.arg1);
                   if (readMessage.length() > 0) {
//                        mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                        if(!voted){
                            voted =true;


//                            Intent intent = new Intent(getApplicationContext(),VoteActivity.class);
//                            Bundle b = new Bundle();
//                            b.putString(VoteActivity.QUESTION_OPTION_TEXT,readMessage);
//
//                            intent.putExtras(b);
//                            startActivityForResult(intent, VOTE_RESULT);


                            connect_to_vote_main.removeAllViews();
                            Button bb = new Button(getApplicationContext());
                            bb.setText("Vote");
                            bb.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getApplicationContext(),VoteActivity.class);
                                    Bundle b = new Bundle();
                                    b.putString(VoteActivity.QUESTION_OPTION_TEXT,readMessage);

                                    intent.putExtras(b);
                                    startActivityForResult(intent, VOTE_RESULT);
                                        }
                                    });
                            Toast.makeText(getApplicationContext(), readMessage,
                           Toast.LENGTH_SHORT).show();
                            connect_to_vote_main.addView(bb);

                        }
                       else{
                            String []temp_arr = readMessage.split("\007");
                            if(temp_arr[0].equals(Constants.MESSAGE_RESULT)){
                                connect_to_vote_main.removeAllViews();
                                EditText result_t = new EditText(getApplicationContext());
                                result_t.setOnClickListener(null);
                                result_t.setText(temp_arr[1]);
                                connect_to_vote_main.addView(result_t);
                            }
                        }



                   }
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

    public void GetRemoteDevice(View view) {
        Intent intent = new Intent(getApplicationContext(), GetDevice.class);
        startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
    }
}
