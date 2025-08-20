package com.s23010155.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.s23010155.R;
import com.s23010155.databinding.FragmentProfileBinding;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment implements OnMapReadyCallback {

    private FragmentProfileBinding binding;
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ProfilePrefs";
    // Keys for SharedPreferences
    private static final String KEY_NAME = "profileName";
    private static final String KEY_BIRTHDAY = "profileBirthday";
    private static final String KEY_NIC = "profileNic";
    private static final String KEY_DISTRICT = "profileDistrict";
    private static final String KEY_REGION = "profileRegion";
    private static final String KEY_RELIGION = "profileReligion";
    private static final String KEY_LAT = "profileLat";
    private static final String KEY_LNG = "profileLng";
    private static final String KEY_ADDRESS = "profileAddress";
    private Marker activeMarker;

    private final ActivityResultLauncher<android.content.Intent> placesLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
                        dropOrMoveMarker(latLng, null);
                        binding.editTextLocationSearchProfile.setText(place.getName());
                        binding.textSelectedAddressProfile.setText(place.getAddress());
                        sharedPreferences.edit().putString(KEY_ADDRESS, place.getAddress()).apply();
                    }
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your app.
                    getLastKnownLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission is required to tag location.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Init Places SDK
        try {
            if (!Places.isInitialized()) {
                Places.initialize(requireContext().getApplicationContext(), getString(R.string.google_maps_key));
            }
        } catch (Exception ignored) {}

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
        loadProfileData();
    }

    private void setupUI() {
        binding.editTextBirthday.setOnClickListener(v -> showDatePickerDialog());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.districts_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDistrict.setAdapter(adapter);

        binding.buttonSaveProfile.setOnClickListener(v -> saveProfileData());

        // Search click and drawable tap
        binding.editTextLocationSearchProfile.setOnClickListener(v -> launchPlacesSearch());
        binding.editTextLocationSearchProfile.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                EditText et = binding.editTextLocationSearchProfile;
                if (et.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                    int drawableWidth = et.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    float touchX = event.getX();
                    int rightEdge = et.getWidth() - et.getPaddingRight();
                    int leftEdgeOfRightDrawable = rightEdge - drawableWidth;
                    if (touchX >= leftEdgeOfRightDrawable) {
                        launchPlacesSearch();
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
            binding.editTextBirthday.setText(selectedDate);
        }, year, month, day).show();
    }

    private void loadProfileData() {
        binding.editTextFullName.setText(sharedPreferences.getString(KEY_NAME, ""));
        binding.editTextBirthday.setText(sharedPreferences.getString(KEY_BIRTHDAY, ""));
        binding.editTextNic.setText(sharedPreferences.getString(KEY_NIC, ""));
        binding.editTextRegion.setText(sharedPreferences.getString(KEY_REGION, ""));
        binding.editTextReligion.setText(sharedPreferences.getString(KEY_RELIGION, ""));

        String savedDistrict = sharedPreferences.getString(KEY_DISTRICT, null);
        if (savedDistrict != null) {
            String[] districts = getResources().getStringArray(R.array.districts_array);
            for (int i = 0; i < districts.length; i++) {
                if (districts[i].equals(savedDistrict)) {
                    binding.spinnerDistrict.setText(savedDistrict, false);
                    break;
                }
            }
        }
    }

    private void saveProfileData() {
        String name = binding.editTextFullName.getText().toString().trim();
        String birthday = binding.editTextBirthday.getText().toString().trim();
        String nic = binding.editTextNic.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(birthday) || TextUtils.isEmpty(nic)) {
            Toast.makeText(getContext(), R.string.profile_validation_error, Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_BIRTHDAY, birthday);
        editor.putString(KEY_NIC, nic);
        editor.putString(KEY_DISTRICT, binding.spinnerDistrict.getText().toString());
        editor.putString(KEY_REGION, binding.editTextRegion.getText().toString().trim());
        editor.putString(KEY_RELIGION, binding.editTextReligion.getText().toString().trim());
        
        // Lat and Lng are saved in the marker drag listener
        
        editor.apply();
        Toast.makeText(getContext(), R.string.profile_saved_toast, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);

        checkLocationPermission();
        
        googleMap.setOnMapClickListener(latLng -> dropOrMoveMarker(latLng, null));
        googleMap.setOnMapLongClickListener(latLng -> dropOrMoveMarker(latLng, null));
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override public void onMarkerDragStart(@NonNull Marker marker) {}
            @Override public void onMarkerDrag(@NonNull Marker marker) {}
            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                dropOrMoveMarker(marker.getPosition(), marker);
            }
        });
    }
    
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLastKnownLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return; // Should not happen if check is done before calling
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            LatLng userLocation;
            if (location != null) {
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            } else {
                // Default to a location if last known is null (e.g., Colombo)
                userLocation = new LatLng(6.9271, 79.8612);
            }

            // Check for saved location
            float lat = sharedPreferences.getFloat(KEY_LAT, (float) userLocation.latitude);
            float lng = sharedPreferences.getFloat(KEY_LNG, (float) userLocation.longitude);
            LatLng finalPosition = new LatLng(lat, lng);
            
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(finalPosition, 15f));
            dropOrMoveMarker(finalPosition, null);
            
             if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
             }

        });
    }

    private void dropOrMoveMarker(@NonNull LatLng position, Marker existing) {
        if (existing != null) {
            activeMarker = existing;
            activeMarker.setPosition(position);
        } else {
            if (activeMarker != null) {
                activeMarker.setPosition(position);
            } else {
                googleMap.clear();
                activeMarker = googleMap.addMarker(new MarkerOptions().position(position).title("Selected Location").draggable(true));
            }
        }
        sharedPreferences.edit()
                .putFloat(KEY_LAT, (float) position.latitude)
                .putFloat(KEY_LNG, (float) position.longitude)
                .apply();
        updateAddress(position);
    }

    private void updateAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                binding.textSelectedAddressProfile.setText(address);
                sharedPreferences.edit().putString(KEY_ADDRESS, address).apply();
            }
        } catch (IOException ignored) {}
    }

    private void launchPlacesSearch() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        android.content.Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("LK")
                .build(requireContext());
        placesLauncher.launch(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        binding = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
} 