package com.normence.datacollector;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import com.openxc.VehicleManager;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.VehicleSpeed;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class StarterActivity extends AppCompatActivity {
    private static final String TAG = "StarterActivity";

    private VehicleManager mVehicleManager;
    private TextView mVehicleSpeedView, mEngineSpeedView, mSteeringWheelAngleView, mOdometerView;

    private String appDirName;
    public static File SessionDir;
    private String visualpath;
    private FileHandler fileHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);

        fileHandler = new FileHandler(StarterActivity.this);
        appDirName = getString(R.string.appDirName);

        Log.i(TAG, "Set up files");
        try{
            File ProjectDir = new File(Environment.getExternalStorageDirectory() + File.separator + appDirName);
            Log.d(TAG, "ProjectDir: "+ ProjectDir.getAbsolutePath());

            if (!ProjectDir.exists()) {
                if(ProjectDir.mkdir()) {
                    Log.d(TAG, "Create dir: " + appDirName);
                }
                else {
                    Log.d(TAG, "Fail to create dir: " + appDirName);
                }
            }

            ProjectDir = new File(ProjectDir + File.separator + "OpenXC");
            Log.d(TAG, "ProjectDir: "+ ProjectDir.getAbsolutePath());

            if (!ProjectDir.exists()) {
                if(ProjectDir.mkdir()) {
                    Log.d(TAG, "Create dir: " + appDirName + "/OpenXC");
                }
                else {
                    Log.d(TAG, "Fail to create dir: " + appDirName + "/OpenXC");
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
            Log.d(TAG, "SessionDir: "+ SessionDir.getAbsolutePath());
            if (!SessionDir.exists()) {
                if(SessionDir.mkdir()) {
                    Log.d(TAG, "Create dir (OpenXC): " + sessionName);
                }
                else {
                    Log.d(TAG, "Fail to create dir (OpenXC): " + sessionName);
                }
            }

            fileHandler.setStreamsNull();

            fileHandler.createVehicleSpeed(SessionDir + File.separator + "Speed.txt");
            fileHandler.createSteeringWheelAngle(SessionDir + File.separator + "SteeringWheelAngle.txt");
            fileHandler.createOdometer(SessionDir + File.separator + "Odometer.txt");

            visualpath = SessionDir.getAbsolutePath();
        }catch (IOException e){
            e.printStackTrace();
        }

        mVehicleSpeedView = (TextView) findViewById(R.id.text_vehiclespeed);
        mEngineSpeedView = (TextView) findViewById(R.id.text_enginespeed);
        mSteeringWheelAngleView = (TextView) findViewById(R.id.text_steeringwheelangle);
        mOdometerView = (TextView) findViewById(R.id.text_odometer);
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mVehicleManager != null){
            Log.i(TAG, "Unbinding from vehicle Manager");
            mVehicleManager.removeListener(EngineSpeed.class, mEngineSpeedListener);
            mVehicleManager.removeListener(VehicleSpeed.class, mVehicleSpeedListener);
            mVehicleManager.removeListener(SteeringWheelAngle.class, mSteeringWheelAngleListener);
            mVehicleManager.removeListener(Odometer.class, mOdometerListener);
            unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mVehicleManager == null){
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    VehicleSpeed.Listener mVehicleSpeedListener = new VehicleSpeed.Listener(){
        @Override
        public void receive(Measurement measurement) {
            final VehicleSpeed speed = (VehicleSpeed) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVehicleSpeedView.setText("Vehicle speed(km/h): " + speed.getValue().doubleValue());

                    Date current = new Date();
                    String content = Long.toString(current.getTime()) + "\t" + speed.getValue().toString() + "\n";
                    try{
                        fileHandler.speedStreamWrite(content);
                    }catch (IOException e){
                    }catch (NullPointerException e){
                        Log.e(TAG, "Null Pointer Execption");
                    }
                }
            });
        }
    };

    EngineSpeed.Listener mEngineSpeedListener = new EngineSpeed.Listener(){
        @Override
        public void receive(Measurement measurement) {
            final EngineSpeed speed = (EngineSpeed) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mEngineSpeedView.setText("Engine speed(RPM): " + speed.getValue().doubleValue());
                }
            });
        }
    };

    SteeringWheelAngle.Listener mSteeringWheelAngleListener = new SteeringWheelAngle.Listener(){
        @Override
        public void receive(Measurement measurement) {
            final SteeringWheelAngle angle = (SteeringWheelAngle) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSteeringWheelAngleView.setText("Steering Wheel Angle(degrees): " + angle.getValue().toString());

                    Date current = new Date();
                    String content = Long.toString(current.getTime()) + "\t" + angle.getValue().doubleValue() + "\n";
                    try{
                        fileHandler.angleStreamWrite(content);
                    }catch (IOException e){
                    }catch (NullPointerException e){
                        Log.e(TAG, "Null Pointer Execption");
                    }
                }
            });
        }
    };

    Odometer.Listener mOdometerListener = new Odometer.Listener(){
        @Override
        public void receive(Measurement measurement) {
            final Odometer odometer = (Odometer) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mOdometerView.setText("Odometer(km): " + odometer.getValue().doubleValue());

                    Date current = new Date();
                    String content = Long.toString(current.getTime()) + "\t" + odometer.getValue().toString() + "\n";
                    try{
                        fileHandler.odometerStreamWrite(content);
                    }catch (IOException e){
                    }catch (NullPointerException e){
                        Log.e(TAG, "Null Pointer Execption");
                    }
                }
            });
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder) service).getService();

            mVehicleManager.addListener(VehicleSpeed.class, mVehicleSpeedListener);
            mVehicleManager.addListener(EngineSpeed.class, mEngineSpeedListener);
            mVehicleManager.addListener(SteeringWheelAngle.class, mSteeringWheelAngleListener);
            mVehicleManager.addListener(Odometer.class, mOdometerListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "VehicleManager Service disconnected unexpectedly");
            mVehicleManager = null;
        }
    };
}
