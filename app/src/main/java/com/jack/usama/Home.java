package com.jack.usama;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import dmax.dialog.SpotsDialog;

public class Home extends Activity implements SensorEventListener {
    private Button button;
    private TextView name, number, message;
    private SessionManager sessionManager;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private boolean isAccelerometerSensorAvailable;
    private boolean itIsNotFirstTime = false;
    private float currentX, currentY, currentZ;
    private float lastX, lastY, lastZ;
    private float xDifference, yDifference, zDifference;
    private float shakeThreshold = 20F;
    private Vibrator vibrator;
    private int noOfShakes;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double latitudes, longitudes;
    private Geocoder geocoder;
    private List<Address> addresses;
    private String address;
    private PendingIntent sentPI, deliveredPI;
    private BroadcastReceiver broadcastReceiver, broadcastReceiver1;
    private String AudioSavePathInDevice = null;
    private MediaRecorder mediaRecorder;
    private Random random;
    private String RandomAudioFileName = "ABCDEFGHIJKLMNOP";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        name = findViewById(R.id.textViewContactName);
        number = findViewById(R.id.textViewContactNumber);
        message = findViewById(R.id.textViewMessage);

        random = new Random();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        sessionManager = new SessionManager(this);

        name.setText(sessionManager.getContactName());
        number.setText(sessionManager.getContactNumber());
        message.setText(sessionManager.getMessage());

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            isAccelerometerSensorAvailable = true;
        } else {
            Toast.makeText(this, "Accelerometer Sensor Is not available", Toast.LENGTH_SHORT).show();
            isAccelerometerSensorAvailable = false;
        }

        button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(Home.this).build();
            waitingDialog.show();
            button.setText(R.string.fetching_contacts);
            Intent intent = new Intent(Home.this, Edit.class);
            waitingDialog.dismiss();
            startActivity(intent);

        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) ->
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("No", (dialog, id) -> {
                        Toast.makeText(Home.this, "Cannot Access Location ! Permission Denied", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    });
            final AlertDialog alert = builder.create();
            alert.show();
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                try {
                    latitudes = location.getLatitude();
                    longitudes = location.getLongitude();
                    geocoder = new Geocoder(Home.this, Locale.getDefault());
                    addresses = geocoder.getFromLocation(latitudes, longitudes, 1);
                    address = addresses.get(0).getAddressLine(0);
                } catch (IOException e) {
                    Toast.makeText(Home.this, "Unknown Error in Getting Location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter(SENT));

        broadcastReceiver1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        registerReceiver(broadcastReceiver1, new IntentFilter(DELIVERED));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(broadcastReceiver1);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        currentX = event.values[0];
        currentY = event.values[1];
        currentZ = event.values[2];

        if (itIsNotFirstTime) {
            xDifference = Math.abs(lastX - currentX);
            yDifference = Math.abs(lastY - currentY);
            zDifference = Math.abs(lastZ - currentZ);

            if ((xDifference > shakeThreshold && yDifference < shakeThreshold) ||
                    (xDifference > shakeThreshold && zDifference > shakeThreshold) ||
                    (yDifference > shakeThreshold && zDifference > shakeThreshold)) {

                noOfShakes++;

                if (noOfShakes % 3 == 0) {
                    String messageToBeSent = message.getText().toString() + "\n" + "Location : " + address;
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(sessionManager.getContactNumber(), null, messageToBeSent, sentPI, deliveredPI);
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));

                    AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                                    CreateRandomAudioFileName() + "AudioRecording.3gp";

                    MediaRecorderReady();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException | IOException e) {
                        Toast.makeText(this, "Unable to Record Voice", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(Home.this, "Recording started", Toast.LENGTH_LONG).show();

                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + sessionManager.getContactNumber()));
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivity(callIntent);
                }
            }
        }

        lastX = currentX;
        lastY = currentY;
        lastZ = currentZ;

        itIsNotFirstTime = true;
    }

    private String CreateRandomAudioFileName() {
        StringBuilder stringBuilder = new StringBuilder(5);
        int i = 0;
        while (i < 5) {
            stringBuilder.append(RandomAudioFileName.charAt(random.nextInt(RandomAudioFileName.length())));
            i++;
        }
        return stringBuilder.toString();
    }

    private void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setMaxDuration(60000);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do Something Here.
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAccelerometerSensorAvailable) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isAccelerometerSensorAvailable) {
            sensorManager.unregisterListener(this);
        }
    }
}