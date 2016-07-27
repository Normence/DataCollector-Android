package com.normence.datacollector;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Normence on 16/4/19.
 */
public class FileHandler {
    private final Context mContext;

    private File speed;
    private File angle;
    private File odometer;
    private File accelerometer;
    private File gravity;
    private File gyroscope;
    private File summaryFile;

    private BufferedWriter speed_ofstream;
    private BufferedWriter angle_ofstream;
    private BufferedWriter odometer_ofsteam;
    private BufferedWriter accelerometer_ofstream;
    private BufferedWriter gravity_ofstream;
    private BufferedWriter gyroscope_ofstream;
    private BufferedWriter summaryFile_ofstream;

    public FileHandler(Context context){
        this.mContext = context;
    }

    public void setStreamsNull(){
        speed_ofstream = null;
        angle_ofstream = null;
        odometer_ofsteam = null;
        accelerometer_ofstream = null;
        gravity_ofstream = null;
        gyroscope_ofstream = null;
        summaryFile_ofstream = null;
    }

    /* create files */
    public void createVehicleSpeed(String dir) throws IOException{
        speed = new File(dir);
        speed_ofstream = new BufferedWriter(new FileWriter(speed));
    }

    public void createSteeringWheelAngle(String dir) throws IOException{
        angle = new File(dir);
        angle_ofstream = new BufferedWriter(new FileWriter(angle));
    }

    public void createOdometer(String dir) throws IOException{
        odometer = new File(dir);
        odometer_ofsteam = new BufferedWriter(new FileWriter(odometer));
    }

    public void createAccelerometer(String dir) throws IOException{
        accelerometer = new File(dir);
        accelerometer_ofstream = new BufferedWriter(new FileWriter(accelerometer));
    }

    public void createGravity(String dir) throws IOException{
        gravity = new File(dir);
        gravity_ofstream = new BufferedWriter(new FileWriter(gravity));
    }

    public void createGyroscope(String dir) throws IOException{
        gyroscope = new File(dir);
        gyroscope_ofstream = new BufferedWriter(new FileWriter(gyroscope));
    }

    public void createSummaryFile(String dir) throws IOException{
        summaryFile = new File(dir);
        summaryFile_ofstream = new BufferedWriter(new FileWriter(summaryFile));
    }

    /* write into files */
    public void speedStreamWrite(String content) throws IOException, NullPointerException{
        speed_ofstream.write(content);
        speed_ofstream.flush();
    }

    public void angleStreamWrite(String content) throws IOException, NullPointerException{
        angle_ofstream.write(content);
        angle_ofstream.flush();
    }

    public void odometerStreamWrite(String content) throws IOException, NullPointerException{
        odometer_ofsteam.write(content);
        odometer_ofsteam.flush();
    }

    public void accelerometerStreamWrite(String content) throws IOException, NullPointerException{
        accelerometer_ofstream.write(content);
        accelerometer_ofstream.flush();
    }

    public void gravityStreamWrite(String content) throws IOException, NullPointerException{
        gravity_ofstream.write(content);
        gravity_ofstream.flush();
    }

    public void gyroscopeStreamWrite(String content) throws IOException, NullPointerException{
        gyroscope_ofstream.write(content);
        gyroscope_ofstream.flush();
    }

    public void summaryFileStreamWrite(String content) throws IOException, NullPointerException{
        summaryFile_ofstream.write(content);
        summaryFile_ofstream.flush();
    }

    public boolean isSpeedStreamNull(){return speed_ofstream == null;}
    public boolean isAngleStreamNull(){return angle_ofstream == null;}
    public boolean isOdometerStreamNull(){return odometer_ofsteam == null;}
    public boolean isAccelerometerStreamNull(){return accelerometer_ofstream == null;}
    public boolean isGravityStreamNull(){return gravity_ofstream == null;}
    public boolean isGyroscopeStreamNull(){return gyroscope_ofstream == null;}
}
