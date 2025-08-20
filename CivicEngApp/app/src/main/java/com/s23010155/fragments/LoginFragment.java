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
import com.s23010155.databinding.FragmentLoginBinding;
import com.s23010155.database.AuthDatabaseHelper;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthDatabaseHelper authDatabaseHelper;
    private boolean isPasswordVisible = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authDatabaseHelper = new AuthDatabaseHelper(requireContext());

        // Toggle password visibility when end drawable is tapped
        setupPasswordVisibilityToggle(binding.editTextPassword);

        binding.buttonSignIn.setOnClickListener(v -> attemptLogin());

        // This should navigate to the Register screen
        binding.textViewSignUpLink.setOnClickListener(v ->
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_loginFragment_to_registerFragment));
        
        // TODO: Implement "Forgot Password" functionality for textViewForgotPassword
    }

    private void attemptLogin() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString();

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show();
            return;
        }
        if (password == null || password.length() < 4) {
            Toast.makeText(requireContext(), getString(R.string.error_password_too_short), Toast.LENGTH_SHORT).show();
            return;
        }

        binding.buttonSignIn.setEnabled(false);
        boolean ok = authDatabaseHelper.validateCredentials(email, password);
        binding.buttonSignIn.setEnabled(true);
        if (ok) {
            Toast.makeText(requireContext(), getString(R.string.auth_login_success), Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_loginFragment_to_dashboardFragment);
        } else {
            Toast.makeText(requireContext(), getString(R.string.auth_invalid_credentials), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPasswordVisibilityToggle(EditText passwordEditText) {
        passwordEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2; // index for drawableEnd
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (passwordEditText.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                    int drawableWidth = passwordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    float touchX = event.getX();
                    int rightEdge = passwordEditText.getWidth() - passwordEditText.getPaddingRight();
                    int leftEdgeOfRightDrawable = rightEdge - drawableWidth;
                    if (touchX >= leftEdgeOfRightDrawable) {
                        togglePasswordVisibility(passwordEditText);
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private void togglePasswordVisibility(EditText passwordEditText) {
        isPasswordVisible = !isPasswordVisible;
        int cursorPosition = passwordEditText.getSelectionStart();
        if (isPasswordVisible) {
            passwordEditText.setTransformationMethod((TransformationMethod) null);
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye_open, 0);
        } else {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye_closed, 0);
        }
        passwordEditText.setSelection(cursorPosition);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 