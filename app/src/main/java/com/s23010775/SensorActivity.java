package com.s23010775;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor tempSensor;
    private TextView tempText;
    private MediaPlayer mediaPlayer;
    private static final float TEMP_THRESHOLD = 75f; // Last two digits of SID
    private boolean played = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        tempText = findViewById(R.id.text_temperature);
        ImageView imageView = findViewById(R.id.image_multimedia);
        imageView.setImageResource(R.drawable.sample_image); // Add sample_image.png to drawable
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mediaPlayer = MediaPlayer.create(this, R.raw.sample_audio); // Add sample_audio.mp3 to raw
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tempSensor != null) {
            sensorManager.registerListener(this, tempSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "No temperature sensor found!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float temp = event.values[0];
        tempText.setText("Temperature: " + temp + "°C");
        if (temp > TEMP_THRESHOLD && !played) {
            mediaPlayer.start();
            played = true;
            Toast.makeText(this, "Temperature threshold exceeded! Playing audio.", Toast.LENGTH_SHORT).show();
        }
        if (temp <= TEMP_THRESHOLD) {
            played = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
} 