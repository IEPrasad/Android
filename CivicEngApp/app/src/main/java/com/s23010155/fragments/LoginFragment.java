package com.s23010155.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.s23010155.R;
import com.s23010155.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // This should navigate to the Dashboard
        binding.buttonSignIn.setOnClickListener(v ->
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_loginFragment_to_dashboardFragment));

        // This should navigate to the Register screen
        binding.textViewSignUpLink.setOnClickListener(v ->
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_loginFragment_to_registerFragment));
        
        // TODO: Implement "Forgot Password" functionality for textViewForgotPassword
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 