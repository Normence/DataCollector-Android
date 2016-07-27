package com.normence.datacollector;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.UnableToConnectException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class BluetoothConnectedActivity extends AppCompatActivity {
    private static String TAG = "BluetoothConnectedActivity";
    private SharedPreferences SP;

    private TextView mVehicleSpeedView;

    private String appDirName;
    public static File SessionDir;
    private String visualpath;
    private FileHandler fileHandler;

    private SensorManager mSensorManager;
    private SensorEventListener mListener;
    private List<Sensor> currentDevice = new ArrayList<Sensor>();
    private int sensorScanRate;

    private BluetoothSocket mBluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Context ctx;
    private TextView textViewStatus, textviewSpeed, textviewAccelerometer, textviewGravity, textviewGyroscope;

    Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                for(int i = 0; i < 1000; i++) {
                    Log.d(TAG, "Execution times: " + i);
                    Execute();
                }
            }catch (InterruptedException e){
                t.interrupt();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connected_service);
        SP = PreferenceManager.getDefaultSharedPreferences(this);

        sensorScanRate = SP.getInt("SEEKBAR_VALUE_SENSOR", 20000);

        Intent intent = getIntent();
        String address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        fileHandler = new FileHandler(BluetoothConnectedActivity.this);
        appDirName = getString(R.string.appDirName);

        textviewSpeed = (TextView) findViewById(R.id.textview_speed);
        textviewAccelerometer = (TextView) findViewById(R.id.textview_accelerometer);
        textviewGravity = (TextView) findViewById(R.id.textview_gravity);
        textviewGyroscope = (TextView) findViewById(R.id.textview_gyroscope);
        textViewStatus = (TextView) findViewById(R.id.textview_status);
        textViewStatus.setText("Status: Connecting...");
        final TextView textView = (TextView) findViewById(R.id.textview_device);
        textView.setText("Device Connected: ");

        Button buttonStop = (Button) findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewStatus.setText("Status: Finished");
                Log.d(TAG, "Stop Execution");
                if (mSensorManager != null) {
                    mSensorManager.unregisterListener(mListener);
                }
                finish();
            }
        });


        /*  OBD part  *//*
        if (address == null || "".equals(address)) {
            Toast.makeText(this, getString(R.string.text_bluetooth_nodevice), Toast.LENGTH_LONG).show();

            // log error
            Log.e(TAG, "No Bluetooth device has been selected.");

            finish();
        }
        else if(!address.equals("88:1B:99:04:35:16")){
            Toast.makeText(this, "This is not an OBD device", Toast.LENGTH_LONG).show();

            // log error
            Log.e(TAG, "Wrong OBD device has been selected.");

            finish();
        }

        BluetoothDevice device = MainActivity.mBluetoothAdapter.getRemoteDevice(address);*/
        /////////////////

        Log.d(TAG, "Stopping Bluetooth discovery.");
        MainActivity.mBluetoothAdapter.cancelDiscovery();

        /* File management */
        Log.i(TAG, "Set up files");
        try {
            File ProjectDir = new File(Environment.getExternalStorageDirectory() + File.separator + appDirName);
            Log.d(TAG, "ProjectDir: " + ProjectDir.getAbsolutePath());

            if (!ProjectDir.exists()) {
                if (ProjectDir.mkdir()) {
                    Log.d(TAG, "Create dir: " + appDirName);
                } else {
                    Log.d(TAG, "Fail to create dir: " + appDirName);
                }
            }

            ProjectDir = new File(ProjectDir + File.separator + "OBDnSensors");
            Log.d(TAG, "ProjectDir: " + ProjectDir.getAbsolutePath());

            if (!ProjectDir.exists()) {
                if (ProjectDir.mkdir()) {
                    Log.d(TAG, "Create dir: " + appDirName + "/OBDnSensors");
                } else {
                    Log.d(TAG, "Fail to create dir: " + appDirName + "/OBDnSensors");
                }
            }

            Calendar calNow = Calendar.getInstance();
            Date current = new Date();
            calNow.setTimeInMillis(current.getTime());
            TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            String sessionName = (calNow.get(Calendar.MONTH) + 1) + "_" + calNow.get(Calendar.DATE) + "_" + calNow.get(Calendar.YEAR) + "_" +
                    calNow.get(Calendar.HOUR_OF_DAY) + "-" + calNow.get(Calendar.MINUTE) +
                    "-" + calNow.get(Calendar.SECOND) + "(" + tm.getDeviceId() + ")";
            SessionDir = new File(ProjectDir + File.separator + sessionName);
            Log.d(TAG, "SessionDir: " + SessionDir.getAbsolutePath());
            if (!SessionDir.exists()) {
                if (SessionDir.mkdir()) {
                    Log.d(TAG, "Create dir (OnS): " + sessionName);
                } else {
                    Log.d(TAG, "Fail to create dir (OnS): " + sessionName);
                }
            }
            visualpath = SessionDir.getAbsolutePath();

            fileHandler.setStreamsNull();

            fileHandler.createVehicleSpeed(SessionDir + File.separator + "Speed.txt");
            fileHandler.createAccelerometer(SessionDir + File.separator + "Acceletrometer.txt");
            fileHandler.createGravity(SessionDir + File.separator + "Gravity.txt");
            fileHandler.createGyroscope(SessionDir + File.separator + "Gyroscope.txt");
            fileHandler.createSummaryFile(SessionDir + File.separator + "Summary File.txt");
        }catch (IOException e) {
            e.printStackTrace();
        }
        //////////////////////////////

        /* Android Sensors */
        Log.i(TAG, "set up SensorManager");
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
            currentDevice.add(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null)
            currentDevice.add(mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null)
            currentDevice.add(mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));

        mListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Date current = new Date();
                DecimalFormat REAL_FORMATTER = new DecimalFormat("0.###");

                if (event.sensor == mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) && !fileHandler.isAccelerometerStreamNull()) {// && !lowAccuracy[currentDevice.indexOf
                    //(event.sensor)]) {
                    String add = Long.toString(current.getTime()) + "\t" + event.values[0] + "\t" + event.values[1] + "\t" + event.values[2] + "\n";
                    try {
                        fileHandler.accelerometerStreamWrite(add);
                    } catch (IOException e) {
                        Toast.makeText(BluetoothConnectedActivity.this, "Accel record fail; queue full", Toast.LENGTH_SHORT).show();
                    }
                    textviewAccelerometer.setText("accelerometer: " + REAL_FORMATTER.format(event.values[0]) + "    " + REAL_FORMATTER.format(event.values[1]) + "    " + REAL_FORMATTER.format(event.values[2]));
                }
                if (event.sensor == mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) && !fileHandler.isGravityStreamNull()) {// && !lowAccuracy[currentDevice.indexOf
                    //(event.sensor)]) {
                    String add = Long.toString(current.getTime()) + "\t" + event.values[0] + "\t" + event.values[1] + "\t" + event.values[2] + "\n";
                    try {
                        fileHandler.gravityStreamWrite(add);
                    } catch (IOException e) {
                        Toast.makeText(BluetoothConnectedActivity.this, "Gravity record fail; queue full", Toast.LENGTH_SHORT).show();
                    }
                    textviewGravity.setText("gravity: " + REAL_FORMATTER.format(event.values[0]) + "    " + REAL_FORMATTER.format(event.values[1]) + "    " + REAL_FORMATTER.format(event.values[2]));
                }
                if (event.sensor == mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) && !fileHandler.isGyroscopeStreamNull()) {// && !lowAccuracy[currentDevice.indexOf
                    //(event.sensor)]) {
                    String add = Long.toString(current.getTime()) + "\t" + event.values[0] + "\t" + event.values[1] + "\t" + event.values[2] + "\n";
                    try {
                        fileHandler.gyroscopeStreamWrite(add);
                    } catch (IOException e) {
                        Toast.makeText(BluetoothConnectedActivity.this, "Gyro record fail; queue full", Toast.LENGTH_SHORT).show();
                    }
                    textviewGyroscope.setText("gyroscope: " + REAL_FORMATTER.format(event.values[0]) + "    " + REAL_FORMATTER.format(event.values[1]) + "    " + REAL_FORMATTER.format(event.values[2]));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        for (Sensor insert : currentDevice) {
            mSensorManager.registerListener(mListener, insert, sensorScanRate, 100);
        }
        /* end sensors *///////////////////

        /*   OBD   *//*
        try {
            Log.d(TAG, getString(R.string.service_starting));
            Toast.makeText(this, device.getName() + ": connecting", Toast.LENGTH_SHORT).show();
            try {
                mBluetoothSocket = BluetoothConnectService.connect(device);
            } catch (Exception e) {
                Log.e(TAG, "There was an error while establishing Bluetooth connection. Stopping app..", e);
                finish();
                return;
//                stopService();
//                throw new IOException();
            }
        } catch (Exception e) {
            Log.e(TAG, "There was an error while establishing connection. -> " + e.getMessage());

            finish();
            return;
            // in case of failure, stop this service.
//                    stopService();
//                    throw new IOException();
        }

        Log.d(TAG, getString(R.string.service_started));
        Toast.makeText(this, device.getName() + ": connected", Toast.LENGTH_SHORT).show();
        textViewStatus.setText("Status: Connected");
        textView.append(device.getName());
//                showNotification(getString(R.string.notification_action), getString(R.string.service_started), R.drawable.ic_btcar, true, true, false);

        try {
            inputStream = mBluetoothSocket.getInputStream();
            outputStream = mBluetoothSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ctx = BluetoothConnectedActivity.this;
        Initialize();
        textViewStatus.setText("Status: Receiving data...");
        t.start();*/
        /*  OBD end  *////////////////

//            finish();


    }

    private void Initialize(){
        Log.d(TAG, "Initializing OBD...");
        textViewStatus.setText("Status: Initializing...");

        ObdCommand obdCommand = new ObdResetCommand();
        try {
             obdCommand.run(inputStream, outputStream);
        } catch (IOException e) {
            Log.e(TAG, "Error at ObdResetCommand");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e(TAG, "Error at ObdResetCommand");
            e.printStackTrace();
        }
        Log.d(TAG, "Complete ObdResetCommand");

        obdCommand = new EchoOffCommand();
        try {
            obdCommand.run(inputStream, outputStream);
        } catch (IOException e) {
            Log.e(TAG, "Error at EchoOffCommand1");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e(TAG, "Error at EchoOffCommand1");
            e.printStackTrace();
        }
        Log.d(TAG, "Complete EchoOffCommand1");

    /*
     * Will send second-time based on tests.
     *
     * TODO this can be done w/o having to queue jobs by just issuing
     * command.run(), command.getResult() and validate the result.
     */
        obdCommand = new EchoOffCommand();
        try {
            obdCommand.run(inputStream, outputStream);
        } catch (IOException e) {
            Log.e(TAG, "Error at EchoOffCommand2");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e(TAG, "Error at EchoOffCommand2");
            e.printStackTrace();
        }
        Log.d(TAG, "Complete EchoOffCommand2");

        obdCommand = new LineFeedOffCommand();
        try {
            obdCommand.run(inputStream, outputStream);
        } catch (IOException e) {
            Log.e(TAG, "Error at LineFeedOffCommand");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e(TAG, "Error at LineFeedOffCommand");
            e.printStackTrace();
        }
        Log.d(TAG, "Complete LineFeedOffCommand");

        obdCommand = new TimeoutCommand(62);
        try {
            obdCommand.run(inputStream, outputStream);
        } catch (IOException e) {
            Log.e(TAG, "Error at TimeoutCommand");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e(TAG, "Error at TimeoutCommand");
            e.printStackTrace();
        }
        Log.d(TAG, "Complete TimeoutCommand");


        // Get protocol from preferences
        final String protocol = "AUTO"/*prefs.getString("obd_protocols_preference", "AUTO")*/;
        obdCommand = new SelectProtocolCommand(ObdProtocols.valueOf(protocol));
        Log.d("ProtocolSelection", protocol);
        try {
            obdCommand.run(inputStream, outputStream);
        } catch (IOException e) {
            Log.e(TAG, "Error at SelectProtocolCommand");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e(TAG, "Error at SelectProtocolCommand");
            e.printStackTrace();
        }
        Log.d(TAG, "Complete SelectProtocolCommand");

        // Job for returning dummy data
//        obdCommand = new AmbientAirTemperatureCommand();
//        try {
//            obdCommand.run(inputStream, outputStream);
//        } catch (IOException e) {
//            Log.e(TAG, "Error at AmbientAirTemperatureCommand");
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            Log.e(TAG, "Error at AmbientAirTemperatureCommand");
//            e.printStackTrace();
//        }
        Log.d(TAG, "Finished Initialization");
        textViewStatus.setText("Initialized");
    }

    private void Execute() throws InterruptedException{
        Log.d(TAG, "Executing...");

        SpeedCommand command = new SpeedCommand();

//        for(int i = 0; i < 10; i++){
//            Log.d("I", "" + i);
            if(mBluetoothSocket.isConnected()) {
                try {
                    command.run(inputStream, outputStream);
                } catch (IOException e) {
                    Log.e(TAG, "Error at SpeedCommand");
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error at SpeedCommand");
                    e.printStackTrace();
                } catch (UnableToConnectException e){
                    Log.e(TAG, "Speed: Unable to connect");
                }

                final String speed = Long.toString(command.getMetricSpeed());
                // UiThread
                ((BluetoothConnectedActivity) ctx).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((BluetoothConnectedActivity) ctx).Update(speed);
                    }
                });

            }else{
                Log.e(TAG, "Socket is not connected while executing");
            }

//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Stopping Activity");

        if(mBluetoothSocket != null){
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void Update(String speed){
        Date current = new Date();
        String content = Long.toString(current.getTime()) + "\t" + speed + "\n";
        try{
            fileHandler.speedStreamWrite(content);
        }catch (IOException e){
        }catch (NullPointerException e){
            Log.e(TAG, "Null Pointer Execption");
        }
        textviewSpeed.setText("Speed: " + speed);
    }
}
