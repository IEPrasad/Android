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
import com.google.android.gms.maps.model.Marker;
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
    private Marker activeMarker;

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
        // Ensure Places SDK is initialized
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

        // Location Search: allow typing + search icon + overlay launch
        binding.editTextLocationSearch.setEnabled(true);
        binding.editTextLocationSearch.setHint("Search or pan map to select");
        binding.editTextLocationSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getAction() == android.view.KeyEvent.ACTION_DOWN && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {
                performGeocodeSearch();
                return true;
            }
            return false;
        });
        binding.editTextLocationSearch.setOnClickListener(v -> launchPlacesSearch());
        // District dropdown
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.districts_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerIssueDistrict.setAdapter(adapter);
        binding.editTextLocationSearch.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (binding.editTextLocationSearch.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                    int drawableWidth = binding.editTextLocationSearch.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    float touchX = event.getX();
                    int rightEdge = binding.editTextLocationSearch.getWidth() - binding.editTextLocationSearch.getPaddingRight();
                    int leftEdgeOfRightDrawable = rightEdge - drawableWidth;
                    if (touchX >= leftEdgeOfRightDrawable) {
                        // Try overlay first; if user typed text, also geocode
                        if (binding.editTextLocationSearch.getText() != null && binding.editTextLocationSearch.getText().length() > 0) {
                            performGeocodeSearch();
                        } else {
                            launchPlacesSearch();
                        }
                        return true;
                    }
                }
            }
            return false;
        });

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
        String district = binding.spinnerIssueDistrict.getText().toString();
        String locationName = binding.textSelectedAddress.getText().toString();

        if (selectedCategory == null || TextUtils.isEmpty(title) || selectedLatLng == null || TextUtils.isEmpty(district)) {
            Toast.makeText(getContext(), R.string.report_issue_validation_error, Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(IssueDatabaseHelper.COLUMN_CATEGORY, selectedCategory);
        values.put(IssueDatabaseHelper.COLUMN_TITLE, title);
        values.put(IssueDatabaseHelper.COLUMN_DESCRIPTION, description);
        values.put(IssueDatabaseHelper.COLUMN_DISTRICT, district);
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
            dropOrMoveMarker(latLng, null);
        });

        googleMap.setOnMapLongClickListener(latLng -> {
            dropOrMoveMarker(latLng, null);
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override public void onMarkerDragStart(@NonNull Marker marker) { }
            @Override public void onMarkerDrag(@NonNull Marker marker) { }
            @Override public void onMarkerDragEnd(@NonNull Marker marker) {
                dropOrMoveMarker(marker.getPosition(), marker);
            }
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
                dropOrMoveMarker(userLocation, null);
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

    private void performGeocodeSearch() {
        String query = String.valueOf(binding.editTextLocationSearch.getText()).trim();
        if (query.isEmpty()) {
            launchPlacesSearch();
            return;
        }
        if (googleMap == null) return;
        // Hide keyboard
        try {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(binding.editTextLocationSearch.getWindowToken(), 0);
        } catch (Exception ignored) {}

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            // Sri Lanka bounds SW (5.7, 79.5) NE (9.9, 81.9)
            List<Address> results = geocoder.getFromLocationName(query, 1, 5.7, 79.5, 9.9, 81.9);
            if (results != null && !results.isEmpty()) {
                Address a = results.get(0);
                LatLng ll = new LatLng(a.getLatitude(), a.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 16f));
                dropOrMoveMarker(ll, null);
                binding.textSelectedAddress.setText(a.getAddressLine(0));
            } else {
                // fallback to places overlay
                launchPlacesSearch();
            }
        } catch (IOException e) {
            e.printStackTrace();
            launchPlacesSearch();
        }
    }

    private void dropOrMoveMarker(@NonNull LatLng position, @Nullable Marker existing) {
        selectedLatLng = position;
        if (existing != null) {
            activeMarker = existing;
            activeMarker.setPosition(position);
        } else {
            if (activeMarker != null) {
                activeMarker.setPosition(position);
            } else {
                googleMap.clear();
                activeMarker = googleMap.addMarker(new MarkerOptions().position(position).draggable(true));
            }
        }
        getAddressFromLatLng(position);
    }

    // MapView lifecycle methods
    @Override public void onResume() { super.onResume(); binding.mapViewReport.onResume(); }
    @Override public void onPause() { super.onPause(); binding.mapViewReport.onPause(); }
    @Override public void onDestroyView() { super.onDestroyView(); binding.mapViewReport.onDestroy(); binding = null; }
    @Override public void onLowMemory() { super.onLowMemory(); binding.mapViewReport.onLowMemory(); }
    @Override public void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); binding.mapViewReport.onSaveInstanceState(outState); }
} 