package com.normence.datacollector;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";

    public static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//    private BluetoothSocket mBluetoothSocket;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private ListView mPairedDevicesListView;
    private ListView mNewDevicesListView;

    private Button mScanButton, mStarterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing adapter
        mPairedDevicesArrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item);
        mNewDevicesArrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item);

        // Initializing the button to perform devices discovered
        mScanButton = (Button) findViewById(R.id.button_scan);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mNewDevicesArrayAdapter.clear();
                doDiscovery();
            }
        });

        mStarterButton = (Button) findViewById(R.id.button_starter);
        mStarterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StarterActivity.class);
                startActivity(intent);
            }
        });

        if (mBluetoothAdapter == null) {
            Toast toast = Toast.makeText(MainActivity.this, "Bluetooth is not supported on this device.", Toast.LENGTH_LONG);
            Log.d(TAG, "Bluetooth is not supported on this device.");
            toast.show();
            finish();
        } else {

            Toast toast = Toast.makeText(MainActivity.this, "Bluetooth is supported on this device.", Toast.LENGTH_SHORT);
            toast.show();


            int REQUEST_ENABLE_BT = 2;

            // enable Bluetooth
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            // get paired devices
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            toast.show();
            if (pairedDevices.size() > 0) {
//                TextView textView = new TextView()
                for (BluetoothDevice device : pairedDevices) {
                    mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }

            // register the BroadcastReceiver
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);

            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mReceiver, filter);

            // Enabling discoverability
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
            startActivity(discoverableIntent);

            // connect ListViews with Adapters
            mPairedDevicesListView = (ListView) findViewById(R.id.listview_paired_devices);
            mPairedDevicesListView.setAdapter(mPairedDevicesArrayAdapter);
            mPairedDevicesListView.setOnItemClickListener(mDeviceClickListener);
            mNewDevicesListView = (ListView) findViewById(R.id.listview_new_devices);
            mNewDevicesListView.setAdapter(mNewDevicesArrayAdapter);
            mNewDevicesListView.setOnItemClickListener(mDeviceClickListener);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }

        this.unregisterReceiver(mReceiver);
    }

    private void doDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        mBluetoothAdapter.startDiscovery();
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mBluetoothAdapter.cancelDiscovery();

            // get the MAC address, which is the last 17 chars in the view
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            Log.d(TAG, "Target address: " + address);

            // create the result Intent and include the MAC address
            Intent intent = new Intent(MainActivity.this, BluetoothConnectedActivity.class);
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(intent);

//            setResult(RESULT_OK, intent);
        }
    };

    // discover devices
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // ensure not to present repeated device
                // p.s. position starts from '0'
                for(int count = 1; count <= mNewDevicesArrayAdapter.getCount(); count++){
                    if(mNewDevicesArrayAdapter.getItem(count - 1).equals(device.getName() + "\n" + device.getAddress())){
                        Log.d(TAG, "Same device found: " + device.getName());
                        return;
                    }
                }
                ////////////////////////////////////////
                mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                Log.d(TAG, "Devices number: " + mNewDevicesArrayAdapter.getCount());
            }
        }
    };
}



/*
// managing a connection
private class ConnectedThread extends Thread{
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectThread(BluetoothSocket socket){
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        }catch (IOException e){}

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run(){
        byte[] buffer = new byte[1024];
        int bytes;

        while(true){
            try {
                bytes = mmInStream.read(buffer);
                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
            }catch (IOException e){
                break;
            }
        }
    }

    public void write(byte[] bytes){
        try {
            mmOutStream.write(bytes);
        }catch (IOException e){}
    }

    public void cancel(){
        try {
            mmSocket.close();
        }catch (IOException e){}
    }
}*/
