package com.s23010155.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.s23010155.R;
import com.s23010155.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SharedPreferences settingsPrefs;
    private static final String PREFS_NAME = "SettingsPrefs";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_NOTIFICATIONS = "notificationsEnabled";
    private static final String KEY_LANGUAGE = "selectedLanguage";
    private static final String KEY_PASSWORD = "userPassword"; // In a real app, never store passwords this way

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        settingsPrefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
        loadSettings();
    }

    private void setupUI() {
        setupDarkModeSwitch();
        // Language Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.languages_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLanguage.setAdapter(adapter);

        // Clear Data Button
        binding.buttonClearData.setOnClickListener(v -> showClearDataConfirmation());

        // Save Button
        binding.buttonSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void setupDarkModeSwitch() {
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    private void loadSettings() {
        // Remove listener before setting checked state
        binding.switchDarkMode.setOnCheckedChangeListener(null);
        binding.switchDarkMode.setChecked(settingsPrefs.getBoolean(KEY_DARK_MODE, false));
        // Re-add listener
        setupDarkModeSwitch();

        // Load Notifications
        binding.switchNotifications.setChecked(settingsPrefs.getBoolean(KEY_NOTIFICATIONS, true));

        // Load Language
        String savedLanguage = settingsPrefs.getString(KEY_LANGUAGE, "English");
        String[] languages = getResources().getStringArray(R.array.languages_array);
        for (int i = 0; i < languages.length; i++) {
            if (languages[i].equals(savedLanguage)) {
                binding.spinnerLanguage.setText(savedLanguage, false);
                break;
            }
        }
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = settingsPrefs.edit();

        // Save Dark Mode
        editor.putBoolean(KEY_DARK_MODE, binding.switchDarkMode.isChecked());

        // Save Notifications
        editor.putBoolean(KEY_NOTIFICATIONS, binding.switchNotifications.isChecked());

        // Save Language
        editor.putString(KEY_LANGUAGE, binding.spinnerLanguage.getText().toString());

        // Password Change Logic
        String currentPass = binding.editTextCurrentPassword.getText().toString();
        String newPass = binding.editTextNewPassword.getText().toString();
        String confirmPass = binding.editTextConfirmPassword.getText().toString();

        if (!TextUtils.isEmpty(currentPass) || !TextUtils.isEmpty(newPass) || !TextUtils.isEmpty(confirmPass)) {
            // In a real app, you would verify the current password against a secure source.
            // Here, we'll just check if it's not empty and if new passwords match.
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(getContext(), R.string.settings_password_mismatch_error, Toast.LENGTH_SHORT).show();
                return; // Stop saving if passwords don't match
            }
            // "Save" the new password (highly insecure, for demonstration only)
            editor.putString(KEY_PASSWORD, newPass);
            Toast.makeText(getContext(), R.string.settings_password_changed_toast, Toast.LENGTH_SHORT).show();
        }

        editor.apply();

        Toast.makeText(getContext(), R.string.settings_saved_toast, Toast.LENGTH_LONG).show();
    }

    private void showClearDataConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_clear_data_confirm_title)
                .setMessage(R.string.settings_clear_data_confirm_message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Clear all SharedPreferences for this app
                    requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
                    requireActivity().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE).edit().clear().apply();
                    requireActivity().getSharedPreferences("VotePolicyPrefs", Context.MODE_PRIVATE).edit().clear().apply();
                    Toast.makeText(getContext(), R.string.settings_clear_data_toast, Toast.LENGTH_SHORT).show();
                    // Reload the screen or restart the app
                    NavHostFragment.findNavController(this).navigate(R.id.action_settingsFragment_self);
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 