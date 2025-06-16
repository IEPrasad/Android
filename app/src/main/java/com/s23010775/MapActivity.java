package com.s23010775;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private EditText editAddress;
    private Button btnShowLocation, btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        editAddress = findViewById(R.id.edit_address);
        btnShowLocation = findViewById(R.id.button_show_location);
        btnNext = findViewById(R.id.button_next_sensor);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnShowLocation.setOnClickListener(v -> {
            String address = editAddress.getText().toString();
            if (!address.isEmpty()) {
                Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocationName(address, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address location = addresses.get(0);
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    } else {
                        Toast.makeText(MapActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(MapActivity.this, "Geocoding failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MapActivity.this, "Enter an address", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v -> startActivity(new Intent(MapActivity.this, SensorActivity.class)));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }
} 