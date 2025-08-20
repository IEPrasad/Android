package com.s23010155.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.s23010155.R;
import com.s23010155.databinding.FragmentRegisterBinding;
import com.s23010155.database.AuthDatabaseHelper;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthDatabaseHelper authDatabaseHelper;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authDatabaseHelper = new AuthDatabaseHelper(requireContext());

        setupPasswordVisibilityToggle(binding.editTextPassword, true);
        setupPasswordVisibilityToggle(binding.editTextConfirmPassword, false);

        binding.buttonSignUp.setOnClickListener(v -> attemptRegister());
        
        binding.textViewSignInLink.setOnClickListener(v ->
                NavHostFragment.findNavController(RegisterFragment.this)
                        .navigate(R.id.action_registerFragment_to_loginFragment));
    }

    private void attemptRegister() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString();
        String confirmPassword = binding.editTextConfirmPassword.getText().toString();
        String nic = binding.editTextNic.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show();
            return;
        }
        if (password == null || password.length() < 4) {
            Toast.makeText(requireContext(), getString(R.string.error_password_too_short), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidNic(nic)) {
            Toast.makeText(requireContext(), getString(R.string.error_invalid_nic), Toast.LENGTH_SHORT).show();
            return;
        }
        if (authDatabaseHelper.isNicExists(nic)) {
            Toast.makeText(requireContext(), getString(R.string.error_nic_exists), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(requireContext(), getString(R.string.error_passwords_do_not_match), Toast.LENGTH_SHORT).show();
            return;
        }

        if (authDatabaseHelper.isUserExists(email)) {
            Toast.makeText(requireContext(), getString(R.string.auth_email_exists), Toast.LENGTH_SHORT).show();
            return;
        }

        binding.buttonSignUp.setEnabled(false);
        boolean created = authDatabaseHelper.registerUser(email, password, nic);
        binding.buttonSignUp.setEnabled(true);
        if (created) {
            Toast.makeText(requireContext(), getString(R.string.auth_register_success), Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(RegisterFragment.this)
                    .navigate(R.id.action_registerFragment_to_dashboardFragment);
        } else {
            Toast.makeText(requireContext(), getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPasswordVisibilityToggle(EditText passwordEditText, boolean isPrimary) {
        passwordEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2; // index for drawableEnd
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (passwordEditText.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                    int drawableWidth = passwordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    float touchX = event.getX();
                    int rightEdge = passwordEditText.getWidth() - passwordEditText.getPaddingRight();
                    int leftEdgeOfRightDrawable = rightEdge - drawableWidth;
                    if (touchX >= leftEdgeOfRightDrawable) {
                        togglePasswordVisibility(passwordEditText, isPrimary);
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private void togglePasswordVisibility(EditText passwordEditText, boolean isPrimary) {
        if (isPrimary) {
            isPasswordVisible = !isPasswordVisible;
        } else {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
        }
        boolean visible = isPrimary ? isPasswordVisible : isConfirmPasswordVisible;
        int cursorPosition = passwordEditText.getSelectionStart();
        if (visible) {
            passwordEditText.setTransformationMethod((TransformationMethod) null);
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye_open, 0);
        } else {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye_closed, 0);
        }
        passwordEditText.setSelection(cursorPosition);
    }

    private boolean isValidNic(String nic) {
        if (nic == null) return false;
        String trimmed = nic.trim();
        // Accept formats like old 9 digits + [VvXx] or new 12 digits
        return trimmed.matches("^[0-9]{9}[VvXx]$") || trimmed.matches("^[0-9]{12}$");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 