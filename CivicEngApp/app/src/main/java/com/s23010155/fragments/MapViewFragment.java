package com.s23010155.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.chip.Chip;
import com.s23010155.R;
import com.s23010155.database.MockIssues;
import com.s23010155.databinding.FragmentMapViewBinding;
import com.s23010155.database.PollDatabaseHelper;
import com.s23010155.models.Issue;

import java.io.IOException;
import java.util.List;

public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FragmentMapViewBinding binding;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng lastSearchedLatLng = null;
    private String lastSearchedCategory = null;
    private PollDatabaseHelper pollDb;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapViewBinding.inflate(inflater, container, false);
        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        pollDb = new PollDatabaseHelper(requireContext());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Make the button act as: search if query present, otherwise GPS locate-me.
        binding.buttonLocateMe.setOnClickListener(v -> {
            String query = binding.searchBar.getText().toString().trim();
            if (!query.isEmpty()) {
                searchLocation();
            } else {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    Toast.makeText(getContext(), "Location permission required to locate you", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null &&
                            event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (event == null || !event.isShiftPressed()) {
                    // the user is done typing.
                    searchLocation();
                    return true; // consume.
                }
            }
            return false; // pass on to other listeners.
        });

        // Handle tapping the search icon at the end of the EditText
        binding.searchBar.setOnTouchListener((v1, motionEvent) -> {
            final int DRAWABLE_RIGHT = 2;
            if (motionEvent.getAction() == android.view.MotionEvent.ACTION_UP) {
                android.widget.EditText et = binding.searchBar;
                if (et.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                    int drawableWidth = et.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    float touchX = motionEvent.getX();
                    int rightEdge = et.getWidth() - et.getPaddingRight();
                    int leftEdgeOfRightDrawable = rightEdge - drawableWidth;
                    if (touchX >= leftEdgeOfRightDrawable) {
                        searchLocation();
                        return true;
                    }
                }
            }
            return false;
        });

        setupCategoryChips();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        enableMyLocation();
        // Set a default map location
        LatLng defaultLocation = new LatLng(34.0522, -118.2437); // Example: Los Angeles
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));

        // Render simple district-based aggregates for current month if available (mock by centroids)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int year = cal.get(java.util.Calendar.YEAR);
        int month = cal.get(java.util.Calendar.MONTH) + 1;
        java.util.Map<String, int[]> totals = pollDb.getDistrictTotals(year, month);
        for (java.util.Map.Entry<String, int[]> e : totals.entrySet()) {
            String district = e.getKey();
            int good = e.getValue()[0];
            int bad = e.getValue()[1];
            int total = good + bad;
            if (total == 0) continue;
            int goodPct = (int) ((good * 100.0f) / total);
            LatLng centroid = guessDistrictCentroid(district);
            if (centroid != null) {
                String title = district + ": Good " + goodPct + "% (" + total + ")";
                googleMap.addMarker(new MarkerOptions().position(centroid).title(title));
            }
        }
    }

    private void setupCategoryChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            googleMap.clear(); // Clear existing markers
            if (checkedIds.isEmpty()) {
                lastSearchedCategory = null;
                return;
            }

            Chip checkedChip = group.findViewById(checkedIds.get(0));
            if (checkedChip != null) {
                String category = checkedChip.getText().toString();
                lastSearchedCategory = category;
                filterAndShowIssues(category);
            }
        });
    }

    private void filterAndShowIssues(String category) {
        List<Issue> allIssues = MockIssues.getMockIssues();
        boolean foundAny = false;
        for (Issue issue : allIssues) {
            if (issue.getCategory().equalsIgnoreCase(category)) {
                LatLng issueLocation = new LatLng(issue.getLatitude(), issue.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(issueLocation).title(issue.getCategory()));
                foundAny = true;
            }
        }
        if (lastSearchedLatLng != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastSearchedLatLng, 13f));
        }
        if (!foundAny) {
            Toast.makeText(getContext(), "No centers found for this category", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchLocation() {
        String searchString = binding.searchBar.getText().toString();
        if (searchString.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hide keyboard
        try {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(binding.searchBar.getWindowToken(), 0);
        } catch (Exception ignored) {}

        if (googleMap == null) {
            Toast.makeText(getContext(), "Map is not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(getContext());
        try {
            // Restrict search within Sri Lanka bounds to avoid results from other countries
            // Approximate bounding box: southwest (5.7, 79.5), northeast (9.9, 81.9)
            List<Address> addressList = geocoder.getFromLocationName(
                    searchString,
                    1,
                    5.7, 79.5, 9.9, 81.9
            );
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                lastSearchedLatLng = latLng;
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                // If a category is selected, show filtered centers near this location
                if (lastSearchedCategory != null) {
                    filterAndShowIssues(lastSearchedCategory);
                }
            } else {
                // Try fallback: if user typed a district from our list, center to an approximate centroid
                LatLng centroid = guessDistrictCentroid(searchString.trim());
                if (centroid != null) {
                    lastSearchedLatLng = centroid;
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centroid, 12f));
                    if (lastSearchedCategory != null) {
                        filterAndShowIssues(lastSearchedCategory);
                    }
                } else {
                    Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error searching for location", Toast.LENGTH_SHORT).show();
        }
    }

    private LatLng guessDistrictCentroid(String district) {
        // Simple hardcoded hints for Sri Lanka common districts; fallback to default
        if (district == null) return null;
        switch (district) {
            case "Colombo": return new LatLng(6.9271, 79.8612);
            case "Gampaha": return new LatLng(7.0897, 80.0088);
            case "Galle": return new LatLng(6.0535, 80.2210);
            case "Kandy": return new LatLng(7.2906, 80.6337);
            case "Kurunegala": return new LatLng(7.4863, 80.3647);
            case "Jaffna": return new LatLng(9.6615, 80.0255);
            case "Matara": return new LatLng(5.9549, 80.5540);
            case "Anuradhapura": return new LatLng(8.3114, 80.4037);
            case "Badulla": return new LatLng(6.9896, 81.0568);
            case "Batticaloa": return new LatLng(7.7170, 81.7000);
            case "Hambantota": return new LatLng(6.1246, 81.1016);
            case "Kalutara": return new LatLng(6.5854, 80.1250);
            case "Kegalle": return new LatLng(7.2513, 80.3464);
            case "Kilinochchi": return new LatLng(9.3897, 80.4068);
            case "Mannar": return new LatLng(8.9809, 79.9048);
            case "Matale": return new LatLng(7.4675, 80.6234);
            case "Monaragala": return new LatLng(6.8726, 81.3507);
            case "Mullaitivu": return new LatLng(9.2671, 80.8128);
            case "Nuwara Eliya": return new LatLng(6.9708, 80.7829);
            case "Polonnaruwa": return new LatLng(7.9392, 81.0000);
            case "Puttalam": return new LatLng(8.0408, 79.8395);
            case "Ratnapura": return new LatLng(6.7056, 80.3847);
            case "Trincomalee": return new LatLng(8.5874, 81.2152);
            case "Vavuniya": return new LatLng(8.7514, 80.4971);
            case "Ampara": return new LatLng(7.3000, 81.6667);
            default: return null;
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }
} 