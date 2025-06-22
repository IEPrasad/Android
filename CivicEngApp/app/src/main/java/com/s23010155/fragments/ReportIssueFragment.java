package com.s23010155.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.chip.Chip;
import com.s23010155.R;
import com.s23010155.database.IssueDatabaseHelper;
import com.s23010155.databinding.FragmentReportIssueBinding;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ReportIssueFragment extends Fragment implements OnMapReadyCallback {

    private FragmentReportIssueBinding binding;
    private IssueDatabaseHelper dbHelper;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLatLng;
    private Uri photoUri;

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher;
    private final ActivityResultLauncher<Intent> placesLauncher;
    private String selectedCategory = null;

    public ReportIssueFragment() {
        requestPermissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            // We'll check permissions right before using the feature.
        });

        placesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        selectedLatLng = place.getLatLng();
                        if (selectedLatLng != null && googleMap != null) {
                            googleMap.clear();
                            googleMap.addMarker(new MarkerOptions().position(selectedLatLng).title(place.getName()));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f));
                            binding.textSelectedAddress.setText(place.getAddress());
                            binding.editTextLocationSearch.setText(place.getName());
                        }
                    }
                }
        );
    }

    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        android.graphics.Bitmap imageBitmap = (android.graphics.Bitmap) extras.get("data");
                        binding.imagePreview.setImageBitmap(imageBitmap);
                        updatePhotoUI(true);
                        photoUri = null; // URI not available for thumbnail
                    }
                }
            });

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    photoUri = result.getData().getData();
                    binding.imagePreview.setImageURI(photoUri);
                    updatePhotoUI(true);
                }
            });


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReportIssueBinding.inflate(inflater, container, false);
        dbHelper = new IssueDatabaseHelper(getContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        binding.mapViewReport.onCreate(savedInstanceState);
        binding.mapViewReport.getMapAsync(this);
        requestPermissions();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
    }

    private void requestPermissions() {
        requestPermissionsLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
        });
    }

    private void setupUI() {
        // Category Chips
        setupCategoryChips();

        // Location Search
        binding.editTextLocationSearch.setEnabled(false);
        binding.editTextLocationSearch.setHint("Map is loading...");
        binding.editTextLocationSearch.setOnClickListener(v -> launchPlacesSearch());

        // Photo Card
        binding.photoCard.setOnClickListener(v -> showPhotoSourceDialog());
        binding.removePhotoButton.setOnClickListener(v -> {
            photoUri = null;
            binding.imagePreview.setImageDrawable(null);
            updatePhotoUI(false);
        });
        updatePhotoUI(false);

        // Submit Button
        binding.buttonSubmitIssue.setOnClickListener(v -> submitIssue());
    }

    private void setupCategoryChips() {
        String[] categories = getResources().getStringArray(R.array.issue_categories_array);
        for (String category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> selectedCategory = chip.getText().toString());
            binding.categoryChipGroup.addView(chip);
        }
    }

    private void updatePhotoUI(boolean hasPhoto) {
        binding.addPhotoLayout.setVisibility(hasPhoto ? View.GONE : View.VISIBLE);
        binding.removePhotoButton.setVisibility(hasPhoto ? View.VISIBLE : View.GONE);
    }

    private void launchPlacesSearch() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(requireContext());
        placesLauncher.launch(intent);
    }

    private void showPhotoSourceDialog() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                handleCameraPermission();
            } else if (options[item].equals("Choose from Gallery")) {
                handleGalleryPermission();
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    
    private void handleCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureLauncher.launch(takePictureIntent);
        } else {
            requestPermissionsLauncher.launch(new String[]{Manifest.permission.CAMERA});
        }
    }
    
    private void handleGalleryPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(pickPhoto);
        } else {
            requestPermissionsLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
        }
    }

    private void submitIssue() {
        String title = binding.editTextProblemTitle.getText().toString().trim();
        String description = binding.editTextDescription.getText().toString().trim();
        String locationName = binding.textSelectedAddress.getText().toString();

        if (selectedCategory == null || TextUtils.isEmpty(title) || selectedLatLng == null) {
            Toast.makeText(getContext(), R.string.report_issue_validation_error, Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(IssueDatabaseHelper.COLUMN_CATEGORY, selectedCategory);
        values.put(IssueDatabaseHelper.COLUMN_TITLE, title);
        values.put(IssueDatabaseHelper.COLUMN_DESCRIPTION, description);
        values.put(IssueDatabaseHelper.COLUMN_LOCATION_NAME, locationName);
        values.put(IssueDatabaseHelper.COLUMN_LATITUDE, selectedLatLng.latitude);
        values.put(IssueDatabaseHelper.COLUMN_LONGITUDE, selectedLatLng.longitude);
        if (photoUri != null) {
            values.put(IssueDatabaseHelper.COLUMN_PHOTO_URI, photoUri.toString());
        }
        db.insert(IssueDatabaseHelper.TABLE_ISSUES, null, values);
        Toast.makeText(getContext(), R.string.report_issue_success_toast, Toast.LENGTH_SHORT).show();
        clearForm();
    }
    
    private void clearForm() {
        binding.categoryChipGroup.clearCheck();
        selectedCategory = null;
        binding.editTextProblemTitle.setText("");
        binding.editTextDescription.setText("");
        binding.textSelectedAddress.setText("");
        binding.editTextLocationSearch.setText("");
        photoUri = null;
        updatePhotoUI(false);
        if (googleMap != null) {
            googleMap.clear();
        }
        selectedLatLng = null;
    }


    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        binding.editTextLocationSearch.setEnabled(true);
        binding.editTextLocationSearch.setHint("Search or pan map to select");
        LatLng colombo = new LatLng(6.9271, 79.8612);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(colombo, 12f));

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastKnownLocation();
            googleMap.setMyLocationEnabled(true);
        }

        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng));
            selectedLatLng = latLng;
            getAddressFromLatLng(latLng);
        });
    }

    private void getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
                googleMap.addMarker(new MarkerOptions().position(userLocation));
                selectedLatLng = userLocation;
                getAddressFromLatLng(userLocation);
            }
        });
    }
    
    private void getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                binding.textSelectedAddress.setText(address.getAddressLine(0));
            } else {
                binding.textSelectedAddress.setText(R.string.report_issue_address_not_found);
            }
        } catch (IOException e) {
            e.printStackTrace();
            binding.textSelectedAddress.setText(R.string.report_issue_address_not_found);
        }
    }

    // MapView lifecycle methods
    @Override public void onResume() { super.onResume(); binding.mapViewReport.onResume(); }
    @Override public void onPause() { super.onPause(); binding.mapViewReport.onPause(); }
    @Override public void onDestroyView() { super.onDestroyView(); binding.mapViewReport.onDestroy(); binding = null; }
    @Override public void onLowMemory() { super.onLowMemory(); binding.mapViewReport.onLowMemory(); }
    @Override public void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); binding.mapViewReport.onSaveInstanceState(outState); }
} 